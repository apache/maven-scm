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
package org.apache.maven.scm.provider.git.gitexe.command.info;

import java.io.File;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.junit.Test;

import static org.apache.maven.scm.provider.git.GitScmTestUtils.GIT_COMMAND_LINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Olivier Lamy
 */
public class GitInfoCommandTest extends ScmTestCase {

    @Test
    public void testInfoCommand() throws Exception {
        checkScmPresence(GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/git/info", getRepositoryRoot(), getWorkingCopy());

        ScmProvider provider = getScmManager().getProviderByUrl(getScmUrl());
        ScmProviderRepository repository = provider.makeProviderScmRepository(getRepositoryRoot());
        assertNotNull(repository);
        InfoScmResult result = provider.info(repository, new ScmFileSet(getRepositoryRoot()), new CommandParameters());
        assertNotNull(result);
        assertEquals(
                "cd3c0dfacb65955e6fbb35c56cc5b1bf8ce4f767",
                result.getInfoItems().get(0).getRevision());
        //
    }

    @Test
    public void testInfoCommandWithShortRevision() throws Exception {
        checkScmPresence(GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/git/info", getRepositoryRoot(), getWorkingCopy());

        ScmProvider provider = getScmManager().getProviderByUrl(getScmUrl());
        ScmProviderRepository repository = provider.makeProviderScmRepository(getRepositoryRoot());
        assertNotNull(repository);
        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setInt(CommandParameter.SCM_SHORT_REVISION_LENGTH, 6);
        InfoScmResult result = provider.info(repository, new ScmFileSet(getRepositoryRoot()), commandParameters);
        assertNotNull(result);
        assertEquals(
                "revision must be short, exactly 6 digits ",
                "cd3c0d",
                result.getInfoItems().get(0).getRevision());
    }

    @Test
    public void testInfoCommandWithNegativeShortRevision() throws Exception {
        checkScmPresence(GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/git/info", getRepositoryRoot(), getWorkingCopy());

        ScmProvider provider = getScmManager().getProviderByUrl(getScmUrl());
        ScmProviderRepository repository = provider.makeProviderScmRepository(getRepositoryRoot());
        assertNotNull(repository);
        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setInt(CommandParameter.SCM_SHORT_REVISION_LENGTH, GitInfoCommand.NO_REVISION_LENGTH);
        InfoScmResult result = provider.info(repository, new ScmFileSet(getRepositoryRoot()), commandParameters);
        assertNotNull(result);
        assertEquals(
                "revision should not be short",
                "cd3c0dfacb65955e6fbb35c56cc5b1bf8ce4f767",
                result.getInfoItems().get(0).getRevision());
    }

    @Test
    public void testInfoCommandWithZeroShortRevision() throws Exception {
        checkScmPresence(GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/git/info", getRepositoryRoot(), getWorkingCopy());

        ScmProvider provider = getScmManager().getProviderByUrl(getScmUrl());
        ScmProviderRepository repository = provider.makeProviderScmRepository(getRepositoryRoot());
        assertNotNull(repository);
        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setInt(CommandParameter.SCM_SHORT_REVISION_LENGTH, 0);
        InfoScmResult result = provider.info(repository, new ScmFileSet(getRepositoryRoot()), commandParameters);
        assertNotNull(result);
        assertTrue(
                "revision should be not empty, minimum 4 (see git help rev-parse --short)",
                result.getInfoItems().get(0).getRevision().length() >= 4);
    }

    protected File getRepositoryRoot() {
        return getTestFile("target/scm-test/repository/git/info");
    }

    public String getScmUrl() throws Exception {
        return GitScmTestUtils.getScmUrl(getRepositoryRoot(), "git");
    }

    protected File getWorkingCopy() {
        return getTestFile("target/scm-test/git/info");
    }
}
