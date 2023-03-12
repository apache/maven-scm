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
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;

import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.transport.sshd.IdentityPasswordProvider;
import org.eclipse.jgit.transport.sshd.KeyPasswordProvider;
import org.eclipse.jgit.transport.sshd.SshdSessionFactory;
import org.eclipse.jgit.util.StringUtils;
import org.slf4j.Logger;

/**
 * {@link SshdSessionFactory} considering the settings from {@link GitScmProviderRepository}.
 *
 */
public class ScmProviderAwareSshdSessionFactory extends SshdSessionFactory {
    private final GitScmProviderRepository repo;
    private final Logger logger;

    public ScmProviderAwareSshdSessionFactory(GitScmProviderRepository repo, Logger logger) {
        this.repo = repo;
        this.logger = logger;
    }

    @Override
    protected List<Path> getDefaultIdentities(File sshDir) {
        if (!StringUtils.isEmptyOrNull(repo.getPrivateKey())) {
            logger.debug("Using private key at {}", repo.getPrivateKey());
            return Collections.singletonList(Paths.get(repo.getPrivateKey()));
        } else {
            return super.getDefaultIdentities(sshDir);
        }
    }

    @Override
    protected KeyPasswordProvider createKeyPasswordProvider(CredentialsProvider provider) {
        if (repo.getPassphrase() != null) {
            return new IdentityPasswordProvider(provider) {
                @Override
                public char[] getPassphrase(URIish uri, int attempt) throws IOException {
                    if (attempt > 0) {
                        throw new IOException(
                                "Passphrase was not correct in first attempt, " + "canceling further attempts!");
                    }
                    logger.debug("Using stored passphrase");
                    return repo.getPassphrase().toCharArray();
                }
            };
        } else {
            return super.createKeyPasswordProvider(provider);
        }
    }
}
