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
package org.apache.maven.scm.provider.git.jgit.command;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.WindowCacheStats;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JGitUtilsTest {
    @Test
    void openRepoReturnsAnOpenRepositoryForNestedDirectory(@TempDir Path tempDir) throws Exception {
        initializeRepository(tempDir);
        Files.createDirectory(tempDir.resolve("nested"));
        runProbe(tempDir, "nested");
    }

    @Test
    void openRepoAcceptsGitDirectory(@TempDir Path tempDir) throws Exception {
        initializeRepository(tempDir);
        runProbe(tempDir, "git-directory");
    }

    @Test
    void openRepoRejectsMissingRepository(@TempDir Path tempDir) throws Exception {
        runProbe(tempDir, "missing");
        assertFalse(Files.exists(tempDir.resolve(".git")));
    }

    @Test
    void openRepoPreservesWorkTreeEnvironment(@TempDir Path tempDir) throws Exception {
        initializeRepository(tempDir);
        Path workTree = Files.createDirectory(tempDir.resolve("alternate"));
        Files.write(workTree.resolve("tracked.txt"), "changed\n".getBytes(StandardCharsets.UTF_8));
        runProbe(tempDir, "work-tree");
    }

    @Test
    void openRepoPreservesIndexEnvironment(@TempDir Path tempDir) throws Exception {
        initializeRepository(tempDir);
        Files.copy(tempDir.resolve(".git/index"), tempDir.resolve("alternate.index"));
        Files.write(tempDir.resolve("tracked.txt"), "changed\n".getBytes(StandardCharsets.UTF_8));
        runProbe(tempDir, "index");
        try (Git git = Git.open(tempDir.toFile())) {
            assertTrue(git.status().call().getModified().contains("tracked.txt"));
            assertTrue(git.status().call().getChanged().isEmpty(), "the default index must not be changed");
        }
    }

    @Test
    void closingGitClosesRepositoryPackFiles(@TempDir Path tempDir) throws Exception {
        initializeRepository(tempDir);
        try (Git git = Git.open(tempDir.toFile())) {
            git.gc().call();
        }
        runProbe(tempDir, "close");
    }

    private static void initializeRepository(Path directory) throws Exception {
        try (Git git = Git.init().setDirectory(directory.toFile()).call()) {
            Files.write(directory.resolve("tracked.txt"), "initial\n".getBytes(StandardCharsets.UTF_8));
            git.add().addFilepattern("tracked.txt").call();
            git.commit()
                    .setMessage("initial")
                    .setAuthor("SCM Test", "scm@example.invalid")
                    .setCommitter("SCM Test", "scm@example.invalid")
                    .call();
        }
    }

    private static void runProbe(Path directory, String mode) throws Exception {
        String java = new File(System.getProperty("java.home"), "bin/java").getAbsolutePath();
        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        Path output = directory.resolve("probe-" + mode + ".log");
        ProcessBuilder builder =
                new ProcessBuilder(java, "-cp", classPath, RepositoryProbe.class.getName(), directory.toString(), mode);
        Map<String, String> environment = builder.environment();
        environment.keySet().removeIf(name -> name.startsWith("GIT_"));
        environment.put("GIT_CEILING_DIRECTORIES", directory.getParent().toString());
        if (mode.equals("work-tree")) {
            environment.put("GIT_DIR", directory.resolve(".git").toString());
            environment.put("GIT_WORK_TREE", directory.resolve("alternate").toString());
        } else if (mode.equals("index")) {
            environment.put(
                    "GIT_INDEX_FILE", directory.resolve("alternate.index").toString());
        }
        Process process = builder.redirectErrorStream(true)
                .redirectOutput(output.toFile())
                .start();
        try {
            if (!process.waitFor(30, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
                throw new AssertionError("Repository probe timed out: " + mode);
            }
            assertEquals(0, process.exitValue(), new String(Files.readAllBytes(output), StandardCharsets.UTF_8));
        } catch (InterruptedException e) {
            process.destroyForcibly();
            try {
                process.waitFor(5, TimeUnit.SECONDS);
            } catch (InterruptedException cleanupInterrupted) {
                e.addSuppressed(cleanupInterrupted);
            }
            Thread.currentThread().interrupt();
            throw e;
        }
    }

    /** Exercises environment-dependent behavior without changing the test JVM's global environment. */
    public static class RepositoryProbe {
        public static void main(String[] args) throws Exception {
            Path directory = new File(args[0]).toPath();
            String mode = args[1];
            if (mode.equals("missing")) {
                try (Git ignored = JGitUtils.openRepo(directory.toFile())) {
                    throw new AssertionError("A missing repository must not be opened");
                } catch (IllegalArgumentException expected) {
                    return;
                }
            }
            File basedir = directory.toFile();
            if (mode.equals("nested")) {
                basedir = directory.resolve("nested").toFile();
            } else if (mode.equals("git-directory")) {
                basedir = directory.resolve(".git").toFile();
            } else if (mode.equals("work-tree")) {
                basedir = directory.resolve("alternate").toFile();
            }
            long initialOpenFiles = WindowCacheStats.getStats().getOpenFileCount();
            try (Git git = JGitUtils.openRepo(basedir)) {
                Repository repository = git.getRepository();
                assertEquals(
                        directory.resolve(".git").toFile().getCanonicalFile(),
                        repository.getDirectory().getCanonicalFile());
                assertTrue(repository.getObjectDatabase().exists());
                if (mode.equals("work-tree")) {
                    assertEquals(
                            directory.resolve("alternate").toFile().getCanonicalFile(),
                            repository.getWorkTree().getCanonicalFile());
                    assertTrue(git.status().call().getModified().contains("tracked.txt"));
                } else {
                    assertEquals(
                            directory.toFile().getCanonicalFile(),
                            repository.getWorkTree().getCanonicalFile());
                }
                if (mode.equals("index")) {
                    assertEquals(
                            directory.resolve("alternate.index").toFile().getCanonicalFile(),
                            repository.getIndexFile().getCanonicalFile());
                    git.add().addFilepattern("tracked.txt").call();
                    assertTrue(git.status().call().getChanged().contains("tracked.txt"));
                    assertTrue(git.status().call().getModified().isEmpty());
                }
                if (mode.equals("close")) {
                    repository.open(repository.resolve("HEAD")).getBytes();
                    assertTrue(WindowCacheStats.getStats().getOpenFileCount() > initialOpenFiles);
                }
            }
            if (mode.equals("close")) {
                assertEquals(initialOpenFiles, WindowCacheStats.getStats().getOpenFileCount());
            }
        }
    }
}
