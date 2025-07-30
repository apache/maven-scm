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
package org.apache.maven.scm.provider.git.jgit.command.checkout;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.GeneralSecurityException;
import java.security.PublicKey;

import org.apache.commons.io.IOUtils;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.git.command.checkout.GitSshCheckOutCommandTckTest;
import org.apache.maven.scm.provider.git.jgit.JGitTestScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.sshd.common.config.keys.PublicKeyEntry;
import org.eclipse.jgit.util.FileUtils;
import org.junit.Test;

/** @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a> */
public class JGitSshCheckOutCommandTckTest extends GitSshCheckOutCommandTckTest {

    public JGitSshCheckOutCommandTckTest() throws GeneralSecurityException, IOException {
        super();
    }

    @Override
    protected String getScmProvider() {
        return "jgit";
    }

    @Override
    public void initRepo() throws Exception {
        super.initRepo();
        JGitTestScmProvider provider =
                (JGitTestScmProvider) getScmManager().getProviderByRepository(getScmRepository());
        provider.useLenientSshdSessionFactory();
    }

    @Override
    protected void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            FileUtils.delete(directory, FileUtils.RECURSIVE | FileUtils.RETRY);
        }
    }

    @Test
    public void testCheckOutCommandWithPregeneratedKeysTest() throws Exception {
        // test key pairs being generated with ssh-keygen (they have a slighly different format than the ones tested
        // in testCheckOutCommandWithPassphraseTest and testCheckOutCommandTest)
        configureKeypairFromClasspathResource(getScmRepository(), "sample_rsa", "mySecret");
        super.testCheckOutCommandTest();
    }

    private void configureKeypairFromClasspathResource(ScmRepository repository, String resourceName, String passphrase)
            throws IOException, GeneralSecurityException {
        // accept public key
        try (InputStream publicKeyInputStream =
                this.getClass().getResourceAsStream("/ssh-keypairs/" + resourceName + ".pub")) {
            PublicKey publicKey = PublicKeyEntry.parsePublicKeyEntry(
                            IOUtils.toString(publicKeyInputStream, StandardCharsets.US_ASCII))
                    .resolvePublicKey(null, null, null);
            gitSshServer.addPublicKey(publicKey);
        }
        Path privateKeyFile = Files.createTempFile("privateKey", null);
        // private key into tmp file
        try (InputStream privateKeyInputStream = this.getClass().getResourceAsStream("/ssh-keypairs/" + resourceName)) {
            Files.copy(privateKeyInputStream, privateKeyFile, StandardCopyOption.REPLACE_EXISTING);
        }
        // configure provider repository with private key details
        ScmProviderRepositoryWithHost providerRepository =
                ScmProviderRepositoryWithHost.class.cast(repository.getProviderRepository());
        providerRepository.setPassphrase(passphrase); // may be null
        providerRepository.setPrivateKey(privateKeyFile.toString());
    }
}
