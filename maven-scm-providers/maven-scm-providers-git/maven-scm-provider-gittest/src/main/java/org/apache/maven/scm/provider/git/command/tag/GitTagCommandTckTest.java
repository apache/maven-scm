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
package org.apache.maven.scm.provider.git.command.tag;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.tag.TagCommandTckTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public abstract class GitTagCommandTckTest extends TagCommandTckTest {
    /**
     * {@inheritDoc}
     */
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
    void testPushTagRejected() throws Exception {
        String tag = getTagName();

        GitScmTestUtils.setupRejectAllCommitsPrePushHook(getWorkingCopy());
        @SuppressWarnings("deprecation")
        TagScmResult tagResult = getScmManager()
                .getProviderByUrl(getScmUrl())
                .tag(getScmRepository(), new ScmFileSet(getWorkingCopy()), tag);

        assertFalse(tagResult.isSuccess(), "Tag should not have been pushed");
    }

    @Test
    void testAnnotatedTagIsTheDefault() throws Exception {
        ScmTagParameters parameters = new ScmTagParameters();
        parameters.setMessage("annotated message");

        tag("annotated-tag", parameters);

        assertEquals("tag", gitObjectType("annotated-tag"));
    }

    @Test
    void testLightweightTag() throws Exception {
        ScmTagParameters parameters = new ScmTagParameters();
        parameters.setMessage("ignored message");
        parameters.setLightweight(true);

        tag("lightweight-tag", parameters);

        assertEquals("commit", gitObjectType("lightweight-tag"));
    }

    private void tag(String tag, ScmTagParameters parameters) throws Exception {
        TagScmResult tagResult = getScmManager()
                .getProviderByUrl(getScmUrl())
                .tag(getScmRepository(), new ScmFileSet(getWorkingCopy()), tag, parameters);

        assertResultIsSuccess(tagResult);
    }

    private String gitObjectType(String tag) throws Exception {
        Process process = new ProcessBuilder("git", "cat-file", "-t", "refs/tags/" + tag)
                .directory(getWorkingCopy())
                .redirectErrorStream(true)
                .start();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String type = reader.readLine();
            process.waitFor();
            return type;
        }
    }
}
