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
package org.apache.maven.scm.provider.git.jgit.command.remoteinfo;

import java.io.File;
import java.io.IOException;

import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.command.remoteinfo.AbstractGitRemoteInfoCommandTckTest;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.eclipse.jgit.util.FileUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Dominik Bartholdi (imod)
 */
public class JGitRemoteInfoCommandTckTest extends AbstractGitRemoteInfoCommandTckTest {
    @Override
    protected void checkResult(RemoteInfoScmResult result) {
        assertEquals(1, result.getBranches().size());
        assertEquals(
                "92f139dfec4d1dfb79c3cd2f94e83bf13129668b", result.getBranches().get("master"));

        assertEquals(0, result.getTags().size());
    }

    /**
     * {@inheritDoc}
     */
    public String getScmUrl() throws Exception {
        String scmUrl = GitScmTestUtils.getScmUrl(getRepositoryRoot(), "jgit");
        return scmUrl;
    }

    @Override
    protected ScmProviderRepository getScmProviderRepository() throws Exception {
        return new GitScmProviderRepository(getScmUrl().substring("scm:jgit:".length()));
    }

    @Override
    protected void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            FileUtils.delete(directory, FileUtils.RECURSIVE | FileUtils.RETRY);
        }
    }
}
