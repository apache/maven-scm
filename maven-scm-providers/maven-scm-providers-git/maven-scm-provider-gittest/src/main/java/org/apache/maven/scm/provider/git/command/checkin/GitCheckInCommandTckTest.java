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
package org.apache.maven.scm.provider.git.command.checkin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.PlexusJUnit4TestCase;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.util.GitUtil;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.checkin.CheckInCommandTckTest;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 *
 */
public abstract class GitCheckInCommandTckTest extends CheckInCommandTckTest {

    /** {@inheritDoc} */
    public void initRepo() throws Exception {
        GitScmTestUtils.initRepo("src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory());
    }

    @Override
    protected CheckOutScmResult checkOut(File workingDirectory, ScmRepository repository) throws Exception {
        try {
            return super.checkOut(workingDirectory, repository);
        } finally {
            GitScmTestUtils.setDefaultGitConfig(workingDirectory);
        }
    }

    @Test
    public void testUpToDatePush() throws Exception {
        File checkedOutRepo = getWorkingCopy();

        ScmRepository scmRepository = getScmManager().makeScmRepository(getScmUrl());
        checkoutRepoInto(checkedOutRepo, scmRepository);

        // Add a default user to the config
        GitScmTestUtils.setDefaultGitConfig(checkedOutRepo);

        CheckInScmResult result =
                getScmManager().checkIn(scmRepository, new ScmFileSet(checkedOutRepo), "No change commit message");

        assertResultIsSuccess(result);
    }

    @Test
    public void testRejectedNonFastForwardPush() throws Exception {
        File blockingRepo = PlexusJUnit4TestCase.getTestFile("target/scm-test/blocking-repo");
        File rejectedRepo = PlexusJUnit4TestCase.getTestFile("target/scm-test/rejected-repo");

        ScmRepository scmRepository = getScmManager().makeScmRepository(getScmUrl());
        checkoutRepoInto(rejectedRepo, scmRepository);
        checkoutRepoInto(blockingRepo, scmRepository);

        // Add a default user to the config
        GitScmTestUtils.setDefaultGitConfig(rejectedRepo);
        GitScmTestUtils.setDefaultGitConfig(blockingRepo);

        ScmFileSet blockingFileSet = createWorkspaceChange(rejectedRepo);

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString(CommandParameter.MESSAGE, "Blocking commit");

        CheckInScmResult blockingResult = getScmManager().checkIn(scmRepository, blockingFileSet, commandParameters);
        assertResultIsSuccess(blockingResult);

        ScmFileSet rejectedFileSet = createWorkspaceChange(blockingRepo);

        commandParameters = new CommandParameters();
        commandParameters.setString(CommandParameter.MESSAGE, "Rejected commit");

        CheckInScmResult checkInScmResult = getScmManager().checkIn(scmRepository, rejectedFileSet, commandParameters);
        assertFalse(
                "check-in should have been rejected since fast forward was not possible", checkInScmResult.isSuccess());
    }

    @Test
    public void testCommitWithRejectingPreCommitHook() throws Exception {
        GitScmTestUtils.setupRejectAllCommitsPreCommitHook(getWorkingCopy());
        GitScmTestUtils.setDefaultGitConfig(getWorkingCopy());
        ScmFileSet addedFile = createWorkspaceChange(getWorkingCopy());
        try {
            CheckInScmResult result =
                    getScmManager().checkIn(getScmRepository(), addedFile, "Commit with pre-commit hook");
            assertFalse(
                    "check-in should have been rejected since pre-push hook rejects all commits", result.isSuccess());
        } catch (ScmException e) {
            // some providers may use an exception to indicate a failed commit

        }
    }

    @Test
    public void testCommitNoVerify() throws Exception {
        GitScmTestUtils.setupRejectAllCommitsPreCommitHook(getWorkingCopy());
        GitScmTestUtils.setDefaultGitConfig(getWorkingCopy());
        ScmFileSet addedFile = createWorkspaceChange(getWorkingCopy());
        Path gitSettingsFile =
                createTempFileFromClasspathResource("/git-settings-no-verify.xml", GitUtil.GIT_SETTINGS_FILENAME);
        GitUtil.setSettingsDirectory(
                gitSettingsFile.getParent().toFile()); // ensure that the settings are read from the .git directory
        try {
            CheckInScmResult result =
                    getScmManager().checkIn(getScmRepository(), addedFile, "Commit with pre-commit hook");
            assertResultIsSuccess(result);
        } finally {
            GitUtil.setSettingsDirectory(GitUtil.DEFAULT_SETTINGS_DIRECTORY); // reset to default settings directory
            Files.delete(gitSettingsFile); // delete the temporary settings file
            Files.delete(gitSettingsFile.getParent()); // delete the temporary settings directory
        }
    }

    private CheckOutScmResult checkoutRepoInto(File workingCopy, ScmRepository scmRepository) throws Exception {
        FileUtils.deleteDirectory(workingCopy);
        workingCopy.mkdir();

        CheckOutScmResult result = getScmManager().checkOut(scmRepository, new ScmFileSet(workingCopy), null);

        assertResultIsSuccess(result);
        return result;
    }

    private ScmFileSet createWorkspaceChange(File repo) throws IOException {
        File beerFile = new File(repo.getAbsolutePath(), "beer.xml");
        FileUtils.fileWrite(beerFile.getAbsolutePath(), "1 litre");
        return new ScmFileSet(repo, beerFile.getName());
    }

    /**
     * Creates a new file below a new temporary directory and copies the content of a classpath resource into it.
     * The caller is responsible for deleting the temporary directory afterwards.
     * @param resourceName from where to populate the file (relative to {@code clazz})
     * @param fileName the file name to create below
     * @return the newly created file
     * @throws IOException in case of an error creating the file or copying the resource
     */
    private static Path createTempFileFromClasspathResource(String resourceName, String fileName) throws IOException {
        Path tmpDirectory = Files.createTempDirectory("maven-scm-git-test-");
        Path tmpFile = tmpDirectory.resolve(fileName);
        try (InputStream inputStream = GitCheckInCommandTckTest.class.getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourceName);
            }
            Files.copy(inputStream, tmpFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tmpFile;
    }
}
