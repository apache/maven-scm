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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitStatusConsumerTest
    extends PlexusTestCase
{

    private List<ScmFile> getChangedFiles( File gitlog )
        throws IOException
    {
        return getChangedFiles( gitlog, null );
    }
    
    private List<ScmFile> getChangedFiles( File gitlog, URI relativeRepoPath )
        throws IOException
    {
        GitStatusConsumer consumer = new GitStatusConsumer( new DefaultLog(), null, relativeRepoPath );

        try ( BufferedReader r = new BufferedReader( new FileReader( gitlog ) ) )
        {
            String line;

            while ( ( line = r.readLine() ) != null )
            {
                consumer.consumeLine( line );
            }
        }

        return consumer.getChangedFiles();
    }

    private List<ScmFile> getChangedFiles( String line, File workingDirectory )
    {
        GitStatusConsumer consumer = new GitStatusConsumer( new DefaultLog(), workingDirectory );

        consumer.consumeLine( line );

        return consumer.getChangedFiles();
    }

    private List<ScmFile> getChangedFiles( String line, File workingDirectory, URI relativeRepoPath )
    {
        GitStatusConsumer consumer = new GitStatusConsumer( new DefaultLog(), workingDirectory, relativeRepoPath );

        consumer.consumeLine( line );

        return consumer.getChangedFiles();
    }

    private List<ScmFile> getChangedFiles( String line, File workingDirectory, URI relativeRepoPath,
                                           ScmFileSet scmFileSet )
    {
        GitStatusConsumer consumer =
            new GitStatusConsumer( new DefaultLog(), workingDirectory, relativeRepoPath, scmFileSet );

        consumer.consumeLine( line );

        return consumer.getChangedFiles();
    }

    public void testConsumerUntrackedFile()
    {
        List<ScmFile> changedFiles = getChangedFiles( "?? project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );

        changedFiles = getChangedFiles( "?? \"test file with spaces and a special \\177 character.xml\"", null );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );
    }

    public void testConsumerAddedFile()
    {
        List<ScmFile> changedFiles = getChangedFiles( "A  project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( "project.xml", changedFiles.get( 0 ).getPath() );

        changedFiles = getChangedFiles( "A  \"test file with spaces and a special \\177 character.xml\"", null );
        
        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals("test file with spaces and a special \u007f character.xml", changedFiles.get( 0 ).getPath() );
    }

    public void testConsumerAddedAndModifiedFile()
    {
        List<ScmFile> changedFiles = getChangedFiles( "AM project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        testScmFile( changedFiles.get( 0 ), "project.xml", ScmFileStatus.ADDED );
        
        changedFiles = getChangedFiles( "AM \"test file with spaces and a special \\177 character.xml\"", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        testScmFile( changedFiles.get( 0 ), "test file with spaces and a special \u007f character.xml", ScmFileStatus.ADDED );
    }

    public void testConsumerAddedFileWithDirectoryAndNoFile()
        throws IOException
    {
        File dir = createTempDirectory();

        List<ScmFile> changedFiles = getChangedFiles( "A  project.xml", dir );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );

        changedFiles = getChangedFiles( "A  \"test file with spaces and a special \\177 character.xml\"", dir );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );

        FileUtils.deleteDirectory( dir );
    }

    public void testConsumerAddedFileWithDirectoryAndFile()
        throws IOException
    {
        File dir = createTempDirectory();
        FileUtils.write( new File( dir, "project.xml" ), "data", StandardCharsets.UTF_8 );

        List<ScmFile> changedFiles = getChangedFiles( "A  project.xml", dir );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( "project.xml", changedFiles.get( 0 ).getPath() );

        FileUtils.write( new File( dir, "test file with spaces and a special \u007f character.xml" ),
                         "data", StandardCharsets.UTF_8 );

        changedFiles = getChangedFiles( "A  \"test file with spaces and a special \\177 character.xml\"", dir );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals("test file with spaces and a special \u007f character.xml", changedFiles.get( 0 ).getPath() );

        FileUtils.deleteDirectory( dir );
    }

    public void testConsumerModifiedFile()
    {
        List<ScmFile> changedFiles = getChangedFiles( "M  project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( "project.xml", changedFiles.get( 0 ).getPath() );

        changedFiles = getChangedFiles( "M  \"test file with spaces and a special \\177 character.xml\"", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals("test file with spaces and a special \u007f character.xml", changedFiles.get( 0 ).getPath() );
    }

    public void testURI()
        throws Exception
    {
        String path = "Not%Scheme:/sub dir";
        URI u = GitStatusConsumer.uriFromPath( path );
        assertEquals( path, u.getPath() );
    }

    public void testConsumerWithFileSet()
        throws IOException
    {
        File dir = createTempDirectory();
        FileUtils.write( new File( dir, "project.xml" ), "data", StandardCharsets.UTF_8 );
        FileUtils.write( new File( dir, "pom.xml" ), "more data", StandardCharsets.UTF_8 );
        File subdir = new File( dir.getAbsolutePath(), "subDir" );
        subdir.mkdir();
        FileUtils.write( new File( subdir, "something.xml" ), "data", StandardCharsets.UTF_8 );

        ScmFileSet scmFileSet = new ScmFileSet( dir, null, "project.xml" );
        List<ScmFile> changedFiles = getChangedFiles( "M project.xml", dir, null, scmFileSet );
        assertEquals( 0, changedFiles.size() );

        scmFileSet = new ScmFileSet( dir, "pom.xml" );
        changedFiles = getChangedFiles( "M pom.xml", dir, null, scmFileSet );
        assertEquals( 1, changedFiles.size() );

        scmFileSet = new ScmFileSet( subdir, "something.xml", "pom.xml" );
        changedFiles = getChangedFiles( "M subDir/something.xml", dir, dir.toURI(), scmFileSet );
        assertEquals( 1, changedFiles.size() );
    }

	// SCM-740
	public void testConsumerModifiedFileInComplexDirectorySetup() throws IOException {

		File dir = createTempDirectory();
		URI relativeCWD = URI.create( "" );
		File subdir = new File( dir, "subDirectory" );
		subdir.mkdir();
		FileUtils.write( new File( subdir, "project.xml" ), "data", StandardCharsets.UTF_8 );

		List<ScmFile> changedFiles = getChangedFiles( "M  subDirectory/project.xml", dir, relativeCWD );

		assertNotNull( changedFiles );
		assertEquals( 1, changedFiles.size() );
        assertEquals( "subDirectory/project.xml", changedFiles.get( 0 ).getPath() );

        FileUtils.write( new File( subdir,
                "test file with spaces and a déjà vu character.xml" ), "data", StandardCharsets.UTF_8 );

		changedFiles =
			getChangedFiles( "M  \"subDirectory/test file with spaces and a déjà vu character.xml\"", dir, relativeCWD );

		assertNotNull( changedFiles );
		assertEquals( 1, changedFiles.size() );
        assertEquals( "subDirectory/test file with spaces and a déjà vu character.xml", changedFiles.get( 0 ).getPath() );

        FileUtils.deleteDirectory( dir );
	}

	public void testConsumerModifiedFileInComplexDirectoryWithSpaces() throws IOException {

		File dir = createTempDirectory();
		URI relativeCWD = URI.create( "" );
		File subdir = new File( dir, "sub Directory déjà vu special" );
		subdir.mkdir();
		FileUtils.write( new File( subdir, "project.xml" ), "data", StandardCharsets.UTF_8 );

		List<ScmFile> changedFiles =
			getChangedFiles( "M  \"sub Directory déjà vu special/project.xml\"", dir, relativeCWD );

		assertNotNull( changedFiles );
		assertEquals( 1, changedFiles.size() );
        assertEquals( "sub Directory déjà vu special/project.xml", changedFiles.get( 0 ).getPath() );

        FileUtils.write( new File( subdir, "test file with spaces and a déjà vu character.xml" ),
                "data", StandardCharsets.UTF_8 );

		changedFiles =
			getChangedFiles( "M  \"sub Directory déjà vu special/test file with spaces and a déjà vu character.xml\"",
							dir, relativeCWD );

		assertNotNull( changedFiles );
		assertEquals( 1, changedFiles.size() );
        assertEquals( "sub Directory déjà vu special/test file with spaces and a déjà vu character.xml",
                 changedFiles.get( 0 ).getPath() );

        FileUtils.deleteDirectory( dir );
	}

	public void testConsumerModifiedFileUnstaged()
    {
        List<ScmFile> changedFiles = getChangedFiles( "M  project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        testScmFile( changedFiles.get( 0 ), "project.xml", ScmFileStatus.MODIFIED);

        changedFiles = getChangedFiles( "M  \"test file with spaces and a special \\177 character.xml\"", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        testScmFile( changedFiles.get( 0 ), "test file with spaces and a special \u007f character.xml",
                     ScmFileStatus.MODIFIED );
    }

    public void testConsumerModifiedFileBothStagedAndUnstaged()
    {
        List<ScmFile> changedFiles = getChangedFiles( "MM project.xml", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        testScmFile( changedFiles.get( 0 ), "project.xml", ScmFileStatus.MODIFIED);

        changedFiles = getChangedFiles( "MM \"test file with spaces and a special \\177 character.xml\"", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        testScmFile( changedFiles.get( 0 ), "test file with spaces and a special \u007f character.xml",
                     ScmFileStatus.MODIFIED );
    }

    public void testConsumerModifiedFileWithDirectoryAndNoFile()
        throws IOException
    {
        File dir = createTempDirectory();

        List<ScmFile> changedFiles = getChangedFiles( "M  project.xml", dir );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );

        changedFiles = getChangedFiles( "M  \"test file with spaces and a special \\177 character.xml\"", dir );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );

        FileUtils.deleteDirectory( dir );
    }

    public void testConsumerModifiedFileWithDirectoryAndFile()
        throws IOException
    {
        File dir = createTempDirectory();
        FileUtils.write( new File( dir, "project.xml" ), "data", StandardCharsets.UTF_8 );

        List<ScmFile> changedFiles = getChangedFiles( "M  project.xml", dir );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( "project.xml", changedFiles.get( 0 ).getPath() );

        FileUtils.write( new File( dir, "test file with spaces and a special \u007f character.xml" ), "data",
                         StandardCharsets.UTF_8 );

        changedFiles = getChangedFiles( "M  \"test file with spaces and a special \\177 character.xml\"", dir );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( "test file with spaces and a special \u007f character.xml", changedFiles.get( 0 ).getPath() );

        FileUtils.deleteDirectory( dir );
    }

    public void testConsumerRemovedFile()
    {
        List<ScmFile> changedFiles = getChangedFiles( "D  Capfile", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( "Capfile", changedFiles.get( 0 ).getPath() );

        changedFiles = getChangedFiles( "D  \"test file with spaces and a déjà vu character.xml\"", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( "test file with spaces and a déjà vu character.xml", changedFiles.get( 0 ).getPath() );
    }

    public void testConsumerRemovedFileUnstaged()
    {
        List<ScmFile> changedFiles = getChangedFiles( "D  Capfile", null );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( ScmFileStatus.DELETED, changedFiles.get( 0 ).getStatus() );

        changedFiles = getChangedFiles( "D  \"test file with spaces and a special \\177 character.xml\"", null );

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
        assertEquals( "Capfile", changedFiles.get( 0 ).getPath() );

        changedFiles = getChangedFiles( "D  \"test file with spaces and a special \\177 character.xml\"", dir );

        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
        assertEquals( "test file with spaces and a special \u007f character.xml", changedFiles.get( 0 ).getPath() );
        
        FileUtils.deleteDirectory( dir );
    }

    public void testConsumerRemovedFileWithDirectoryAndFile()
        throws IOException
    {
        File dir = createTempDirectory();
        FileUtils.write( new File( dir, "Capfile" ), "data", StandardCharsets.UTF_8 );

        List<ScmFile> changedFiles = getChangedFiles( "D  Capfile", dir );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );

        FileUtils.write( new File( dir, "test file with spaces and a special \u007f character.xml" ), "data",
                         StandardCharsets.UTF_8 );

        changedFiles = getChangedFiles( "D  \"test file with spaces and a special \\177 character.xml\"", dir );

        assertNotNull( changedFiles );
        assertEquals( 0, changedFiles.size() );
        FileUtils.deleteDirectory( dir );
    }

    // Test reproducing SCM-694
    public void testConsumerRenamedFile()
        throws Exception
    {
        File dir = createTempDirectory();

        File tmpFile = new File( dir, "NewCapFile" );

        FileUtils.write( tmpFile, "data", StandardCharsets.UTF_8 );

        List<ScmFile> changedFiles = getChangedFiles( "R  OldCapfile -> NewCapFile", dir );

        assertNotNull( changedFiles );
        assertEquals( 2, changedFiles.size() );
        assertEquals( "OldCapfile", changedFiles.get(0).getPath() );
        assertEquals( "NewCapFile", changedFiles.get(1).getPath() );

        tmpFile = new File( dir, "New test file with spaces and a special \u007f character.xml" );

        FileUtils.write( tmpFile, "data", StandardCharsets.UTF_8 );

        changedFiles =
            getChangedFiles( "R  \"Old test file with spaces and a special \\177 character.xml\" -> \"New test file with spaces and a special \\177 character.xml\"",
                             dir );

        assertNotNull( changedFiles );
        assertEquals( 2, changedFiles.size() );
        assertEquals( "Old test file with spaces and a special \u007f character.xml", changedFiles.get(0).getPath() );
        assertEquals( "New test file with spaces and a special \u007f character.xml", changedFiles.get(1).getPath() );
        FileUtils.deleteDirectory( dir );
    }

    public void testLog1Consumer()
        throws Exception
    {
        List<ScmFile> changedFiles = getChangedFiles( getTestFile( "/src/test/resources/git/status/gitstatus1.gitlog" ) );

        assertEquals( 4, changedFiles.size() );

        testScmFile( changedFiles.get( 0 ), "project.xml", ScmFileStatus.ADDED );
        testScmFile( changedFiles.get( 1 ), "readme.txt", ScmFileStatus.MODIFIED );
        testScmFile( changedFiles.get( 2 ), "d\u00e9j\u00e0 vu.xml", ScmFileStatus.ADDED );
        testScmFile( changedFiles.get( 3 ), "d\u00e9j\u00e0 vu.txt", ScmFileStatus.MODIFIED );
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

    // SCM-709
    public void testResolvePath()
    {
        File repositoryRoot = getTestFile( "repo" );
        File workingDirectory = getTestFile( "repo/work" );

        URI path = repositoryRoot.toURI().relativize( workingDirectory.toURI() );

        assertEquals( "work", path.getPath() );

        assertEquals( "pom.xml", GitStatusConsumer.resolvePath( "work/pom.xml", path ) );
        assertEquals( "work/pom.xml", GitStatusConsumer.resolvePath( "work/pom.xml", null ) );
        
        // spaces in path
        repositoryRoot = getTestFile( "repo" );
        workingDirectory = getTestFile( "repo/work with spaces" );

        path = repositoryRoot.toURI().relativize( workingDirectory.toURI() );

        assertEquals( "work with spaces", path.getPath() );

        assertEquals( "pom.xml", GitStatusConsumer.resolvePath( "work with spaces/pom.xml", path ) );
        assertEquals( "work with spaces/pom.xml", GitStatusConsumer.resolvePath( "work with spaces/pom.xml", null ) );

        // spaces in path with quotes
        repositoryRoot = getTestFile( "repo" );
        workingDirectory = getTestFile( "repo/work with spaces and quotes" );

        path = repositoryRoot.toURI().relativize( workingDirectory.toURI() );

        assertEquals( "work with spaces and quotes", path.getPath() );

        assertEquals( "pom.xml", GitStatusConsumer.resolvePath( "\"work with spaces and quotes/pom.xml\"", path ) );
        assertEquals( "work with spaces and quotes/pom.xml",
                GitStatusConsumer.resolvePath( "\"work with spaces and quotes/pom.xml\"", null ) );
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
