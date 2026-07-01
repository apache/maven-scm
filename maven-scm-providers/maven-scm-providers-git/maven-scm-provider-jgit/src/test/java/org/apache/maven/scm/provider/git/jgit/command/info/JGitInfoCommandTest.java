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
package org.apache.maven.scm.provider.git.jgit.command.info;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the {@code skipMergeCommits} handling of {@link JGitInfoCommand}.
 * A repository whose {@code HEAD} is a merge commit is built with the JGit API, then the
 * {@code info} command is invoked with and without the {@link CommandParameter#SCM_SKIP_MERGE_COMMITS} flag.
 */
class JGitInfoCommandTest {

    @TempDir
    File workDir;

    @Test
    void includesMergeCommitWhenSkipMergeCommitsIsFalse() throws Exception {
        ObjectId mergeCommit = buildRepositoryWithMergeHead();

        CommandParameters parameters = new CommandParameters();
        parameters.setString(CommandParameter.SCM_SKIP_MERGE_COMMITS, Boolean.FALSE.toString());

        InfoScmResult result = info(parameters);

        assertNotNull(result);
        assertEquals(
                mergeCommit.getName(),
                result.getInfoItems().get(0).getRevision(),
                "HEAD merge commit must be reported when skipMergeCommits=false");
    }

    @Test
    void skipsMergeCommitByDefault() throws Exception {
        ObjectId mergeCommit = buildRepositoryWithMergeHead();

        InfoScmResult result = info(new CommandParameters());

        assertNotNull(result);
        assertNotEquals(
                mergeCommit.getName(),
                result.getInfoItems().get(0).getRevision(),
                "merge commit must be skipped by default, a non-merge commit must be reported");
    }

    private InfoScmResult info(CommandParameters parameters) throws Exception {
        // executeCommand is package-private accessible and ignores the repository argument
        return (InfoScmResult) new JGitInfoCommand().executeCommand(null, new ScmFileSet(workDir), parameters);
    }

    /**
     * Builds a repository whose {@code HEAD} is a no-fast-forward merge commit (two parents).
     *
     * @return the id of the merge commit which is now {@code HEAD}
     */
    private ObjectId buildRepositoryWithMergeHead() throws Exception {
        try (Git git = Git.init().setDirectory(workDir).call()) {
            StoredConfig config = git.getRepository().getConfig();
            config.setString("user", null, "name", "Test User");
            config.setString("user", null, "email", "test@example.com");
            config.setBoolean("commit", null, "gpgsign", false);
            config.save();

            commit(git, "a.txt");
            String mainBranch = git.getRepository().getBranch();

            git.checkout().setCreateBranch(true).setName("feature").call();
            commit(git, "b.txt");

            git.checkout().setName(mainBranch).call();
            commit(git, "c.txt");

            MergeResult mergeResult = git.merge()
                    .include(git.getRepository().resolve("feature"))
                    .setFastForward(MergeCommand.FastForwardMode.NO_FF)
                    .setMessage("merge feature")
                    .call();
            return mergeResult.getNewHead();
        }
    }

    private RevCommit commit(Git git, String fileName) throws Exception {
        File file = new File(git.getRepository().getWorkTree(), fileName);
        Files.write(file.toPath(), ("content of " + fileName).getBytes(StandardCharsets.UTF_8));
        git.add().addFilepattern(fileName).call();
        return git.commit()
                .setMessage("add " + fileName)
                .setAuthor("Test User", "test@example.com")
                .setCommitter("Test User", "test@example.com")
                .setSign(false)
                .call();
    }
}
