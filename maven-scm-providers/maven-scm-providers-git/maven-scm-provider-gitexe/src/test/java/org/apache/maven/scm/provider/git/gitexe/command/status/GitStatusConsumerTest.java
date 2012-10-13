package org.apache.maven.scm.provider.git.gitexe.command.status;

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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitStatusConsumerTest
    extends PlexusTestCase
{

    private List<ScmFile> getChangedFiles( File gitlog )
        throws IOException
    {
        GitStatusConsumer consumer = new GitStatusConsumer( new DefaultLog(), null );

        BufferedReader r = new BufferedReader( new FileReader( gitlog ) );

        try
        {
            String line;

            while ( ( line = r.readLine() ) != null )
            {
                consumer.consumeLine( line );
            }
        }
        finally
        {
            IOUtils.closeQuietly( r );
        }

        return consumer.getChangedFiles();
    }

    private List<ScmFile> getChangedFiles( String line, File workingDirectory )
    {
        GitStatusConsumer consumer = new GitStatusConsumer( new DefaultLog(), workingDirectory );

        consumer.consumeLine( line );

        return consumer.getChangedFiles();
    }

    public void testConsumerUntrackedFile()
    {
        List<ScmFile> changedFiles = getChangedFiles( "?? project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );
    }

    public void testConsumerAddedFile()
    {
        List<ScmFile> changedFiles = getChangedFiles( "A  project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
    }

    public void testConsumerAddedAndModifiedFile()
    {
        List<ScmFile> changedFiles = getChangedFiles( "AM project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( ScmFileStatus.ADDED, changedFiles.get( 0 ).getStatus() );
    }

    public void testConsumerAddedFileWithDirectoryAndNoFile()
        throws IOException
    {
        File dir = createTempDirectory();

        List<ScmFile> changedFiles = getChangedFiles( "A  project.xml", dir );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );

        FileUtils.deleteDirectory( dir );
    }

    public void testConsumerAddedFileWithDirectoryAndFile()
        throws IOException
    {
        File dir = createTempDirectory();
        FileUtils.write( new File( dir, "project.xml" ), "data" );

        List<ScmFile> changedFiles = getChangedFiles( "A  project.xml", dir );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );

        FileUtils.deleteDirectory( dir );
    }

    public void testConsumerModifiedFile()
    {
        List<ScmFile> changedFiles = getChangedFiles( "M  project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
    }

    public void testConsumerModifiedFileUnstaged()
    {
        List<ScmFile> changedFiles = getChangedFiles( "M  project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( ScmFileStatus.MODIFIED, changedFiles.get( 0 ).getStatus() );
    }

    public void testConsumerModifiedFileBothStagedAndUnstaged()
    {
        List<ScmFile> changedFiles = getChangedFiles( "MM project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( ScmFileStatus.MODIFIED, changedFiles.get( 0 ).getStatus() );
    }

    public void testConsumerModifiedFileWithDirectoryAndNoFile()
        throws IOException
    {
        File dir = createTempDirectory();

        List<ScmFile> changedFiles = getChangedFiles( "M  project.xml", dir );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );

        FileUtils.deleteDirectory( dir );
    }

    public void testConsumerModifiedFileWithDirectoryAndFile()
        throws IOException
    {
        File dir = createTempDirectory();
        FileUtils.write( new File( dir, "project.xml" ), "data" );

        List<ScmFile> changedFiles = getChangedFiles( "M  project.xml", dir );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );

        FileUtils.deleteDirectory( dir );
    }

    public void testConsumerRemovedFile()
    {
        List<ScmFile> changedFiles = getChangedFiles( "D  Capfile", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
    }

    public void testConsumerRemovedFileUnstaged()
    {
        List<ScmFile> changedFiles = getChangedFiles( "D  Capfile", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( ScmFileStatus.DELETED, changedFiles.get( 0 ).getStatus() );
    }

    public void testConsumerRemovedFileWithDirectoryAndNoFile()
        throws IOException
    {
        File dir = createTempDirectory();

        List<ScmFile> changedFiles = getChangedFiles( "D  Capfile", dir );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        FileUtils.deleteDirectory( dir );
    }

    public void testConsumerRemovedFileWithDirectoryAndFile()
        throws IOException
    {
        File dir = createTempDirectory();
        FileUtils.write( new File( dir, "Capfile" ), "data" );

        List<ScmFile> changedFiles = getChangedFiles( "D  Capfile", dir );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );
        FileUtils.deleteDirectory( dir );
    }

    // Test reproducing SCM-694
    public void testConsumeRenamedFile()
        throws Exception
    {
        File dir = new File( System.getProperty( "java.io.tmpdir" ) );

        File tmpFile = new File( dir, "NewCapFile" );

        FileUtils.write( tmpFile, "data" );

        List<ScmFile> changedFiles = getChangedFiles( "R  OldCapfile -> NewCapFile", dir );

        assertNotNull( changedFiles );
        assertEquals( 2, changedFiles.size() );
        FileUtils.deleteDirectory( dir );
    }

    public void testLog1Consumer()
        throws Exception
    {
        List<ScmFile> changedFiles = getChangedFiles( getTestFile( "/src/test/resources/git/status/gitstatus1.gitlog" ) );

        assertEquals( 2, changedFiles.size() );

        testScmFile( changedFiles.get( 0 ), "project.xml", ScmFileStatus.ADDED );
        testScmFile( changedFiles.get( 1 ), "readme.txt", ScmFileStatus.MODIFIED );
    }

    public void testEmptyLogConsumer()
        throws Exception
    {
        List<ScmFile> changedFiles = getChangedFiles( getTestFile( "/src/test/resources/git/status/gitstatus-empty.gitlog" ) );

        assertEquals( 0, changedFiles.size() );
    }


    public void testLog2Consumer()
        throws Exception
    {
        List<ScmFile> changedFiles = getChangedFiles( getTestFile( "/src/test/resources/git/status/gitstatus2.gitlog" ) );

        assertEquals( 4, changedFiles.size() );

        testScmFile( changedFiles.get( 0 ),
                     "maven-scm-provider-gitexe/src/main/java/org/apache/maven/scm/provider/git/gitexe/command/add/GitAddCommand.java",
                     ScmFileStatus.MODIFIED );
        testScmFile( changedFiles.get( 1 ),
                     "maven-scm-provider-gitexe/src/main/java/org/apache/maven/scm/provider/git/gitexe/command/checkin/GitCheckInCommand.java",
                     ScmFileStatus.MODIFIED );
        testScmFile( changedFiles.get( 2 ),
                     "maven-scm-provider-gitexe/src/main/java/org/apache/maven/scm/provider/git/gitexe/command/checkin/GitCheckInConsumer.java",
                     ScmFileStatus.DELETED );
        testScmFile( changedFiles.get( 3 ),
                     "maven-scm-provider-gitexe/src/main/java/org/apache/maven/scm/provider/git/gitexe/command/status/GitStatusConsumer.java",
                     ScmFileStatus.MODIFIED );
    }


    private void testScmFile( ScmFile fileToTest, String expectedFilePath, ScmFileStatus expectedStatus )
    {
        assertEquals( expectedFilePath, fileToTest.getPath() );
        assertEquals( expectedStatus, fileToTest.getStatus() );
    }

    private File createTempDirectory()
        throws IOException
    {
        File dir = File.createTempFile( "gitexe", "test" );
        dir.delete();
        dir.mkdir();
        return dir;
    }

}
