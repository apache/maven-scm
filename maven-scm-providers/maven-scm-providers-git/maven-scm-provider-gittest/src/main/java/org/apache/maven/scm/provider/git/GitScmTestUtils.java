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
package org.apache.maven.scm.provider.git;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.function.Consumer;

import org.codehaus.plexus.testing.PlexusExtension;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public final class GitScmTestUtils {
    /** 'git' command line. */
    public static final String GIT_COMMAND_LINE = "git";

    private GitScmTestUtils() {}

    public static void initRepo(File repository, File workingDirectory, File assertionDirectory) throws IOException {
        initRepo("src/test/repository/", repository, workingDirectory);

        FileUtils.deleteDirectory(assertionDirectory);

        assertTrue(assertionDirectory.mkdirs());
    }

    public static void initRepo(String source, File repository, File workingDirectory) throws IOException {
        // Copy the repository to target
        File src = PlexusExtension.getTestFile(source);

        FileUtils.deleteDirectory(repository);

        assertTrue(repository.mkdirs());

        FileUtils.copyDirectoryStructure(src, repository);

        File dotGitDirectory = new File(src, "dotgit");

        if (dotGitDirectory.exists()) {
            FileUtils.copyDirectoryStructure(dotGitDirectory, new File(repository, ".git"));
        }

        FileUtils.deleteDirectory(workingDirectory);

        assertTrue(workingDirectory.mkdirs());
    }

    public static String getScmUrl(File repositoryRootFile, String provider) throws CommandLineException {
        return "scm:" + provider + ":"
                + repositoryRootFile.toPath().toAbsolutePath().toUri().toASCIIString();
    }

    public static void deleteAllDirectories(File startDirectory, String pattern) throws IOException {
        if (startDirectory.isDirectory()) {
            File[] childs = startDirectory.listFiles();
            for (int i = 0; i < childs.length; i++) {
                File child = childs[i];
                if (child.isDirectory()) {
                    if (child.getName().equals(pattern)) {
                        FileUtils.deleteDirectory(child);
                    } else {
                        deleteAllDirectories(child, pattern);
                    }
                }
            }
        }
    }

    public static void setDefaultGitConfig(File repositoryRootFile) throws IOException {
        setDefaultGitConfig(repositoryRootFile, null);
    }

    public static void setDefaultGitConfig(File repositoryRootFile, Consumer<FileWriter> configWriterCustomizer)
            throws IOException {
        File gitConfigFile = new File(new File(repositoryRootFile, ".git"), "config");

        try (FileWriter fw = new FileWriter(gitConfigFile, true)) {
            fw.append("[user]\n");
            fw.append("\tname = John Doe\n");
            fw.append("\temail = john.doe@nowhere.com\n");

            fw.append("[commit]\n");
            // disable gpg signing for commits and tags by default
            fw.append("\tgpgsign = false\n");

            fw.append("[tag]\n");
            fw.append("\tgpgsign = false\n");

            // disable automatic garbage collection to avoid locking issues during tests
            fw.append("[gc]\n");
            fw.append("\tauto = 0\n");

            if (configWriterCustomizer != null) {
                configWriterCustomizer.accept(fw);
            }
        }
    }

    /**
     * Sets up a pre-receive git hook that rejects all commits for the given repository (server-side).
     * This is useful for testing scenarios where you want to simulate a repository
     * that doesn't allow any new commits.
     * This hook is <a href="https://github.com/eclipse-jgit/jgit/issues/192">not supported by JGit</a>.
     *
     * @param repositoryRootFile the root directory of the git repository
     * @throws IOException if there's an error creating or writing the hook file
     */
    public static void setupRejectAllCommitsPreReceiveHook(File repositoryRootFile) throws IOException {
        setupRejectAllCommitsHook(repositoryRootFile, true, "pre-receive");
    }

    /**
     * Sets up a pre-push git hook that rejects all commits for the given repository (client-side).
     * This is useful for testing scenarios where you want to simulate a repository
     * that doesn't allow any new commits.
     *
     * @param workspaceRoot the root directory of the git working copy
     * @throws IOException if there's an error creating or writing the hook file
     */
    public static void setupRejectAllCommitsPrePushHook(File workspaceRoot) throws IOException {
        setupRejectAllCommitsHook(workspaceRoot, false, "pre-push");
    }

    public static void setupRejectAllCommitsPreCommitHook(File workspaceRoot) throws IOException {
        setupRejectAllCommitsHook(workspaceRoot, false, "pre-commit");
    }

    private static void setupRejectAllCommitsHook(
            File repositoryOrWorkspaceRootFile, boolean isServerSide, String hookName) throws IOException {
        File hooksDir;
        if (!isServerSide) {
            // For client-side hooks, we use the .git/hooks directory
            hooksDir = new File(repositoryOrWorkspaceRootFile, ".git/hooks");
        } else {
            // For server-side hooks, we use the hooks directory directly
            hooksDir = new File(repositoryOrWorkspaceRootFile, "hooks");
        }
        if (!hooksDir.exists() && !hooksDir.mkdirs()) {
            throw new IOException("Failed to create hooks directory: " + hooksDir.getAbsolutePath());
        }

        File preReceiveHook = new File(hooksDir, hookName);
        try (FileWriter fw = new FileWriter(preReceiveHook)) {
            fw.write("#!/bin/sh\n");
            fw.write("# Pre-receive hook that rejects all commits\n");
            fw.write("echo \"Error: This repository is configured to reject all commits\"\n");
            fw.write("exit 1\n");
        }

        // Make the hook executable (on Unix-like systems)
        if (!preReceiveHook.setExecutable(true)) {
            throw new IOException("Could not make pre-receive hook executable at: " + preReceiveHook.getAbsolutePath());
        }
    }
}
