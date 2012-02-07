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

        //We should have one log entry for the initial repository
        ChangeLogScmResult result =
            provider.changeLog( getScmRepository(), fileSet, null, null, 0, (ScmBranch) null, null );
        assertTrue( result.getProviderMessage(), result.isSuccess() );
        assertEquals( 1, result.getChangeLog().getChangeSets().size() );

        //Make a timestamp that we know are after initial revision but before the second
        Date timeBeforeSecond = new Date(); //Current time

        // pause a couple seconds... [SCM-244]
        Thread.sleep( 2000 );

        //Make a change to the readme.txt and commit the change
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );
        CheckInScmResult checkInResult = provider.checkIn( getScmRepository(), fileSet, COMMIT_MSG );
        assertTrue( "Unable to checkin changes to the repository", checkInResult.isSuccess() );

        result = provider.changeLog( getScmRepository(), fileSet, (ScmVersion) null, null );
        assertTrue( result.getProviderMessage(), result.isSuccess() );
        assertEquals( 2, result.getChangeLog().getChangeSets().size() );

        //Now only retrieve the changelog after timeBeforeSecondChangeLog
        Date currentTime = new Date();
        result = provider
            .changeLog( getScmRepository(), fileSet, timeBeforeSecond, currentTime, 0, new ScmBranch( "" ) );

        //Thorough assert of the last result
        assertTrue( result.getProviderMessage(), result.isSuccess() );
        assertEquals( 1, result.getChangeLog().getChangeSets().size() );
        ChangeSet changeset = (ChangeSet) result.getChangeLog().getChangeSets().get( 0 );
        assertTrue( changeset.getDate().after( timeBeforeSecond ) );
        assertEquals( COMMIT_MSG, changeset.getComment() );
    }
}
