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
package org.apache.maven.scm.tck.command.untag;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.untag.UntagScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * This test tests the untag command.
 */
public abstract class UntagCommandTckTest extends ScmTckTestCase {

    protected String getTagName() {
        return "test-untag";
    }

    @Test
    public void testUntagCommandTest() throws Exception {
        String tag = getTagName();
        ScmProvider scmProvider = getScmManager().getProviderByUrl(getScmUrl());
        ScmRepository scmRepository = getScmRepository();
        ScmFileSet files = new ScmFileSet(getWorkingCopy());
        TagScmResult tagResult = scmProvider.tag(scmRepository, files, tag, new ScmTagParameters());

        assertResultIsSuccess(tagResult);
        CommandParameters params = new CommandParameters();
        params.setString(CommandParameter.TAG_NAME, tag);

        UntagScmResult untagResult = scmProvider.untag(scmRepository, files, params);

        assertResultIsSuccess(untagResult);

        try {
            untagResult = scmProvider.untag(scmRepository, files, params);
            assertFalse(untagResult.isSuccess()); // already been deleted
        } catch (ScmException ignored) {
        }

        try {
            CheckOutScmResult checkoutResult =
                    getScmManager().checkOut(scmRepository, new ScmFileSet(getAssertionCopy()), new ScmTag(tag));
            assertFalse(checkoutResult.isSuccess()); // can't check out a deleted tags
        } catch (ScmException ignored) {
        }
    }
}
