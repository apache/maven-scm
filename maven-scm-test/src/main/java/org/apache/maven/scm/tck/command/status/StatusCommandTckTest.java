package org.apache.maven.scm.tck.command.status;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * This test tests the status command.
 *
 * It works like this:
 *
 * <ol>
 *  <li>Check out the files to directory getWorkingCopy().
 *  <li>Check out the files to directory getUpdatingCopy().
 *  <li>Change the files in getWorkingCopy().
 *  <li>Commit the files in getWorkingCopy(). Note that the provider <b>must</b> not
 *      use the check in command as it can be guaranteed to work as it's not yet tested.
 *  <li>Use the update command in getUpdatingCopy() to assert that the files
 *      that was supposed to be updated actually was updated.
 * </ol>
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public abstract class StatusCommandTckTest
    extends ScmTestCase
{
    // ----------------------------------------------------------------------
    // Methods the test has to implement
    // ----------------------------------------------------------------------

    public abstract String getScmUrl()
        throws Exception;

    /**
     * Copy the existing checked in repository to the working directory.
     *
     * (src/test/repository/my-cvs-repository)
     *
     * @throws Exception
     */
    public abstract void initRepo()
        throws Exception;

    private void checkOut( File workingDirectory, ScmRepository repository )
        throws Exception
    {
        CheckOutScmResult result = getScmManager().getProviderByUrl( getScmUrl() )
            .checkOut( repository, new ScmFileSet( workingDirectory ), null );

        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );
    }

    private void addToRepository( File workingDirectory, File file, ScmRepository repository )
        throws Exception
    {
        AddScmResult result = getScmManager().getProviderByUrl( getScmUrl() ).add(
                                                                                   repository,
                                                                                   new ScmFileSet( workingDirectory,
                                                                                                   file ) );
        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        List addedFiles = result.getAddedFiles();

        assertEquals( "Expected 1 file in the added files list " + addedFiles, 1, addedFiles.size() );
    }

    private void commit( File workingDirectory, ScmRepository repository )
        throws Exception
    {
        CheckInScmResult result = getScmManager().getProviderByUrl( getScmUrl() )
            .checkIn( repository, new ScmFileSet( workingDirectory ), null, "No msg" );

        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        List committedFiles = result.getCheckedInFiles();

        assertEquals( "Expected 2 files in the committed files list " + committedFiles, 2, committedFiles.size() );
    }

    // ----------------------------------------------------------------------
    // Directories the test must use
    // ----------------------------------------------------------------------

    protected File getRepositoryRoot()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/repository" );
    }

    protected File getWorkingCopy()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/working-copy" );
    }

    protected File getUpdatingCopy()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/updating-copy" );
    }

    // ----------------------------------------------------------------------
    // The test implementation
    // ----------------------------------------------------------------------

    public void setUp()
        throws Exception
    {
        super.setUp();

        FileUtils.deleteDirectory( getRepositoryRoot() );

        FileUtils.deleteDirectory( getWorkingCopy() );

        FileUtils.deleteDirectory( getUpdatingCopy() );

        initRepo();
    }

    public void testStatusCommand()
        throws Exception
    {
        ScmRepository repository = makeScmRepository( getScmUrl() );

        checkOut( getWorkingCopy(), repository );

        checkOut( getUpdatingCopy(), repository );

        // ----------------------------------------------------------------------
        // Assert that the required files is there
        // ----------------------------------------------------------------------

        assertFile( getWorkingCopy(), "/pom.xml" );

        assertFile( getWorkingCopy(), "/readme.txt" );

        assertFile( getWorkingCopy(), "/src/main/java/Application.java" );

        assertFile( getWorkingCopy(), "/src/test/java/Test.java" );

        // ----------------------------------------------------------------------
        // Change the files
        // ----------------------------------------------------------------------

        /*
         * readme.txt is changed (changed file in the root directory)
         * project.xml is added (added file in the root directory)
         */

        // /readme.txt
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );

        // /project.xml
        ScmTestCase.makeFile( getWorkingCopy(), "/project.xml", "changed project.xml" );

        addToRepository( getWorkingCopy(), new File( "project.xml" ), repository );

        commit( getWorkingCopy(), repository );

        // /pom.xml
        ScmTestCase.makeFile( getUpdatingCopy(), "/pom.xml", "changed pom.xml" );

        // /src/test/java/org
        ScmTestCase.makeDirectory( getUpdatingCopy(), "/src/test/java/org" );

        addToRepository( getUpdatingCopy(), new File( "src/test/java/org" ), repository );

        // /src/main/java/org/Foo.java
        ScmTestCase.makeFile( getUpdatingCopy(), "/src/main/java/org/Foo.java" );

        addToRepository( getUpdatingCopy(), new File( "src/main/java/org" ), repository );

        // src/main/java/org/Foo.java
        addToRepository( getUpdatingCopy(), new File( "src/main/java/org/Foo.java" ), repository );

        ScmManager scmManager = getScmManager();

        // ----------------------------------------------------------------------
        // Check status the project
        // src/main/java/org/Foo.java is added
        // /pom.xml is modified
        // check that readme and project.xml are not updated/created
        // ----------------------------------------------------------------------

        StatusScmResult result = scmManager.getProviderByUrl( getScmUrl() )
            .status( repository, new ScmFileSet( getUpdatingCopy() ) );

        assertNotNull( "The command returned a null result.", result );

        assertResultIsSuccess( result );

        assertNull( "The provider message wasn't null", result.getProviderMessage() );

        assertNull( "The command output wasn't null", result.getCommandOutput() );

        List changedFiles = result.getChangedFiles();

        assertEquals( "Expected 2 files in the updated files list " + changedFiles, 2, changedFiles.size() );

        // ----------------------------------------------------------------------
        // Assert the files in the updated files list
        // ----------------------------------------------------------------------

        Iterator files = new TreeSet( changedFiles ).iterator();

        ScmFile file = (ScmFile) files.next();

        assertPath( "/src/main/java/org/Foo.java", file.getPath() );

        assertEquals( ScmFileStatus.ADDED, file.getStatus() );

        file = (ScmFile) files.next();

        assertPath( "/pom.xml", file.getPath() );

        assertEquals( ScmFileStatus.MODIFIED, file.getStatus() );

        assertFile( getUpdatingCopy(), "/readme.txt" );

        assertFalse( "project.xml created incorrectly", new File( getUpdatingCopy(), "/project.xml" ).exists() );
    }

    // ----------------------------------------------------------------------
    // Assertions
    // ----------------------------------------------------------------------

    private void assertFile( File root, String fileName )
        throws Exception
    {
        File file = new File( root, fileName );

        assertTrue( "Missing file: '" + file.getAbsolutePath() + "'.", file.exists() );

        assertTrue( "File isn't a file: '" + file.getAbsolutePath() + "'.", file.isFile() );

        String expected = fileName;

        String actual = FileUtils.fileRead( file );

        assertEquals( "The file doesn't contain the expected contents. File: " + file.getAbsolutePath(), expected,
                      actual );
    }
}
