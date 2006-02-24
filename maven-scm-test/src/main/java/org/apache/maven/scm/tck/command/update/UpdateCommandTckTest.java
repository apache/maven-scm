package org.apache.maven.scm.tck.command.update;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * This test tests the update command.
 * <p/>
 * It works like this:
 * <p/>
 * <ol>
 * <li>Check out the files to directory getWorkingCopy().
 * <li>Check out the files to directory getUpdatingCopy().
 * <li>Change the files in getWorkingCopy().
 * <li>Commit the files in getWorkingCopy(). Note that the provider <b>must</b> not
 * use the check in command as it can be guaranteed to work as it's not yet tested.
 * <li>Use the update command in getUpdatingCopy() to assert that the files
 * that was supposed to be updated actually was updated.
 * </ol>
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class UpdateCommandTckTest
    extends ScmTckTestCase
{

    private void commit( File workingDirectory, ScmRepository repository )
        throws Exception
    {
        CheckInScmResult result = getScmManager().getProviderByUrl( getScmUrl() )
            .checkIn( repository, new ScmFileSet( workingDirectory ), null, "No msg" );

        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        List committedFiles = result.getCheckedInFiles();

        assertEquals( "Expected 3 files in the committed files list " + committedFiles, 3, committedFiles.size() );
    }

    public void testUpdateCommand()
        throws Exception
    {
        ScmRepository repository = makeScmRepository( getScmUrl() );

        checkOut( getUpdatingCopy(), repository );

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

        addToWorkingTree( getWorkingCopy(), new File( "project.xml" ), repository );

        // /src/test/java/org
        ScmTestCase.makeDirectory( getWorkingCopy(), "/src/test/java/org" );

        addToWorkingTree( getWorkingCopy(), new File( "src/test/java/org" ), repository );

        // /src/main/java/org/Foo.java
        ScmTestCase.makeFile( getWorkingCopy(), "/src/main/java/org/Foo.java" );

        addToWorkingTree( getWorkingCopy(), new File( "src/main/java/org" ), repository );

        // src/main/java/org/Foo.java
        addToWorkingTree( getWorkingCopy(), new File( "src/main/java/org/Foo.java" ), repository );

        ScmManager scmManager = getScmManager();

        commit( getWorkingCopy(), repository );

        // ----------------------------------------------------------------------
        // Update the project
        // ----------------------------------------------------------------------

        UpdateScmResult result = scmManager.getProviderByUrl( getScmUrl() )
            .update( repository, new ScmFileSet( getUpdatingCopy() ), null );

        assertNotNull( "The command returned a null result.", result );

        assertResultIsSuccess( result );

        assertNull( "The provider message wasn't null", result.getProviderMessage() );

        assertNull( "The command output wasn't null", result.getCommandOutput() );

        List updatedFiles = result.getUpdatedFiles();

        List changedFiles = result.getChanges();

        assertEquals( "Expected 3 files in the updated files list " + updatedFiles, 3, updatedFiles.size() );

        assertNotNull( "The changed files list is null", changedFiles );

        assertFalse( "The changed files list is empty", changedFiles.isEmpty() );

        // ----------------------------------------------------------------------
        // Assert the files in the updated files list
        // ----------------------------------------------------------------------

        Iterator files = new TreeSet( updatedFiles ).iterator();

        ScmFile file = (ScmFile) files.next();

        assertPath( "/src/main/java/org/Foo.java", file.getPath() );

        // Need to accommodate CVS' weirdness. TODO: Should the API hide this somehow?
        //assertEquals( ScmFileStatus.ADDED, file.getStatus() );
        assertTrue(
            ScmFileStatus.ADDED.equals( file.getStatus() ) || ScmFileStatus.UPDATED == file.getStatus() );

        file = (ScmFile) files.next();

        assertPath( "/readme.txt", file.getPath() );

        //assertEquals( ScmFileStatus.UPDATED, file.getStatus() );
        assertTrue(
            ScmFileStatus.PATCHED.equals( file.getStatus() ) || ScmFileStatus.UPDATED.equals( file.getStatus() ) );

        file = (ScmFile) files.next();

        assertPath( "/project.xml", file.getPath() );

        //assertEquals( ScmFileStatus.ADDED, file.getStatus() );
        assertTrue(
            ScmFileStatus.ADDED.equals( file.getStatus() ) || ScmFileStatus.UPDATED.equals( file.getStatus() ) );
    }
}
