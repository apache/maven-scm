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
package org.apache.maven.scm.provider.git.jgit.command.branch;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.maven.scm.provider.git.command.branch.GitSshBranchCommandTckTest;
import org.apache.maven.scm.provider.git.jgit.JGitTestScmProvider;
import org.eclipse.jgit.util.FileUtils;

/**
 *
 */
public class JGitSshBranchCommandTckTest extends GitSshBranchCommandTckTest {

    public JGitSshBranchCommandTckTest() throws GeneralSecurityException {
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
}
