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
package org.apache.maven.scm.provider.git.gitexe.command.info;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.apache.maven.scm.provider.git.GitScmTestUtils.GIT_COMMAND_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Olivier Lamy
 */
public class GitInfoCommandTest extends ScmTestCase {

    @Test
    void shallowRepositoryOutputIsRecognized() {
        assertTrue(GitInfoCommand.isShallowRepository("true\n"));
        assertFalse(GitInfoCommand.isShallowRepository("false\n"));
        assertFalse(GitInfoCommand.isShallowRepository(""));
    }

    @Test
    void detectsShallowAndFullClones(@TempDir Path temp) throws Exception {
        checkSystemCmdPresence(GIT_COMMAND_LINE);

        Path source = Files.createDirectory(temp.resolve("source"));
        git(source, "init");
        for (String value : new String[] {"one", "two"}) {
            Files.write(source.resolve("file.txt"), value.getBytes("UTF-8"));
            git(source, "add", "file.txt");
            git(source, "-c", "user.name=Test", "-c", "user.email=test@example.invalid", "commit", "-m", value);
        }
        // depth is ignored for plain paths, so clone through a file:// URL
        Path shallow = temp.resolve("shallow");
        Path full = temp.resolve("full");
        git(temp, "clone", "--depth", "1", source.toUri().toString(), shallow.toString());
        git(temp, "clone", source.toUri().toString(), full.toString());

        assertTrue(GitInfoCommand.detectShallowRepository(shallow.toFile()));
        assertFalse(GitInfoCommand.detectShallowRepository(full.toFile()));
    }

    private static void git(Path directory, String... arguments) throws Exception {
        List<String> command = new ArrayList<>();
        command.add("git");
        command.addAll(Arrays.asList(arguments));
        Path output = Files.createTempFile(directory, "git-output", ".log");
        Process process = new ProcessBuilder(command)
                .directory(directory.toFile())
                .redirectErrorStream(true)
                .redirectOutput(output.toFile())
                .start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "git timed out");
            assertEquals(0, process.exitValue(), new String(Files.readAllBytes(output), "UTF-8"));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
            Files.deleteIfExists(output);
        }
    }

    @Test
    void testInfoCommand() throws Exception {
        checkSystemCmdPresence(GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/git/info", getRepositoryRoot(), getWorkingCopy());

        ScmProvider provider = getScmManager().getProviderByUrl(getScmUrl());
        ScmProviderRepository repository = provider.makeProviderScmRepository(getRepositoryRoot());
        assertNotNull(repository);
        InfoScmResult result = provider.info(repository, new ScmFileSet(getRepositoryRoot()), new CommandParameters());
        assertNotNull(result);
        assertEquals(
                "cd3c0dfacb65955e6fbb35c56cc5b1bf8ce4f767",
                result.getInfoItems().get(0).getRevision());
        //
    }

    @Test
    void testInfoCommandWithShortRevision() throws Exception {
        checkSystemCmdPresence(GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/git/info", getRepositoryRoot(), getWorkingCopy());

        ScmProvider provider = getScmManager().getProviderByUrl(getScmUrl());
        ScmProviderRepository repository = provider.makeProviderScmRepository(getRepositoryRoot());
        assertNotNull(repository);
        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setInt(CommandParameter.SCM_SHORT_REVISION_LENGTH, 6);
        InfoScmResult result = provider.info(repository, new ScmFileSet(getRepositoryRoot()), commandParameters);
        assertNotNull(result);
        assertEquals("cd3c0d", result.getInfoItems().get(0).getRevision(), "revision must be short, exactly 6 digits ");
    }

    @Test
    void testInfoCommandWithNegativeShortRevision() throws Exception {
        checkSystemCmdPresence(GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/git/info", getRepositoryRoot(), getWorkingCopy());

        ScmProvider provider = getScmManager().getProviderByUrl(getScmUrl());
        ScmProviderRepository repository = provider.makeProviderScmRepository(getRepositoryRoot());
        assertNotNull(repository);
        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setInt(CommandParameter.SCM_SHORT_REVISION_LENGTH, GitInfoCommand.NO_REVISION_LENGTH);
        InfoScmResult result = provider.info(repository, new ScmFileSet(getRepositoryRoot()), commandParameters);
        assertNotNull(result);
        assertEquals(
                "cd3c0dfacb65955e6fbb35c56cc5b1bf8ce4f767",
                result.getInfoItems().get(0).getRevision(),
                "revision should not be short");
    }

    @Test
    void testInfoCommandWithZeroShortRevision() throws Exception {
        checkSystemCmdPresence(GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/git/info", getRepositoryRoot(), getWorkingCopy());

        ScmProvider provider = getScmManager().getProviderByUrl(getScmUrl());
        ScmProviderRepository repository = provider.makeProviderScmRepository(getRepositoryRoot());
        assertNotNull(repository);
        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setInt(CommandParameter.SCM_SHORT_REVISION_LENGTH, 0);
        InfoScmResult result = provider.info(repository, new ScmFileSet(getRepositoryRoot()), commandParameters);
        assertNotNull(result);
        assertTrue(
                result.getInfoItems().get(0).getRevision().length() >= 4,
                "revision should be not empty, minimum 4 (similar to git help rev-parse --short)");
    }

    protected File getRepositoryRoot() {
        return getTestFile("target/scm-test/repository/git/info");
    }

    public String getScmUrl() throws Exception {
        return GitScmTestUtils.getScmUrl(getRepositoryRoot(), "git");
    }

    protected File getWorkingCopy() {
        return getTestFile("target/scm-test/git/info");
    }
}
