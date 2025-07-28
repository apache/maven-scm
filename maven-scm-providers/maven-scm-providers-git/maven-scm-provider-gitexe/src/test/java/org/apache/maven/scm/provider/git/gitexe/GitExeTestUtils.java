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
package org.apache.maven.scm.provider.git.gitexe;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;

public class GitExeTestUtils {

    /**
     * Environment variable to customize the SSH command used by git, requires git 2.3.0 or newer
     * @see <a href="https://git-scm.com/docs/git#Documentation/git.txt-GITSSHCOMMAND">GIT_SSH_COMMAND</a>
     */
    public static final String VARIABLE_GIT_SSH_COMMAND = "GIT_SSH_COMMAND";

    private GitExeTestUtils() {
        // Utility class, no instantiation
    }

    /**
     * Configures the git executable to use a custom SSH command that allows lenient host key checking.
     * It uses the private key specified in the repository configuration.
     * @param scmManager
     * @param repository
     * @throws NoSuchScmProviderException
     */
    public static void configureLenientSshAuthentication(ScmManager scmManager, ScmRepository repository)
            throws NoSuchScmProviderException {
        ScmProviderRepositoryWithHost providerRepository =
                (ScmProviderRepositoryWithHost) repository.getProviderRepository();
        GitExeScmProvider provider = (GitExeScmProvider) scmManager.getProviderByRepository(repository);
        String privateKey = providerRepository.getPrivateKey();
        if (StringUtils.isBlank(privateKey)) {
            throw new IllegalArgumentException("Private key must be set in the repository configuration");
        }
        provider.setEnvironmentVariable(
                VARIABLE_GIT_SSH_COMMAND,
                "ssh -o StrictHostKeyChecking=no -o IdentitiesOnly=yes -i "
                        + FilenameUtils.separatorsToUnix(providerRepository.getPrivateKey()));
    }
}
