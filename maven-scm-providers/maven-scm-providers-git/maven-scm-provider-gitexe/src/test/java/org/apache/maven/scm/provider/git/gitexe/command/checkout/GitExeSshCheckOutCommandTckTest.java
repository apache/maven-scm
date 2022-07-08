package org.apache.maven.scm.provider.git.gitexe.command.checkout;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.scm.provider.git.command.checkout.GitSshCheckOutCommandTckTest;
import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

/**
 *
 */
public class GitExeSshCheckOutCommandTckTest
    extends GitSshCheckOutCommandTckTest
{
    private Path knownHostsFile;

    public static final String VARIABLE_GIT_SSH_COMMAND = "GIT_SSH_COMMAND"; // https://git-scm.com/docs/git#Documentation/git.txt-codeGITSSHCOMMANDcode, requires git 2.3.0 or newer

    public GitExeSshCheckOutCommandTckTest() throws GeneralSecurityException
    {
        super();
    }

    @Override
    protected String getScmProvider()
    {
        return "git";
    }

    @Override
    public void configureCredentials( ScmRepository repository, String passphrase ) throws Exception
    {
        super.configureCredentials( repository, passphrase );
        GitScmProviderRepository providerRepository = (GitScmProviderRepository) repository.getProviderRepository();
        GitExeScmProvider provider = (GitExeScmProvider) getScmManager().getProviderByRepository( getScmRepository() );
        knownHostsFile = Files.createTempFile( "known-hosts", null );
        provider.setEnvironmentVariable( VARIABLE_GIT_SSH_COMMAND, "ssh -o UserKnownHostsFile=" + knownHostsFile +
                " -o StrictHostKeyChecking=no -o IdentitiesOnly=yes -i " + FilenameUtils.separatorsToUnix( providerRepository.getPrivateKey() ) );
    }

    @Override
    public void removeRepo() throws Exception
    {
        super.removeRepo();
        Files.deleteIfExists( knownHostsFile );
    }

}
