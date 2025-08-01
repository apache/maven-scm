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
package org.apache.maven.scm.provider.git.command.untag;

import java.io.File;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.untag.UntagScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.untag.UntagCommandTckTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/** {@inheritDoc} */
public abstract class GitUntagCommandTckTest extends UntagCommandTckTest {
    /** {@inheritDoc} */
    public void initRepo() throws Exception {
        GitScmTestUtils.initRepo("src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory());
    }

    @Override
    protected CheckOutScmResult checkOut(File workingDirectory, ScmRepository repository) throws Exception {
        try {
            return super.checkOut(workingDirectory, repository);
        } finally {
            GitScmTestUtils.setDefaultGitConfig(workingDirectory);
        }
    }

    @Test
    public void testPushTagDeletionRejected() throws Exception {
        String tag = getTagName();
        ScmProvider scmProvider = getScmManager().getProviderByUrl(getScmUrl());
        ScmRepository scmRepository = getScmRepository();
        ScmFileSet files = new ScmFileSet(getWorkingCopy());
        TagScmResult tagResult = scmProvider.tag(scmRepository, files, tag, new ScmTagParameters());

        assertResultIsSuccess(tagResult);

        GitScmTestUtils.setupRejectAllCommitsPrePushHook(getWorkingCopy());
        CommandParameters params = new CommandParameters();
        params.setString(CommandParameter.TAG_NAME, tag);

        UntagScmResult untagResult = scmProvider.untag(scmRepository, files, params);

        assertFalse("Tag deletion should not have been pushed", untagResult.isSuccess());
    }
}
