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
package org.apache.maven.scm.provider.git.jgit;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.StoredConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JGitShallowRepositoryTest {
    @TempDir
    Path temp;

    @Test
    void detectsExistingShallowHistoryAndReset() throws Exception {
        Path directory = temp.resolve("repository with spaces");
        try (Git git = Git.init().setDirectory(directory.toFile()).call()) {
            StoredConfig config = git.getRepository().getConfig();
            config.setString("remote", "origin", "url", directory.toUri().toString());
            config.save();
            git.commit()
                    .setAllowEmpty(true)
                    .setAuthor("Test", "test@example.invalid")
                    .setCommitter("Test", "test@example.invalid")
                    .setMessage("first")
                    .call();
            String head = git.commit()
                    .setAllowEmpty(true)
                    .setAuthor("Test", "test@example.invalid")
                    .setCommitter("Test", "test@example.invalid")
                    .setMessage("second")
                    .call()
                    .name();
            JGitScmProvider provider = new JGitScmProvider(null);
            assertFalse(provider.makeProviderScmRepository(directory.toFile()).isShallow());
            Files.write(directory.resolve(".git/shallow"), (head + "\n").getBytes("UTF-8"));
            assertTrue(provider.makeProviderScmRepository(directory.toFile()).isShallow());
            assertFalse(provider.makeProviderScmRepository(directory.toUri().toString(), ':')
                    .isShallow());
            Files.delete(directory.resolve(".git/shallow"));
            assertFalse(provider.makeProviderScmRepository(directory.toFile()).isShallow());
            // JGit models actual shallow boundary commits; an empty marker has none.
            Files.createFile(directory.resolve(".git/shallow"));
            assertFalse(provider.makeProviderScmRepository(directory.toFile()).isShallow());
            Files.write(directory.resolve(".git/shallow"), "invalid commit\n".getBytes("UTF-8"));
            assertThrows(ScmException.class, () -> provider.isShallowRepository(directory.toFile()));
        }
    }

    @Test
    void preservesInvalidAndBareRepositoryFailures() throws Exception {
        JGitScmProvider provider = new JGitScmProvider(null);
        assertThrows(ScmRepositoryException.class, () -> provider.makeProviderScmRepository(temp.toFile()));
        assertThrows(ScmException.class, () -> provider.isShallowRepository(temp.toFile()));
        Path bare = temp.resolve("bare.git");
        try (Git git = Git.init().setBare(true).setDirectory(bare.toFile()).call()) {
            assertThrows(ScmRepositoryException.class, () -> provider.makeProviderScmRepository(bare.toFile()));
        }
    }
}
