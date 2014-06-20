package org.apache.maven.scm.tck.command.changelog;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProvider;

import java.util.Date;

/**
 * Test Changlog command. <br>
 * 1. Get initial log <br>
 * 2. Add one revision <br>
 * 3. Get the two logs <br>
 * 4. Get the last log based on date <br>
 * 5. Test last log for date and comment <br>
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a>
 */
public abstract class ChangeLogCommandTckTest
    extends ScmTckTestCase
{
    private static final String COMMIT_MSG = "Second changelog";

    public void testChangeLogCommand()
        throws Exception
    {
        Thread.sleep( 1000 );
        ScmProvider provider = getScmManager().getProviderByRepository( getScmRepository() );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmResult firstResult =
            provider.changeLog( getScmRepository(), fileSet, null, null, 0, (ScmBranch) null, null );
        assertTrue( firstResult.getProviderMessage() + ": " + firstResult.getCommandLine() + "\n" + firstResult.getCommandOutput(),
                    firstResult.isSuccess() );

        //for svn, cvs, git, the repo get recreated for each test and therefore initial changelog size is 1
        // for SCM like perforce, it is not possible to recreate the repo, therefor the size will be greater then 1
        int firstLogSize = firstResult.getChangeLog().getChangeSets().size();
        assertTrue( "Unexpected initial log size", firstLogSize >= 1 );

        //Make a timestamp that we know are after initial revision but before the second
        Date timeBeforeSecond = new Date(); //Current time

        // pause a couple seconds... [SCM-244]
        Thread.sleep( 2000 );

        //Make a change to the readme.txt and commit the change
        this.edit( getWorkingCopy(), "readme.txt", null, getScmRepository() );
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );
        CheckInScmResult checkInResult = provider.checkIn( getScmRepository(), fileSet, COMMIT_MSG );
        assertTrue( "Unable to checkin changes to the repository", checkInResult.isSuccess() );

        ChangeLogScmResult secondResult = provider.changeLog( getScmRepository(), fileSet, (ScmVersion) null, null );
        assertTrue( secondResult.getProviderMessage(), secondResult.isSuccess() );
        assertEquals( firstLogSize + 1, secondResult.getChangeLog().getChangeSets().size() );

        //Now only retrieve the changelog after timeBeforeSecondChangeLog
        Date currentTime = new Date();
        ChangeLogScmResult thirdResult = provider
            .changeLog( getScmRepository(), fileSet, timeBeforeSecond, currentTime, 0, new ScmBranch( "" ) );

        //Thorough assert of the last result
        assertTrue( thirdResult.getProviderMessage(), thirdResult.isSuccess() );
        assertEquals( 1, thirdResult.getChangeLog().getChangeSets().size() );
        ChangeSet changeset = thirdResult.getChangeLog().getChangeSets().get( 0 );
        assertTrue( changeset.getDate().after( timeBeforeSecond ) );


        assertEquals( COMMIT_MSG, changeset.getComment() );
    }
}
