package org.apache.maven.scm.tck.command.checkin;

/*
 * Copyright 2003-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;

/**
 * This test tests the check out command.
 * <p/>
 * A check out has to produce these files:
 * <p/>
 * <ul>
 * <li>/pom.xml</li>
 * <li>/readme.txt</li>
 * <li>/src/main/java/Application.java</li>
 * <li>/src/test/java/Test.java</li>
 * </ul>
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public abstract class CheckInCommandTckTest
    extends ScmTestCase
{
    private File workingDirectory;

    private ScmManager scmManager;

    private ScmRepository repository;

    private File assertionDirectory;

    // ----------------------------------------------------------------------
    // Methods the provider test has to implement
    // ----------------------------------------------------------------------

    public abstract String getScmUrl()
        throws Exception;

    /**
     * Copy the existing checked in repository to the working directory.
     * <p/>
     * (src/test/repository/my-cvs-repository)
     *
     * @throws Exception
     */
    public abstract void initRepo()
        throws Exception;

    // ----------------------------------------------------------------------
    // Directories the test must use
    // ----------------------------------------------------------------------

    protected File getRepositoryRoot()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/repository/trunk" );
    }

    protected File getWorkingCopy()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/working-copy" );
    }

    protected File getAssertionCopy()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/assertion-copy" );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        File repositoryRoot = getRepositoryRoot();

        if ( repositoryRoot.exists() )
        {
            FileUtils.deleteDirectory( repositoryRoot );
        }

        assertTrue( "Could not make the repository root directory: " + repositoryRoot.getAbsolutePath(), repositoryRoot
            .mkdirs() );

        workingDirectory = getWorkingCopy();

        if ( workingDirectory.exists() )
        {
            FileUtils.deleteDirectory( workingDirectory );
        }

        assertTrue( "Could not make the working directory: " + workingDirectory.getAbsolutePath(), workingDirectory
            .mkdirs() );

        assertionDirectory = getAssertionCopy();

        if ( assertionDirectory.exists() )
        {
            FileUtils.deleteDirectory( assertionDirectory );
        }

        assertTrue( "Could not make the assertion directory: " + assertionDirectory.getAbsolutePath(),
                    assertionDirectory.mkdirs() );

        initRepo();

        scmManager = getScmManager();

        repository = scmManager.makeScmRepository( getScmUrl() );

        CheckOutScmResult result = scmManager.getProviderByUrl( getScmUrl() )
            .checkOut( repository, new ScmFileSet( workingDirectory ), null );

        assertResultIsSuccess( result );
    }

    public void testCheckInCommandTest()
        throws Exception
    {
        // Make sure that the correct files was checked out
        File fooJava = new File( workingDirectory, "src/main/java/Foo.java" );

        File barJava = new File( workingDirectory, "src/main/java/Bar.java" );

        File readmeTxt = new File( workingDirectory, "readme.txt" );

        assertFalse( "check Foo.java doesn't yet exist", fooJava.canRead() );

        assertFalse( "check Bar.java doesn't yet exist", barJava.canRead() );

        assertTrue( "check can read readme.txt", readmeTxt.canRead() );

        // Change the files
        createFooJava( fooJava );

        createBarJava( barJava );

        changeReadmeTxt( readmeTxt );

        AddScmResult addResult = scmManager.getProviderByUrl( getScmUrl() )
            .add( repository, new ScmFileSet( workingDirectory, "src/main/java/Foo.java", null ) );

        assertResultIsSuccess( addResult );

        CheckInScmResult result = scmManager.getProviderByUrl( getScmUrl() )
            .checkIn( repository, new ScmFileSet( workingDirectory ), null, "Commit message" );

        assertResultIsSuccess( result );

        assertNull( "The provider message wasn't null", result.getProviderMessage() );

        assertNull( "The command output wasn't null", result.getCommandOutput() );

        List files = result.getCheckedInFiles();

        assertNotNull( files );

        assertEquals( 2, files.size() );

        ScmFile file1 = (ScmFile) files.get( 0 );

        assertEquals( ScmFileStatus.CHECKED_IN, file1.getStatus() );

        assertPath( "/test-repo/check-in/Foo.java", file1.getPath() );

        ScmFile file2 = (ScmFile) files.get( 1 );

        assertEquals( ScmFileStatus.CHECKED_IN, file2.getStatus() );

        assertPath( "/test-repo/check-in/readme.txt", file2.getPath() );

        assertNull( result.getProviderMessage() );

        assertNull( result.getCommandOutput() );

        CheckOutScmResult checkoutResult = scmManager.getProviderByUrl( getScmUrl() )
            .checkOut( repository, new ScmFileSet( assertionDirectory ), null );

        assertResultIsSuccess( checkoutResult );

        fooJava = new File( assertionDirectory, "src/main/java/Foo.java" );

        barJava = new File( assertionDirectory, "src/main/java/Bar.java" );

        readmeTxt = new File( assertionDirectory, "readme.txt" );

        assertTrue( "check can read Foo.java", fooJava.canRead() );

        assertFalse( "check Bar.java doesn't exist", barJava.canRead() );

        assertTrue( "check can read readme.txt", readmeTxt.canRead() );

        assertEquals( "check readme.txt contents", "changed file", FileUtils.fileRead( readmeTxt ) );
    }

    public void testCheckInCommandPartialFileset()
        throws Exception
    {
        // Make sure that the correct files was checked out
        File fooJava = new File( workingDirectory, "src/main/java/Foo.java" );

        File barJava = new File( workingDirectory, "src/main/java/Bar.java" );

        File readmeTxt = new File( workingDirectory, "readme.txt" );

        assertFalse( "check Foo.java doesn't yet exist", fooJava.canRead() );

        assertFalse( "check Bar.java doesn't yet exist", barJava.canRead() );

        assertTrue( "check can read readme.txt", readmeTxt.canRead() );

        // Change the files
        createFooJava( fooJava );

        createBarJava( barJava );

        changeReadmeTxt( readmeTxt );

        AddScmResult addResult = scmManager.getProviderByUrl( getScmUrl() )
            .add( repository, new ScmFileSet( workingDirectory, "src/main/java/Foo.java", null ) );

        assertResultIsSuccess( addResult );

        CheckInScmResult result = scmManager.getProviderByUrl( getScmUrl() ).checkIn( repository,
                                                                                      new ScmFileSet( workingDirectory,
                                                                                                      "**/Foo.java",
                                                                                                      null ),
                                                                                      null,
                                                                                      "Commit message" );

        assertResultIsSuccess( result );

        assertNull( "The provider message wasn't null", result.getProviderMessage() );

        assertNull( "The command output wasn't null", result.getCommandOutput() );

        List files = result.getCheckedInFiles();

        assertNotNull( files );

        assertEquals( 1, files.size() );

        ScmFile file1 = (ScmFile) files.get( 0 );

        assertEquals( ScmFileStatus.CHECKED_IN, file1.getStatus() );

        assertPath( "/test-repo/check-in/Foo.java", file1.getPath() );

        assertNull( result.getProviderMessage() );

        assertNull( result.getCommandOutput() );

        CheckOutScmResult checkoutResult = scmManager.getProviderByUrl( getScmUrl() )
            .checkOut( repository, new ScmFileSet( assertionDirectory ), null );

        assertResultIsSuccess( checkoutResult );

        fooJava = new File( assertionDirectory, "src/main/java/Foo.java" );

        barJava = new File( assertionDirectory, "src/main/java/Bar.java" );

        readmeTxt = new File( assertionDirectory, "readme.txt" );

        assertTrue( "check can read Foo.java", fooJava.canRead() );

        assertFalse( "check Bar.java doesn't exist", barJava.canRead() );

        assertTrue( "check can read readme.txt", readmeTxt.canRead() );

        assertEquals( "check readme.txt contents", "/readme.txt", FileUtils.fileRead( readmeTxt ) );
    }

    private void createFooJava( File fooJava )
        throws Exception
    {
        FileWriter output = new FileWriter( fooJava );

        PrintWriter printer = new PrintWriter( output );

        printer.println( "public class Foo" );
        printer.println( "{" );

        printer.println( "    public void foo()" );
        printer.println( "    {" );
        printer.println( "        int i = 10;" );
        printer.println( "    }" );

        printer.println( "}" );

        printer.close();

        output.close();
    }

    private void createBarJava( File barJava )
        throws Exception
    {
        FileWriter output = new FileWriter( barJava );

        PrintWriter printer = new PrintWriter( output );

        printer.println( "public class Bar" );
        printer.println( "{" );

        printer.println( "    public int bar()" );
        printer.println( "    {" );
        printer.println( "        return 20;" );
        printer.println( "    }" );

        printer.println( "}" );

        printer.close();

        output.close();
    }

    private void changeReadmeTxt( File readmeTxt )
        throws Exception
    {
        FileWriter output = new FileWriter( readmeTxt );

        output.write( "changed file" );

        output.close();
    }
}
