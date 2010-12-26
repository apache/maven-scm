package org.apache.maven.scm.tck.command.update;

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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

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
        CheckInScmResult result = getScmManager().checkIn( repository, new ScmFileSet( workingDirectory ), "No msg" );

        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        List committedFiles = result.getCheckedInFiles();

        assertEquals(
            "Expected 3 files in the committed files list:\n  " + StringUtils.join( committedFiles.iterator(), "\n  " ),
            3, committedFiles.size() );
    }

    public void testUpdateCommand()
        throws Exception
    {
        
        FileUtils.deleteDirectory( getUpdatingCopy() );
        
        assertFalse( getUpdatingCopy().exists() );    
        
        //FileUtils.deleteDirectory( getWorkingCopy() );
        
        //assertFalse( getUpdatingCopy().exists() );
        
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

        Date lastUpdate = new Date( System.currentTimeMillis() - 100000 );

        Thread.sleep( 2000 );

        commit( getWorkingCopy(), repository );

        // ----------------------------------------------------------------------
        // Update the project
        // ----------------------------------------------------------------------
       
        UpdateScmResult result = scmManager.update( repository, new ScmFileSet( getUpdatingCopy() ), lastUpdate );

        assertNotNull( "The command returned a null result.", result );

        assertResultIsSuccess( result );

        List updatedFiles = result.getUpdatedFiles();

        List changedSets = result.getChanges();

        assertEquals( "Expected 3 files in the updated files list " + updatedFiles, 3, updatedFiles.size() );

        assertNotNull( "The changed files list is null", changedSets );

        assertFalse( "The changed files list is empty ", changedSets.isEmpty() );

        for ( Iterator i = changedSets.iterator(); i.hasNext(); )
        {
            ChangeSet changeSet = (ChangeSet) i.next();
            System.out.println( changeSet.toXML() );
        }

        // ----------------------------------------------------------------------
        // Assert the files in the updated files list
        // ----------------------------------------------------------------------

        Iterator files = new TreeSet( updatedFiles ).iterator();

        //Foo.java
        ScmFile file = (ScmFile) files.next();
        assertPath( "/src/main/java/org/Foo.java", file.getPath() );
        //TODO : Consolidate file status so that we can remove "|| ADDED" term
        assertTrue( file.getStatus().isUpdate() || file.getStatus() == ScmFileStatus.ADDED );

        //readme.txt
        file = (ScmFile) files.next();
        assertPath( "/readme.txt", file.getPath() );
        assertTrue( file.getStatus().isUpdate() );

        //project.xml
        file = (ScmFile) files.next();
        assertPath( "/project.xml", file.getPath() );
        //TODO : Consolidate file status so that we can remove "|| ADDED" term
        assertTrue( file.getStatus().isUpdate() || file.getStatus() == ScmFileStatus.ADDED );
    }
    
}
