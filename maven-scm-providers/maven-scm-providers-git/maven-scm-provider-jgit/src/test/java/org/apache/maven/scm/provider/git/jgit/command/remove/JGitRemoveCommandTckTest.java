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
package org.apache.maven.scm.provider.git.jgit.command.remove;

import java.io.File;
import java.io.IOException;

import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.command.remove.GitRemoveCommandTckTest;
import org.apache.maven.scm.repository.ScmRepository;
import org.eclipse.jgit.util.FileUtils;

/**
 * @author Georg Tsakumagos
 */
public class JGitRemoveCommandTckTest extends GitRemoveCommandTckTest {
    /**
     * {@inheritDoc}
     */
    public String getScmUrl() throws Exception {
        return GitScmTestUtils.getScmUrl(getRepositoryRoot(), "jgit");
    }

    @Override
    protected void deleteDirectory(File directory) throws IOException {
        if (directory.exists()) {
            FileUtils.delete(directory, FileUtils.RECURSIVE | FileUtils.RETRY);
        }
    }

    @Override
    protected CheckOutScmResult checkOut(File workingDirectory, ScmRepository repository) throws Exception {
        CheckOutScmResult result = super.checkOut(workingDirectory, repository);
        GitScmTestUtils.setDefaulGitConfig(workingDirectory);
        return result;
    }
}
