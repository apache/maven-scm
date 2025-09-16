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
package org.apache.maven.scm.provider.svn.command.mkdir;

import java.io.File;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.apache.maven.scm.tck.command.mkdir.MkdirCommandTckTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 */
public class SvnMkdirCommandTckTest extends MkdirCommandTckTest {
    /**
     * {@inheritDoc}
     */
    public String getScmUrl() throws Exception {
        return SvnScmTestUtils.getScmUrl(new File(getRepositoryRoot(), "trunk"));
    }

    /**
     * {@inheritDoc}
     */
    public void initRepo() throws Exception {
        SvnScmTestUtils.initializeRepository(getRepositoryRoot());
    }

    @Test
    public void testMkdirCommandMkdirUrl() throws Exception {
        ScmFileSet fileSet = new ScmFileSet(getWorkingCopy(), new File(getMissingDirectory()));

        MkdirScmResult result = getScmManager().mkdir(getScmRepository(), fileSet, "Mkdir message", false);

        assertResultIsSuccess(result);

        assertNotNull(result.getRevision());

        ListScmResult listResult = getScmManager().list(getScmRepository(), fileSet, true, null);

        assertTrue("Directory should have been found.", listResult.isSuccess());
    }

    @Test
    public void testMkdirCommandDirAlreadyAdded() throws Exception {
        ScmFileSet fileSet = new ScmFileSet(getWorkingCopy(), new File(getMissingDirectory()));

        MkdirScmResult result = getScmManager().mkdir(getScmRepository(), fileSet, null, false);

        assertResultIsSuccess(result);

        assertNotNull(result.getRevision());

        ListScmResult listResult = getScmManager().list(getScmRepository(), fileSet, true, null);

        assertTrue("Directory should have been found.", listResult.isSuccess());

        // add the directory again
        result = getScmManager().mkdir(getScmRepository(), fileSet, null, false);

        printOutputError(result);

        assertFalse(result.isSuccess());
    }
}
