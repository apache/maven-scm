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
package org.apache.maven.scm.provider.git.gitexe;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitExeShallowRepositoryTest {
    @TempDir
    Path temp;

    @Test
    void detectsExistingClonesAndLinkedWorktrees() throws Exception {
        Path source = createSource();
        Path shallow = temp.resolve("shallow clone");
        Path full = temp.resolve("full clone");
        git(temp, "clone", "--depth", "1", source.toUri().toString(), shallow.toString());
        git(temp, "clone", source.toUri().toString(), full.toString());
        GitExeScmProvider provider = new GitExeScmProvider();
        assertTrue(provider.makeProviderScmRepository(shallow.toFile()).isShallow());
        assertFalse(provider.makeProviderScmRepository(full.toFile()).isShallow());
        assertFalse(provider.makeProviderScmRepository(source.toUri().toString(), ':')
                .isShallow());
        Path linked = temp.resolve("linked checkout");
        git(shallow, "worktree", "add", "--detach", linked.toString(), "HEAD");
        assertTrue(provider.makeProviderScmRepository(linked.toFile()).isShallow());
        assertTrue(legacy(false).isShallowRepository(linked.toFile()));
        git(shallow, "fetch", "--unshallow");
        assertFalse(provider.makeProviderScmRepository(shallow.toFile()).isShallow());
        assertFalse(provider.makeProviderScmRepository(linked.toFile()).isShallow());
        Path bare = temp.resolve("bare.git");
        git(temp, "clone", "--bare", source.toUri().toString(), bare.toString());
        assertThrows(ScmRepositoryException.class, () -> provider.makeProviderScmRepository(bare.toFile()));
    }

    @Test
    void legacyDetectionUsesResolvedDirectoryAndPreservesEmptyMarker() throws Exception {
        Path source = createSource();
        for (boolean oldGitDir : new boolean[] {false, true}) {
            GitExeScmProvider provider = legacy(oldGitDir);
            assertFalse(provider.isShallowRepository(source.toFile()));
            Files.createFile(source.resolve(".git/shallow"));
            assertTrue(provider.isShallowRepository(source.toFile()));
            assertTrue(new GitExeScmProvider().isShallowRepository(source.toFile()));
            Files.write(source.resolve(".git/shallow"), (git(source, "rev-parse", "HEAD") + "\n").getBytes("UTF-8"));
            assertTrue(provider.isShallowRepository(source.toFile()));
            Files.delete(source.resolve(".git/shallow"));
        }
    }

    @Test
    void malformedAndFailedDetectionDoesNotBecomeFalse() throws Exception {
        Path source = createSource();
        for (String response : new String[] {"", "unexpected"}) {
            GitExeScmProvider provider = new GitExeScmProvider() {
                @Override
                String revParse(File path, String option) {
                    return response;
                }
            };
            assertThrows(ScmException.class, () -> provider.isShallowRepository(source.toFile()));
        }
        assertThrows(ScmException.class, () -> new GitExeScmProvider().isShallowRepository(temp.toFile()));
        Files.createDirectory(source.resolve(".git/shallow"));
        assertThrows(ScmException.class, () -> legacy(false).isShallowRepository(source.toFile()));
        Files.delete(source.resolve(".git/shallow"));
        Path notDirectory = Files.createFile(temp.resolve("not a directory"));
        GitExeScmProvider provider = new GitExeScmProvider() {
            @Override
            String revParse(File path, String option) {
                return "--is-shallow-repository".equals(option) ? option : notDirectory.toString();
            }
        };
        assertThrows(ScmException.class, () -> provider.isShallowRepository(source.toFile()));
    }

    private GitExeScmProvider legacy(boolean oldGitDir) {
        return new GitExeScmProvider() {
            @Override
            String revParse(File path, String option) throws ScmException {
                if ("--is-shallow-repository".equals(option) || (oldGitDir && "--git-common-dir".equals(option))) {
                    return option;
                }
                return super.revParse(path, option);
            }
        };
    }

    private Path createSource() throws Exception {
        Path source = Files.createDirectory(temp.resolve("source repository"));
        git(source, "init");
        for (String value : new String[] {"one", "two"}) {
            Files.write(source.resolve("file.txt"), value.getBytes("UTF-8"));
            git(source, "add", "file.txt");
            git(source, "-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-m", value);
        }
        return source;
    }

    private String git(Path directory, String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(Arrays.asList(arguments));
        Path output = Files.createTempFile(temp, "git-output", ".log");
        Process process = new ProcessBuilder(command)
                .directory(directory.toFile())
                .redirectErrorStream(true)
                .redirectOutput(output.toFile())
                .start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "git timed out");
            String text = new String(Files.readAllBytes(output), "UTF-8");
            assertEquals(0, process.exitValue(), text);
            return text.trim();
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
    }
}
