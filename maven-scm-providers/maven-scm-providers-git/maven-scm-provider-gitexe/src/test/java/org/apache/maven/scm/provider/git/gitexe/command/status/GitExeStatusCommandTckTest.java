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
package org.apache.maven.scm.provider.git.gitexe.command.status;

import java.io.File;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.provider.git.command.status.GitStatusCommandTckTest;
import org.apache.maven.scm.repository.ScmRepository;

import static org.apache.maven.scm.provider.git.GitScmTestUtils.GIT_COMMAND_LINE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class GitExeStatusCommandTckTest extends GitStatusCommandTckTest {
    @org.junit.jupiter.api.Test
    void testStatusFindsUntrackedFilesInPatternMatchedDirectories() throws Exception {
        ScmRepository repository = makeScmRepository(getScmUrl());
        File workingCopy = getUpdatingCopy();
        checkOut(workingCopy, repository);
        GitScmTestUtils.setDefaultGitConfig(workingCopy);

        File projectFile = new File(workingCopy, "new-dir/project.xml");
        assertTrue(projectFile.getParentFile().mkdirs());
        assertTrue(projectFile.createNewFile());

        StatusScmResult result = getScmManager()
                .getProviderByUrl(getScmUrl())
                .status(repository, new ScmFileSet(workingCopy, "**/*.xml", null));

        assertResultIsSuccess(result);
        List<ScmFile> changedFiles = result.getChangedFiles();
        assertEquals(1, changedFiles.size());
        assertEquals("new-dir/project.xml", changedFiles.get(0).getPath());
        assertEquals(ScmFileStatus.UNKNOWN, changedFiles.get(0).getStatus());
    }

    @Override
    public String getScmProviderCommand() {
        return GIT_COMMAND_LINE;
    }

    /** {@inheritDoc} */
    public String getScmUrl() throws Exception {
        return GitScmTestUtils.getScmUrl(getRepositoryRoot(), "git");
    }
}
