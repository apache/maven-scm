package org.apache.maven.scm.tck.command.status;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

/**
 * This test tests the status command.
 * <p>
 * It works like this:
 * <p>
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
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public abstract class StatusCommandTckTest
    extends ScmTckTestCase
{

    protected void commit( File workingDirectory, ScmRepository repository )
        throws Exception
    {
        CheckInScmResult result = getScmManager().checkIn( repository, new ScmFileSet( workingDirectory ), "No msg" );

        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        List<ScmFile> committedFiles = result.getCheckedInFiles();

        assertEquals( "Expected 2 files in the committed files list " + committedFiles, 2, committedFiles.size() );
    }

    protected boolean commitUpdateCopy()
    {
        return false;
    }


    public void testStatusCommand()
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
         */

        // /readme.txt
        this.edit( getWorkingCopy(), "readme.txt", null, getScmRepository() );
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );

        // /project.xml
        ScmTestCase.makeFile( getWorkingCopy(), "/project.xml", "changed project.xml" );

        addToWorkingTree( getWorkingCopy(), new File( "project.xml" ), getScmRepository() );

        commit( getWorkingCopy(), getScmRepository() );

        // /pom.xml
        this.edit( getUpdatingCopy(), "pom.xml", null, repository );
        ScmTestCase.makeFile( getUpdatingCopy(), "/pom.xml", "changed pom.xml" );

        // /src/test/java/org
        ScmTestCase.makeDirectory( getUpdatingCopy(), "/src/test/java/org" );

        addToWorkingTree( getUpdatingCopy(), new File( "src/test/java/org" ), repository );

        // /src/main/java/org/Foo.java
        ScmTestCase.makeFile( getUpdatingCopy(), "/src/main/java/org/Foo.java" );

        addToWorkingTree( getUpdatingCopy(), new File( "src/main/java/org" ), repository );

        // src/main/java/org/Foo.java
        addToWorkingTree( getUpdatingCopy(), new File( "src/main/java/org/Foo.java" ), repository );

        ScmManager scmManager = getScmManager();

        // ----------------------------------------------------------------------
        // Check status the project
        // src/main/java/org/Foo.java is added
        // /pom.xml is modified
        // check that readme and project.xml are not updated/created
        // ----------------------------------------------------------------------

        StatusScmResult result = scmManager.getProviderByUrl( getScmUrl() )
            .status( repository, new ScmFileSet( getUpdatingCopy() ) );

        if ( this.commitUpdateCopy() )
        {
          //this is needed for perforce so that teardown can remove its client workspace, no harm for cvs/svn/git
            commit( getUpdatingCopy(), repository );
        }

        assertNotNull( "The command returned a null result.", result );

        assertResultIsSuccess( result );

        List<ScmFile> changedFiles = result.getChangedFiles();

        assertEquals( "Expected 2 files in the updated files list " + changedFiles, 2, changedFiles.size() );

        // ----------------------------------------------------------------------
        // Assert the files in the updated files list
        // ----------------------------------------------------------------------

        Iterator<ScmFile> files = new TreeSet<ScmFile>( changedFiles ).iterator();

        ScmFile file = files.next();
        assertPath( "src/main/java/org/Foo.java", file.getPath() );
        assertEquals( ScmFileStatus.ADDED, file.getStatus() );

        file = files.next();
        assertPath( "pom.xml", file.getPath() );
        assertEquals( ScmFileStatus.MODIFIED, file.getStatus() );

        assertFile( getUpdatingCopy(), "/readme.txt" );

        assertFalse( "project.xml created incorrectly", new File( getUpdatingCopy(), "/project.xml" ).exists() );
    }
}
