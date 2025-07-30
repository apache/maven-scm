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
package org.apache.maven.scm.provider.git.command.untag;

import java.io.File;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.GitSshServer;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.untag.UntagCommandTckTest;
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 */
public abstract class GitSshUntagCommandTckTest extends UntagCommandTckTest {
    protected final GitSshServer gitSshServer;

    @Rule
    public TemporaryFolder tmpDirectory = new TemporaryFolder();

    protected GitSshUntagCommandTckTest() throws GeneralSecurityException {
        gitSshServer = new GitSshServer();
    }

    protected abstract String getScmProvider();

    /** {@inheritDoc} */
    public String getScmUrl() throws Exception {
        return "scm:" + getScmProvider() + ":ssh://localhost:" + gitSshServer.getPort() + "/repository";
    }

    protected void configureCredentials(ScmRepository repository, String passphrase) throws Exception {
        ScmProviderRepositoryWithHost providerRepository =
                ScmProviderRepositoryWithHost.class.cast(repository.getProviderRepository());
        // store as file
        Path privateKeyFile = tmpDirectory.newFile().toPath();
        gitSshServer.writePrivateKeyAsPkcs8(privateKeyFile, passphrase);
        providerRepository.setPrivateKey(privateKeyFile.toString());
        providerRepository.setPassphrase(passphrase); // may be null
    }

    /** {@inheritDoc} */
    public void initRepo() throws Exception {
        GitScmTestUtils.initRepo("src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory());
        gitSshServer.start(getRepositoryRoot().getParentFile().toPath());

        // as checkout also already happens in setup() make sure to configure credentials here as well
        configureCredentials(getScmRepository(), null);
    }

    @Override
    public void removeRepo() throws Exception {
        gitSshServer.stop();
        super.removeRepo();
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
    public void testUntagCommandTestWithPush() throws Exception {
        configureCredentials(getScmRepository(), null);
        getScmRepository().getProviderRepository().setPushChanges(true);
        super.testUntagCommandTest();
    }

    @Test
    public void testUntagCommandWithPassphraseAndPushTest() throws Exception {
        // TODO: currently no easy way to pass passphrase in gitexe
        Assume.assumeTrue(
                "Ignore test with passphrase for provider " + getScmProvider(), "jgit".equals(getScmProvider()));
        configureCredentials(getScmRepository(), "mySecret");
        getScmRepository().getProviderRepository().setPushChanges(true);
        super.testUntagCommandTest();
    }
}
