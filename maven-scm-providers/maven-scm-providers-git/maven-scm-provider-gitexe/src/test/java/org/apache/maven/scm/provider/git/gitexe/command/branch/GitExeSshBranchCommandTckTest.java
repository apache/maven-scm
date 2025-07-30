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
package org.apache.maven.scm.provider.git.gitexe.command.branch;

import java.security.GeneralSecurityException;

import org.apache.maven.scm.provider.git.command.branch.GitSshBranchCommandTckTest;
import org.apache.maven.scm.provider.git.gitexe.GitExeTestUtils;
import org.apache.maven.scm.repository.ScmRepository;

/**
 *
 */
public class GitExeSshBranchCommandTckTest extends GitSshBranchCommandTckTest {

    public GitExeSshBranchCommandTckTest() throws GeneralSecurityException {
        super();
    }

    @Override
    protected String getScmProvider() {
        return "git";
    }

    @Override
    public void configureCredentials(ScmRepository repository, String passphrase) throws Exception {
        super.configureCredentials(repository, passphrase);
        GitExeTestUtils.configureLenientSshAuthentication(getScmManager(), repository);
    }
}
