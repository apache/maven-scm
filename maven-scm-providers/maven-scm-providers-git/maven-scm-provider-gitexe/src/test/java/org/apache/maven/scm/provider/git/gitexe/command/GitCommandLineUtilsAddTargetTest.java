package org.apache.maven.scm.provider.git.gitexe.command;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author mfriedenhagen
 */
public class GitCommandLineUtilsAddTargetTest
{
	//SCM-695 we need to test that files exist, so the test must create a test file tree. We will store the usefull file descriptors to avoid create File with hardcoded strings that must deals with OS differences.
	private static final String DIR_NAME__TEST_ROOT = "GitCommandLineUtilsAddTargetTest";
	private static final String DIR_NAME__PARENT_PROJECT = "prj";
	private static final String DIR_NAME__CHILD_PROJECT = "mod1";
	private static final String DIR_NAME__CHILD_PROJECT__PREFIXED_WITH_PARENT_PROJECT_DIR_NAME = "prj-mod1";
	private static final String FILE_NAME__POM = "pom.xml";
	
	private File myParentProject ;
	private File myPomFromParentProject ;
	private File myPomFromChildProjectInSubfolder ;
	private File myPomFromChildProjectInSiblingFolder ;
	private File myPomFromChildProjectInSiblingFolderWithPrefixedName ;
	private File myRootOfTestFiles ;

    /**
     * Test of addTarget method, of class GitCommandLineUtils on Non-Windows
     * systems.
     */
    @Test
    public void testAddTargetNonWindows()
    {
        assumeTrue( !runsOnWindows() );
        final File workingDir = myParentProject;
        final List<File> filesToAdd = Arrays.asList( myPomFromParentProject, myPomFromChildProjectInSubfolder );
        final String expectedArguments = "[add, pom.xml, mod1/pom.xml]";
        check( workingDir, filesToAdd, expectedArguments );
    }

    //SCM-695 test flat projects.
    /**
     * Test of addTarget method, of class GitCommandLineUtils
     * systems, on flat project (parent and submodule are at the same location).
     * @throws IOException if there is a problem that cannot be dealt with.
     */
    @Test
    public void testAddTargetForFlatProject() throws IOException
    {
        final File workingDir = myParentProject ;
        final List<File> filesToAdd = Arrays.asList( myPomFromParentProject, myPomFromChildProjectInSiblingFolder, myPomFromChildProjectInSiblingFolderWithPrefixedName);
        final String expectedArguments = "[add, pom.xml, "+myPomFromChildProjectInSiblingFolder.getCanonicalPath()+", "+myPomFromChildProjectInSiblingFolderWithPrefixedName.getCanonicalPath()+"]";
        check( workingDir, filesToAdd, expectedArguments );
    }

    /**
     * Test of addTarget method, of class GitCommandLineUtils on Windows.
     */
    @Test
    public void testAddTargetWindows()
    {
        assumeTrue( runsOnWindows() );
        final File workingDir = myParentProject;
        // Note that the second file has a lowercase drive letter, see
        // https://jira.codehaus.org/browse/SCM-667
        final List<File> filesToAdd = Arrays.asList( myPomFromParentProject,
            new File( myPomFromChildProjectInSubfolder.getAbsolutePath().toLowerCase() ) );
        final String expectedArguments = "[add, pom.xml, mod1\\pom.xml]";
        check( workingDir, filesToAdd, expectedArguments );
    }

    private void check( final File workingDir, final List<File> filesToAdd, final String expectedArguments )
    {
        final Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDir, "add" );
        GitCommandLineUtils.addTarget( cl, filesToAdd );
        final String arguments = Arrays.toString( cl.getArguments() );
        assertEquals( 1+filesToAdd.size(), cl.getArguments().length );
        assertEquals( expectedArguments, arguments );
    }

    @Test
    public void testPasswordAnonymous()
        throws Exception
    {

        String commandLine = "git push https://user:password@foo.com/git/trunk refs/tags/my-tag-1";

        final Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( new File( "." ), commandLine );

        String[] commandLineArgs = cl.getShellCommandline();

        //
        for ( int i = 0; i < commandLineArgs.length; i++ )
        {
            assertFalse( MessageFormat.format( "The target log message should not contain <{0}> but it contains <{1}>",
                AnonymousCommandLine.PASSWORD_PLACE_HOLDER, commandLineArgs[i] ),
                commandLineArgs[i].contains( AnonymousCommandLine.PASSWORD_PLACE_HOLDER ) );
        }

        final String scmUrlFakeForTest = "https://user:".concat( AnonymousCommandLine.PASSWORD_PLACE_HOLDER ).concat(
            "@foo.com/git/trunk" );

        assertTrue( MessageFormat.format( "The target log message should contain <{0}> but it contains <{1}>",
            scmUrlFakeForTest, cl.toString() ), cl.toString().contains( scmUrlFakeForTest ) );
    }

    private boolean runsOnWindows()
    {
        return Os.isFamily( Os.FAMILY_WINDOWS );
    }
    

    /**
     * Create a temp file tree because we need testing file existence.
     * @throws IOException if there is a problem that cannot be dealt with.
     */
    @Before
    public void createTestFileTree() throws IOException {
    	//This implementation seems overkill but is OS agnostic.
    	final File tempDir = new File(getSystemTempDir());
		myRootOfTestFiles = makeSubdir(tempDir, DIR_NAME__TEST_ROOT);

		myParentProject = makeSubdir(myRootOfTestFiles, DIR_NAME__PARENT_PROJECT) ;
    	myPomFromParentProject = createDummyFile(myParentProject, FILE_NAME__POM);

    	final File projectInsideParentFolder = makeSubdir(myParentProject, DIR_NAME__CHILD_PROJECT);
    	myPomFromChildProjectInSubfolder = createDummyFile(projectInsideParentFolder, FILE_NAME__POM);

    	final File projectInSiblingFolder = makeSubdir(myRootOfTestFiles, DIR_NAME__CHILD_PROJECT);
    	myPomFromChildProjectInSiblingFolder = createDummyFile(projectInSiblingFolder, FILE_NAME__POM);
    	
    	final File projectInPrefixedSiblingFolder = makeSubdir(myRootOfTestFiles, DIR_NAME__CHILD_PROJECT__PREFIXED_WITH_PARENT_PROJECT_DIR_NAME);
    	myPomFromChildProjectInSiblingFolderWithPrefixedName = createDummyFile(projectInPrefixedSiblingFolder, FILE_NAME__POM);
    }
    
    @After
    public void deleteTestFileTree() throws IOException {
    	FileUtils.deleteDirectory(myRootOfTestFiles);
    }

	/**
	 * @param directory
	 * @param fileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private static File createDummyFile(final File directory, String fileName) throws FileNotFoundException, IOException
	{
		File pom = new File(directory,fileName);
    	FileOutputStream out = new FileOutputStream(pom);
    	out.write(0);
    	out.close();
    	return pom ;
	}

	/**
	 * @param parent directory into which the directory will be created.
	 * @param name name of the directory to create.
	 * @return the file descriptor.
	 */
	private static File makeSubdir(final File parent, final String name)
	{
		final File rootDir = new File(parent, name);
    	rootDir.mkdir();
		return rootDir;
	}

	/**
	 * @return
	 */
	private static String getSystemTempDir()
	{
		return System.getProperty("java.io.tmpdir");
	}

}