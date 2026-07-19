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
package org.apache.maven.scm.provider.git.command.info;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.tck.command.info.InfoCommandTckTest;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public abstract class GitInfoCommandTckTest extends InfoCommandTckTest {
    /**
     * {@inheritDoc}
     */
    public void initRepo() throws Exception {
        GitScmTestUtils.initRepo("src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory());
    }

    @Test
    void testInfoCommandSkipsMergeCommitsByDefault() throws Exception {
        MergeScenario scenario = createMergeCommitInWorkingCopy();
        ScmProvider scmProvider = getScmManager().getProviderByUrl(getScmUrl());
        InfoScmResult result = scmProvider.info(getScmRepository().getProviderRepository(), getScmFileSet(), null);
        assertResultIsSuccess(result);
        assertEquals(1, result.getInfoItems().size());
        assertEquals(
                scenario.lastNonMergeCommit.getName(),
                result.getInfoItems().get(0).getRevision(),
                "the most recent non-merge commit must be reported by default");
    }

    @Test
    void testInfoCommandIncludesMergeCommitsOnDemand() throws Exception {
        MergeScenario scenario = createMergeCommitInWorkingCopy();
        ScmProvider scmProvider = getScmManager().getProviderByUrl(getScmUrl());
        CommandParameters parameters = new CommandParameters();
        parameters.setString(CommandParameter.SCM_SKIP_MERGE_COMMITS, Boolean.FALSE.toString());
        InfoScmResult result =
                scmProvider.info(getScmRepository().getProviderRepository(), getScmFileSet(), parameters);
        assertResultIsSuccess(result);
        assertEquals(1, result.getInfoItems().size());
        assertEquals(
                scenario.mergeCommit.getName(),
                result.getInfoItems().get(0).getRevision(),
                "the merge commit at HEAD must be reported when skipMergeCommits=false");
    }

    /**
     * Turns the working copy's {@code HEAD} into a no-fast-forward merge commit (two parents):
     * commits once on a side branch and once on the default branch, then merges the side branch.
     * Distinct commit times make "the most recent non-merge commit" unambiguous.
     */
    private MergeScenario createMergeCommitInWorkingCopy() throws Exception {
        try (Git git = Git.open(getWorkingCopy())) {
            StoredConfig config = git.getRepository().getConfig();
            config.setString("user", null, "name", "Test User");
            config.setString("user", null, "email", "test@example.com");
            config.setBoolean("commit", null, "gpgsign", false);
            config.save();

            Date sideBranchTime = new Date(System.currentTimeMillis() - 120_000L);
            Date defaultBranchTime = new Date(sideBranchTime.getTime() + 60_000L);

            String defaultBranch = git.getRepository().getBranch();
            git.checkout().setCreateBranch(true).setName("side-branch").call();
            commitNewFile(git, "side.txt", sideBranchTime);

            git.checkout().setName(defaultBranch).call();
            RevCommit lastNonMergeCommit = commitNewFile(git, "default.txt", defaultBranchTime);

            MergeResult mergeResult = git.merge()
                    .include(git.getRepository().resolve("side-branch"))
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                    .setMessage("merge side-branch")
                    .call();
            assertTrue(
                    mergeResult.getMergeStatus().isSuccessful(),
                    "merge must succeed but was: " + mergeResult.getMergeStatus());
            ObjectId mergeCommit = mergeResult.getNewHead();
            assertNotNull(mergeCommit, "merge must produce a new HEAD commit");
            return new MergeScenario(lastNonMergeCommit, mergeCommit);
        }
    }

    private RevCommit commitNewFile(Git git, String fileName, Date commitTime) throws Exception {
        File file = new File(git.getRepository().getWorkTree(), fileName);
        Files.write(file.toPath(), ("content of " + fileName).getBytes(StandardCharsets.UTF_8));
        git.add().addFilepattern(fileName).call();
        PersonIdent ident = new PersonIdent("Test User", "test@example.com", commitTime, GMT_TIME_ZONE);
        return git.commit()
                .setMessage("add " + fileName)
                .setAuthor(ident)
                .setCommitter(ident)
                .setSign(false)
                .call();
    }

    private static final class MergeScenario {
        private final RevCommit lastNonMergeCommit;
        private final ObjectId mergeCommit;

        private MergeScenario(RevCommit lastNonMergeCommit, ObjectId mergeCommit) {
            this.lastNonMergeCommit = lastNonMergeCommit;
            this.mergeCommit = mergeCommit;
        }
    }
}
