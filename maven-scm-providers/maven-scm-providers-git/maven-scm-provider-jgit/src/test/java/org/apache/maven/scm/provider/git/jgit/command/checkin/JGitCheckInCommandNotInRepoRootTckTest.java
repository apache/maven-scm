package org.apache.maven.scm.provider.git.jgit.command.checkin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.command.checkin.GitCheckInCommandTckTest;
import org.codehaus.plexus.PlexusTestCase;
import org.eclipse.jgit.util.FileUtils;

/**
 * Test for MRELEASE-875
 * 
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 */
public class JGitCheckInCommandNotInRepoRootTckTest
    extends GitCheckInCommandTckTest
{

    /**
     * {@inheritDoc}
     */
    public String getScmUrl()
        throws Exception
    {
        return GitScmTestUtils.getScmUrl( getRepositoryRoot(), "jgit" );
    }

    @Override
    protected void deleteDirectory( File directory )
        throws IOException
    {
        if ( directory.exists() )
        {
            FileUtils.delete( directory, FileUtils.RECURSIVE | FileUtils.RETRY );
        }
    }

    private File getSubWorkingDir()
    {
        return new File( getWorkingCopy(), "sub1/sub2/" );
    }

    public void testCheckInCommandTest()
        throws Exception
    {
        // Make sure that the correct files was checked out
        File fooJava = new File( getWorkingCopy(), "sub1/sub2/src/main/java/Foo.java" );

        File barJava = new File( getWorkingCopy(), "sub1/sub2/src/main/java/Bar.java" );

        File readmeTxt = new File( getWorkingCopy(), "sub1/sub2/readme.txt" );

        assertFalse( "check Foo.java doesn't yet exist", fooJava.canRead() );

        assertFalse( "check Bar.java doesn't yet exist", barJava.canRead() );

        assertTrue( "check can read readme.txt", readmeTxt.canRead() );

        // Change the files
        createFooJava( fooJava );

        createBarJava( barJava );

        changeReadmeTxt( readmeTxt );

        // call the command on a subdirectory!!!
        AddScmResult addResult =
            getScmManager().add( getScmRepository(),
                                 new ScmFileSet( getSubWorkingDir(), "src/main/java/Foo.java", null ) );

        assertResultIsSuccess( addResult );

        // call the command on a subdirectory!!!
        CheckInScmResult result =
            getScmManager().checkIn( getScmRepository(), new ScmFileSet( getSubWorkingDir() ), "Commit message" );

        assertResultIsSuccess( result );

        List<ScmFile> files = result.getCheckedInFiles();

        assertNotNull( files );

        assertEquals( 2, files.size() );

        Map<String, ScmFile> fileMap = mapFilesByPath( files );
        ScmFile file1 = fileMap.get( "sub1/sub2/src/main/java/Foo.java" );
        assertNotNull( file1 );
        assertEquals( ScmFileStatus.CHECKED_IN, file1.getStatus() );

        ScmFile file2 = fileMap.get( "sub1/sub2/readme.txt" );
        assertNotNull( file2 );
        assertEquals( ScmFileStatus.CHECKED_IN, file2.getStatus() );

        CheckOutScmResult checkoutResult =
            getScmManager().checkOut( getScmRepository(), new ScmFileSet( getAssertionCopy() ) );

        assertResultIsSuccess( checkoutResult );

        fooJava = new File( getAssertionCopy(), "sub1/sub2/src/main/java/Foo.java" );

        barJava = new File( getAssertionCopy(), "sub1/sub2/src/main/java/Bar.java" );

        readmeTxt = new File( getAssertionCopy(), "sub1/sub2/readme.txt" );

        assertTrue( "check can read Foo.java", fooJava.canRead() );

        assertFalse( "check Bar.java doesn't exist", barJava.canRead() );

        assertTrue( "check can read readme.txt", readmeTxt.canRead() );

        assertEquals( "check readme.txt contents", "changed file",
                      org.codehaus.plexus.util.FileUtils.fileRead( readmeTxt ) );
    }

    public void testCheckInCommandPartialFileset()
        throws Exception
    {
        // Make sure that the correct files was checked out
        File fooJava = new File( getWorkingCopy(), "sub1/sub2/src/main/java/Foo.java" );

        File barJava = new File( getWorkingCopy(), "sub1/sub2/src/main/java/Bar.java" );

        File readmeTxt = new File( getWorkingCopy(), "sub1/sub2/readme.txt" );

        assertFalse( "check Foo.java doesn't yet exist", fooJava.canRead() );

        assertFalse( "check Bar.java doesn't yet exist", barJava.canRead() );

        assertTrue( "check can read readme.txt", readmeTxt.canRead() );

        // Change the files
        createFooJava( fooJava );

        createBarJava( barJava );

        changeReadmeTxt( readmeTxt );

        AddScmResult addResult =
            getScmManager().getProviderByUrl( getScmUrl() ).add( getScmRepository(),
                                                                 new ScmFileSet( getSubWorkingDir(),
                                                                                 "src/main/java/Foo.java", null ) );

        assertResultIsSuccess( addResult );

        CheckInScmResult result =
            getScmManager().checkIn( getScmRepository(), new ScmFileSet( getSubWorkingDir(), "**/Foo.java", null ),
                                     "Commit message" );

        assertResultIsSuccess( result );

        List<ScmFile> files = result.getCheckedInFiles();

        assertNotNull( files );

        assertEquals( 1, files.size() );

        ScmFile file1 = files.get( 0 );

        assertEquals( ScmFileStatus.CHECKED_IN, file1.getStatus() );

        assertPath( "/test-repo/check-in/Foo.java", file1.getPath() );

        CheckOutScmResult checkoutResult =
            getScmManager().checkOut( getScmRepository(), new ScmFileSet( getAssertionCopy() ) );

        assertResultIsSuccess( checkoutResult );

        fooJava = new File( getAssertionCopy(), "sub1/sub2/src/main/java/Foo.java" );

        barJava = new File( getAssertionCopy(), "sub1/sub2/src/main/java/Bar.java" );

        readmeTxt = new File( getAssertionCopy(), "sub1/sub2/readme.txt" );

        assertTrue( "check can read Foo.java", fooJava.canRead() );

        assertFalse( "check Bar.java doesn't exist", barJava.canRead() );

        assertTrue( "check can read readme.txt", readmeTxt.canRead() );

        assertEquals( "check readme.txt contents", "/sub1/sub2/readme.txt",
                      org.codehaus.plexus.util.FileUtils.fileRead( readmeTxt ) );
    }

    @Override
    protected List<String> getScmFileNames()
    {
        List<String> scmFileNames = new ArrayList<String>( 4 );
        scmFileNames.add( "/sub1/sub2/pom.xml" );
        scmFileNames.add( "/sub1/sub2/readme.txt" );
        scmFileNames.add( "/sub1/sub2/src/main/java/Application.java" );
        scmFileNames.add( "/sub1/sub2/src/test/java/Test.java" );
        return scmFileNames;
    }

    @Override
    public void initRepo()
        throws Exception
    {
        GitScmTestUtils.initRepo( "src/test/resources/repoWithSubdirs/", getRepositoryRoot(), getWorkingDirectory() );
    }

    /**
     * @return default location of the test read/write repository
     */
    protected File getRepositoryRoot()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/repositoryWithSubdirs" );
    }

    // @Test
    // public void testCheckInWithWorkingdirNotInRepoRoot()
    // throws Exception
    // {
    // System.out.println( "hello" );
    // }
}
