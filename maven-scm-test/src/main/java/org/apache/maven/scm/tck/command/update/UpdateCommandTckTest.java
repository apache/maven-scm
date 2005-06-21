package org.apache.maven.scm.tck.command.update;

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
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.scm.ScmManager;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * This test tests the update command.
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
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class UpdateCommandTckTest
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
        CheckOutScmResult result = getScmManager().checkOut( repository, new ScmFileSet( workingDirectory ), null );
        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );
    }

    private void addToRepository( File workingDirectory, File file, ScmRepository repository )
        throws Exception
    {
        AddScmResult result = getScmManager().add( repository, new ScmFileSet( workingDirectory, file ) );
        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        List addedFiles = result.getAddedFiles();

        assertEquals( "Expected 1 file in the added files list " + addedFiles, 1, addedFiles.size() );
    }

    private void commit( File workingDirectory, ScmRepository repository )
		throws Exception
    {
        CheckInScmResult result = getScmManager().checkIn( repository, new ScmFileSet( workingDirectory ), null, "No msg" );
        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        List committedFiles = result.getCheckedInFiles();

        assertEquals( "Expected 3 files in the committed files list " + committedFiles, 3, committedFiles.size() );
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

    public void testUpdateCommand()
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
         * src/test/resources is untouched (a empty directory is left untouched)
         * src/test/java is untouched (a non empty directory is left untouched)
         * src/test/java/org (a empty directory is added)
         * src/main/java/org/Foo.java (a non empty directory is added)
         */

        // /readme.txt
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );

        // /project.xml
        ScmTestCase.makeFile( getWorkingCopy(), "/project.xml", "changed project.xml" );

        addToRepository( getWorkingCopy(), new File( "project.xml" ), repository );

        // /src/test/java/org
        ScmTestCase.makeDirectory( getWorkingCopy(), "/src/test/java/org" );

        addToRepository( getWorkingCopy(), new File( "src/test/java/org" ), repository );

        // /src/main/java/org/Foo.java
        ScmTestCase.makeFile( getWorkingCopy(), "/src/main/java/org/Foo.java" );

        addToRepository( getWorkingCopy(), new File( "src/main/java/org" ), repository );

        // src/main/java/org/Foo.java
        addToRepository( getWorkingCopy(), new File( "src/main/java/org/Foo.java" ), repository );

        ScmManager scmManager = getScmManager();

        commit( getWorkingCopy(), repository );

        // ----------------------------------------------------------------------
        // Update the project
        // ----------------------------------------------------------------------

        UpdateScmResult result = scmManager.update( repository, new ScmFileSet( getUpdatingCopy() ), null );

        assertNotNull( "The command returned a null result.", result );

        assertResultIsSuccess( result );

        assertNull( "The provider message wasn't null", result.getProviderMessage() );

        assertNull( "The command output wasn't null", result.getCommandOutput() );

        List updatedFiles = result.getUpdatedFiles();
        List changedFiles = result.getChanges();

        assertEquals( "Expected 3 files in the updated files list " + updatedFiles, 3, updatedFiles.size() );

        assertNotNull( "The changed files list is null", changedFiles );

        // ----------------------------------------------------------------------
        // Assert the files in the updated files list
        // ----------------------------------------------------------------------

        Iterator files = new TreeSet( updatedFiles ).iterator();

        ScmFile file = (ScmFile) files.next();

        assertPath( "/src/main/java/org/Foo.java", file.getPath() );

        // Need to accommodate CVS' weirdness. TODO: Should the API hide this somehow?
        //assertEquals( ScmFileStatus.ADDED, file.getStatus() );
        assertTrue( ScmFileStatus.ADDED.equals( file.getStatus() ) || ScmFileStatus.UPDATED.equals( file.getStatus() ) );

        file = (ScmFile) files.next();

        assertPath( "/readme.txt", file.getPath() );

        //assertEquals( ScmFileStatus.UPDATED, file.getStatus() );
        assertTrue( ScmFileStatus.PATCHED.equals( file.getStatus() ) || ScmFileStatus.UPDATED.equals( file.getStatus() ) );

        file = (ScmFile) files.next();

        assertPath( "/project.xml", file.getPath() );

        //assertEquals( ScmFileStatus.ADDED, file.getStatus() );
        assertTrue( ScmFileStatus.ADDED.equals( file.getStatus() ) || ScmFileStatus.UPDATED.equals( file.getStatus() ) );
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

        assertEquals( "The file doesn't contain the expected contents. File: " + file.getAbsolutePath(), expected, actual );
    }
}
