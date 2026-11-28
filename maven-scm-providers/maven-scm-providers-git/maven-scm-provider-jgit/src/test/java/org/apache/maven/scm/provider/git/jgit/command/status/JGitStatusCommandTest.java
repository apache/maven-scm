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
package org.apache.maven.scm.provider.git.jgit.command.status;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.eclipse.jgit.api.Git;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Status filtering when the file set base directory is a subdirectory of the work tree.
 */
class JGitStatusCommandTest {

    @TempDir
    Path workTree;

    private File basedir;

    @BeforeEach
    void setUp() throws Exception {
        Path sub = Files.createDirectory(workTree.resolve("sub"));
        Files.write(sub.resolve("a.txt"), "a".getBytes(StandardCharsets.UTF_8));
        Files.write(sub.resolve("b.txt"), "b".getBytes(StandardCharsets.UTF_8));
        try (Git git = Git.init().setDirectory(workTree.toFile()).call()) {
            git.add().addFilepattern("sub").call();
            git.commit()
                    .setMessage("initial")
                    .setAuthor("test", "test@example.org")
                    .setCommitter("test", "test@example.org")
                    .setSign(false)
                    .call();
        }
        Files.write(sub.resolve("a.txt"), "a2".getBytes(StandardCharsets.UTF_8));
        Files.write(sub.resolve("b.txt"), "b2".getBytes(StandardCharsets.UTF_8));
        basedir = sub.toFile();
    }

    @Test
    void explicitFileListFiltersStatus() throws Exception {
        assertEquals(Collections.singletonList("sub/a.txt"), changedPaths(new ScmFileSet(basedir, new File("a.txt"))));
        assertEquals(
                Collections.singletonList("sub/b.txt"),
                changedPaths(new ScmFileSet(basedir, Collections.singletonList(new File("b.txt")))));
    }

    @Test
    void includesAndExcludesFilterStatus() throws Exception {
        assertEquals(Collections.singletonList("sub/a.txt"), changedPaths(new ScmFileSet(basedir, "*.txt", "b.txt")));
    }

    @Test
    void absoluteFileListFiltersStatus() throws Exception {
        assertEquals(
                Collections.singletonList("sub/a.txt"),
                changedPaths(new ScmFileSet(basedir, new File(basedir, "a.txt"))));
    }

    @Test
    void untrackedFilesAreUnknownAndIgnoredFilesAreOmitted() throws Exception {
        Files.createDirectories(workTree.resolve("new-directory"));
        Files.write(workTree.resolve("new.txt"), new byte[0]);
        Files.write(workTree.resolve("new-directory/nested.txt"), new byte[0]);
        Files.write(workTree.resolve("ignored.txt"), new byte[0]);
        Files.createDirectories(workTree.resolve(".git/info"));
        Files.write(workTree.resolve(".git/info/exclude"), "ignored.txt\n".getBytes(StandardCharsets.UTF_8));

        StatusScmResult result = new JGitStatusCommand().executeStatusCommand(null, new ScmFileSet(workTree.toFile()));

        assertEquals(
                Arrays.asList(
                        new ScmFile("new-directory/nested.txt", ScmFileStatus.UNKNOWN),
                        new ScmFile("new.txt", ScmFileStatus.UNKNOWN),
                        new ScmFile("sub/a.txt", ScmFileStatus.MODIFIED),
                        new ScmFile("sub/b.txt", ScmFileStatus.MODIFIED)),
                result.getChangedFiles().stream()
                        .sorted(Comparator.comparing(ScmFile::getPath))
                        .collect(Collectors.toList()));
    }

    @Test
    void missingFilesAreDeletedAndRespectExplicitFileList() throws Exception {
        Files.delete(basedir.toPath().resolve("a.txt"));

        StatusScmResult result =
                new JGitStatusCommand().executeStatusCommand(null, new ScmFileSet(basedir, new File("a.txt")));

        assertEquals(
                Collections.singletonList(new ScmFile("sub/a.txt", ScmFileStatus.DELETED)), result.getChangedFiles());
    }

    @Test
    void untrackedFilesRespectExplicitFileList() throws Exception {
        Files.write(basedir.toPath().resolve("new.txt"), new byte[0]);
        Files.write(basedir.toPath().resolve("other.txt"), new byte[0]);

        StatusScmResult result =
                new JGitStatusCommand().executeStatusCommand(null, new ScmFileSet(basedir, new File("new.txt")));

        assertEquals(
                Collections.singletonList(new ScmFile("sub/new.txt", ScmFileStatus.UNKNOWN)), result.getChangedFiles());
    }

    @Test
    void stagedDeletionsAreReportedOnce() throws Exception {
        try (Git git = Git.open(workTree.toFile())) {
            git.rm().addFilepattern("sub/a.txt").call();
        }

        StatusScmResult result =
                new JGitStatusCommand().executeStatusCommand(null, new ScmFileSet(basedir, new File("a.txt")));

        assertEquals(
                Collections.singletonList(new ScmFile("sub/a.txt", ScmFileStatus.DELETED)), result.getChangedFiles());
    }

    private List<String> changedPaths(ScmFileSet fileSet) throws Exception {
        StatusScmResult result = new JGitStatusCommand().executeStatusCommand(null, fileSet);
        return result.getChangedFiles().stream().map(f -> f.getPath()).sorted().collect(Collectors.toList());
    }
}
