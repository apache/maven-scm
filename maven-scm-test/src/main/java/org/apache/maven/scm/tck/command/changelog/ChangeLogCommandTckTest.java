/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.tck.command.changelog;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
public abstract class ChangeLogCommandTckTest extends ScmTckTestCase {
    private static final String COMMIT_MSG = "Second changelog";
    private static final String COMMIT_TAG = "v3.14";

    /**
     * In some SCMs (Hg) adding a tag creates an extra commit which offsets the expectations.
     * @return If an extra commit will be present for a tag.
     */
    public boolean isTagAnExtraCommit() {
        return false;
    }

    @Test
    public void testChangeLogCommand() throws Exception {
        Thread.sleep(1000);
        ScmProvider provider = getScmManager().getProviderByRepository(getScmRepository());
        ScmFileSet fileSet = new ScmFileSet(getWorkingCopy());

        ChangeLogScmResult firstResult =
                provider.changeLog(getScmRepository(), fileSet, null, null, 0, (ScmBranch) null, null);
        assertTrue(
                firstResult.getProviderMessage() + ": " + firstResult.getCommandLine() + "\n"
                        + firstResult.getCommandOutput(),
                firstResult.isSuccess());

        // for svn and git the repo get recreated for each test and therefore initial changelog size is 1
        int firstLogSize = firstResult.getChangeLog().getChangeSets().size();
        assertTrue("Unexpected initial log size", firstLogSize >= 1);

        // Make a timestamp that we know are after initial revision but before the second
        Date timeBeforeSecond = new Date(); // Current time

        // pause a couple seconds... [SCM-244]
        Thread.sleep(2000);

        // Make a change to the readme.txt and commit the change
        this.edit(getWorkingCopy(), "readme.txt", null, getScmRepository());
        ScmTestCase.makeFile(getWorkingCopy(), "/readme.txt", "changed readme.txt");
        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString(CommandParameter.MESSAGE, COMMIT_MSG);
        commandParameters.setString(CommandParameter.SCM_COMMIT_SIGN, "false");
        CheckInScmResult checkInResult = provider.checkIn(getScmRepository(), fileSet, commandParameters);
        assertTrue("Unable to checkin changes to the repository", checkInResult.isSuccess());

        ScmTagParameters scmTagParameters = new ScmTagParameters();
        TagScmResult tagResult = provider.tag(getScmRepository(), fileSet, COMMIT_TAG, scmTagParameters);
        assertTrue("Unable to tag the changes in the repository", tagResult.isSuccess());

        ChangeLogScmRequest changeLogScmRequest = new ChangeLogScmRequest(getScmRepository(), fileSet);
        ChangeLogScmResult secondResult = provider.changeLog(changeLogScmRequest);
        assertTrue(secondResult.getProviderMessage(), secondResult.isSuccess());

        List<ChangeSet> changeSets = secondResult.getChangeLog().getChangeSets();

        int expectedChangeSets = firstLogSize + 1;
        boolean lastCommitIsCausedByTagging = false;
        int lastCodeCommitIndex = 0;

        if (isTagAnExtraCommit()) {
            // This is for example Mercurial which creates an extra commit after tagging.
            lastCommitIsCausedByTagging = true;
            expectedChangeSets += 1;
            lastCodeCommitIndex = 1;
        }

        assertEquals(expectedChangeSets, changeSets.size());

        // Check if the tag has been retrieved again
        ChangeSet changeSetWithTag = changeSets.get(lastCodeCommitIndex);
        assertEquals(Collections.singletonList(COMMIT_TAG), changeSetWithTag.getTags());

        // Now only retrieve the changelog after timeBeforeSecondChangeLog
        Date currentTime = new Date();
        changeLogScmRequest = new ChangeLogScmRequest(getScmRepository(), fileSet);
        changeLogScmRequest.setStartDate(timeBeforeSecond);
        changeLogScmRequest.setEndDate(currentTime);
        changeLogScmRequest.setScmBranch(new ScmBranch(""));
        ChangeLogScmResult thirdResult = provider.changeLog(changeLogScmRequest);

        // Thorough assert of the last result
        assertTrue(thirdResult.getProviderMessage(), thirdResult.isSuccess());

        List<ChangeSet> thirdChangeSets = thirdResult.getChangeLog().getChangeSets();
        assertEquals(lastCommitIsCausedByTagging ? 2 : 1, thirdChangeSets.size());
        ChangeSet changeset = thirdChangeSets.get(lastCodeCommitIndex);
        assertTrue(changeset.getDate().after(timeBeforeSecond));
        assertTrue(changeset.getComment().startsWith(COMMIT_MSG));
    }
}
