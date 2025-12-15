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
package org.apache.maven.scm.provider.git.command.checkout;

import java.io.File;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.GitSshServer;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.checkout.CheckOutCommandTckTest;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public abstract class GitSshCheckOutCommandTckTest extends CheckOutCommandTckTest {
    protected final GitSshServer gitSshServer;

    @TempDir
    protected File tmpDirectory;

    protected GitSshCheckOutCommandTckTest() throws GeneralSecurityException {
        gitSshServer = new GitSshServer();
    }

    protected abstract String getScmProvider();

    /**
     * {@inheritDoc}
     */
    public String getScmUrl() throws Exception {
        return "scm:" + getScmProvider() + ":ssh://localhost:" + gitSshServer.getPort() + "/repository";
    }

    protected void configureCredentials(ScmRepository repository, String passphrase) throws Exception {
        ScmProviderRepositoryWithHost providerRepository =
                ScmProviderRepositoryWithHost.class.cast(repository.getProviderRepository());
        // store as file
        Path privateKeyFile = File.createTempFile("junit", null, tmpDirectory).toPath();
        gitSshServer.writePrivateKeyAsPkcs8(privateKeyFile, passphrase);
        providerRepository.setPrivateKey(privateKeyFile.toString());
        providerRepository.setPassphrase(passphrase); // may be null
    }

    /**
     * {@inheritDoc}
     */
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

    @Test
    public void checkOutCommandTest() throws Exception {
        configureCredentials(getScmRepository(), null);
        super.checkOutCommandTest();
    }

    @Test
    public void checkOutCommandWithPassphraseTest() throws Exception {
        // TODO: currently no easy way to pass passphrase in gitexe
        Assumptions.assumeTrue(
                "jgit".equals(getScmProvider()), "Ignore test with passphrase for provider " + getScmProvider());
        configureCredentials(getScmRepository(), "mySecret");
        super.checkOutCommandTest();
    }
}
