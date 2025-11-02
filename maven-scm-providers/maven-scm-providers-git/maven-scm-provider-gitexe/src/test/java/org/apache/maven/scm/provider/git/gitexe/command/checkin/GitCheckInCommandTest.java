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
package org.apache.maven.scm.provider.git.gitexe.command.checkin;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.CommandParameters.SignOption;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.gitexe.GpgTestUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.util.GitUtil;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.maven.scm.provider.git.GitScmTestUtils.GIT_COMMAND_LINE;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeNoException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class GitCheckInCommandTest extends ScmTestCase {
    private File messageFile;

    private String messageFileString;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        messageFile = new File("commit-message");

        String path = messageFile.getAbsolutePath();
        if (path.indexOf(' ') >= 0) {
            path = "\"" + path + "\"";
        }
        messageFileString = "-F " + path;
    }

    @Test
    public void testCommandLineWithoutTag() throws Exception {
        if (GitUtil.getSettings().isCommitNoVerify()) {
            testCommandLine(
                    "scm:git:http://foo.com/git/trunk",
                    "git commit --verbose " + messageFileString + " -a" + " --no-verify");
        } else {
            testCommandLine("scm:git:http://foo.com/git/trunk", "git commit --verbose " + messageFileString + " -a");
        }
    }

    @Test
    public void testCommandLineWithUsername() throws Exception {
        if (GitUtil.getSettings().isCommitNoVerify()) {
            testCommandLine(
                    "scm:git:http://anonymous@foo.com/git/trunk",
                    "git commit --verbose " + messageFileString + " -a" + " --no-verify");
        } else {
            testCommandLine(
                    "scm:git:http://anonymous@foo.com/git/trunk", "git commit --verbose " + messageFileString + " -a");
        }
    }

    // Test reproducing SCM-694
    @Test
    public void testCheckinAfterRename() throws Exception {
        File repo = getRepositoryRoot();
        File checkedOutRepo = getWorkingCopy();

        checkSystemCmdPresence(GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory());

        ScmRepository scmRepository = getScmManager()
                .makeScmRepository(
                        "scm:git:" + repo.toPath().toAbsolutePath().toUri().toASCIIString());
        checkoutRepoInto(checkedOutRepo, scmRepository);

        // Add a default user to the config
        GitScmTestUtils.setDefaultGitConfig(checkedOutRepo);

        // Creating foo/bar/wine.xml
        File fooDir = new File(checkedOutRepo.getAbsolutePath(), "foo");
        fooDir.mkdir();
        File barDir = new File(fooDir.getAbsolutePath(), "bar");
        barDir.mkdir();
        File wineFile = new File(barDir.getAbsolutePath(), "wine.xml");
        FileUtils.fileWrite(wineFile.getAbsolutePath(), "Lacoste castle");

        // Adding and commiting file
        AddScmResult addResult =
                getScmManager().add(scmRepository, new ScmFileSet(checkedOutRepo, new File("foo/bar/wine.xml")));
        assertResultIsSuccess(addResult);
        CheckInScmResult checkInScmResult =
                getScmManager().checkIn(scmRepository, new ScmFileSet(checkedOutRepo), "Created wine file");
        assertResultIsSuccess(checkInScmResult);

        // Cloning foo/bar/wine.xml to foo/newbar/wine.xml
        File newBarDir = new File(fooDir.getAbsolutePath(), "newbar");
        newBarDir.mkdir();
        File movedWineFile = new File(newBarDir.getAbsolutePath(), "wine.xml");
        FileUtils.copyFile(wineFile, movedWineFile);

        // Removing old file, adding new file and commiting...
        RemoveScmResult removeResult =
                getScmManager().remove(scmRepository, new ScmFileSet(checkedOutRepo, new File("foo/bar/")), "");
        assertResultIsSuccess(removeResult);
        addResult = getScmManager().add(scmRepository, new ScmFileSet(checkedOutRepo, new File("foo/newbar/wine.xml")));
        assertResultIsSuccess(addResult);
        checkInScmResult = getScmManager()
                .checkIn(scmRepository, new ScmFileSet(checkedOutRepo), "moved wine.xml from foo/bar/ to foo/newbar/");
        assertResultIsSuccess(checkInScmResult);
        assertTrue(
                checkInScmResult.getCheckedInFiles().size() != 0,
                "Renamed file has not been commited!");
    }

    // Test FileSet in configuration
    @Test
    public void testCheckinWithFileSet() throws Exception {
        File repo = getRepositoryRoot();
        File checkedOutRepo = getWorkingCopy();

        checkSystemCmdPresence(GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory());

        ScmRepository scmRepository = getScmManager()
                .makeScmRepository(
                        "scm:git:" + repo.toPath().toAbsolutePath().toUri().toASCIIString());
        checkoutRepoInto(checkedOutRepo, scmRepository);

        // Add a default user to the config
        GitScmTestUtils.setDefaultGitConfig(checkedOutRepo);

        // Creating beer.xml and whiskey.xml
        File beerFile = new File(checkedOutRepo.getAbsolutePath(), "beer.xml");
        FileUtils.fileWrite(beerFile.getAbsolutePath(), "1/2 litre");
        File whiskeyFile = new File(checkedOutRepo.getAbsolutePath(), "whiskey.xml");
        FileUtils.fileWrite(whiskeyFile.getAbsolutePath(), "700 ml");

        // Adding and commiting beer and whiskey
        AddScmResult addResult =
                getScmManager().add(scmRepository, new ScmFileSet(checkedOutRepo, "beer.xml,whiskey.xml"));
        assertResultIsSuccess(addResult);
        CheckInScmResult checkInScmResult = getScmManager()
                .checkIn(scmRepository, new ScmFileSet(checkedOutRepo, "beer.xml,whiskey.xml"), "Created beer file");
        assertResultIsSuccess(checkInScmResult);

        // Editing beer and commiting whiskey, should commit nothingi, but succeed
        FileUtils.fileWrite(beerFile.getAbsolutePath(), "1 litre");

        addResult = getScmManager().add(scmRepository, new ScmFileSet(checkedOutRepo, "whiskey.xml"));
        assertResultIsSuccess(addResult);
        checkInScmResult = getScmManager()
                .checkIn(scmRepository, new ScmFileSet(checkedOutRepo, "whiskey.xml"), "Checking beer file");
        assertResultIsSuccess(checkInScmResult);
    }

    @Test
    public void testSignedCheckin() throws Exception {
        checkSystemCmdPresence(GIT_COMMAND_LINE);
        checkSystemCmdPresence(GpgTestUtils.BINARY_NAME);

        File repo = getRepositoryRoot();
        File checkedOutRepo = getWorkingCopy();

        GitScmTestUtils.initRepo("src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory());

        ScmRepository scmRepository = getScmManager()
                .makeScmRepository(
                        "scm:git:" + repo.toPath().toAbsolutePath().toUri().toASCIIString());
        assertResultIsSuccess(checkoutRepoInto(checkedOutRepo, scmRepository));

        // use the GPG key of John Doe for signing
        GitScmTestUtils.setDefaultGitConfig(checkedOutRepo, fw -> {
            try {
                fw.append("[user]\n");
                // Windows GPG dpesn't properly support fingerprint, so we use long ID
                fw.append("\tsigningKey = " + GpgTestUtils.JOHN_DOE_KEY_LONG_ID + "\n");
            } catch (IOException e) {
                throw new UncheckedIOException("Error writing to git config file", e);
            }
        });

        try {
            GpgTestUtils.importKey(GpgTestUtils.JOHN_DOE_SECRET_KEY_RESOURCE_NAME);
        } catch (Exception e) {
            assumeNoException("GPG key import failed, skipping test: " + e.getMessage(), e);
        }
        try {
            // Creating beer.xml
            File beerFile = new File(checkedOutRepo.getAbsolutePath(), "beer.xml");
            FileUtils.fileWrite(beerFile.getAbsolutePath(), "1/2 litre");

            CommandParameters parameters = new CommandParameters();
            parameters.setSignOption(CommandParameter.SIGN_OPTION, SignOption.FORCE_SIGN);
            parameters.setString(CommandParameter.MESSAGE, "Created beer file");
            CheckInScmResult checkInScmResult =
                    getScmManager().checkIn(scmRepository, new ScmFileSet(checkedOutRepo, "beer.xml"), parameters);
            assertResultIsSuccess(checkInScmResult);
        } finally {
            // Clean up GPG key after test
            GpgTestUtils.deleteSecretKey(GpgTestUtils.JOHN_DOE_KEY_FINGERPRINT);
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private CheckOutScmResult checkoutRepoInto(File workingCopy, ScmRepository scmRepository) throws Exception {
        FileUtils.deleteDirectory(workingCopy);
        workingCopy.mkdir();

        CheckOutScmResult result =
                getScmManager().checkOut(scmRepository, new ScmFileSet(workingCopy), (ScmVersion) null);

        assertResultIsSuccess(result);
        return result;
    }

    private void testCommandLine(String scmUrl, String commandLine) throws Exception {
        File workingDirectory = getTestFile("target/git-checkin-command-test");

        ScmRepository repository = getScmManager().makeScmRepository(scmUrl);

        GitScmProviderRepository gitRepository = (GitScmProviderRepository) repository.getProviderRepository();

        Commandline cl =
                GitCheckInCommand.createCommitCommandLine(gitRepository, new ScmFileSet(workingDirectory), messageFile);

        assertCommandLine(commandLine, workingDirectory, cl);
    }
}
