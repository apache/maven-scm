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
package org.apache.maven.scm.provider.git.gitexe.command.checkout;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.junit.Before;
import org.junit.Test;

import static org.apache.maven.scm.provider.git.GitScmTestUtils.GIT_COMMAND_LINE;
import static org.junit.Assert.assertEquals;

/**
 * @author Bertrand Paquet
 *
 */
public class GitExeCheckOutCommandNoBranchTest extends ScmTestCase {
    private File workingDirectory;

    private File repo;

    private ScmRepository scmRepository;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        workingDirectory = new File("target/checkin-nobranch");
        FileUtils.deleteDirectory(workingDirectory);
        repo = new File("src/test/resources/repository_no_branch");

        scmRepository = getScmManager()
                .makeScmRepository(
                        "scm:git:" + repo.toPath().toAbsolutePath().toUri().toASCIIString());
    }

    @Test
    public void testCheckoutNoBranch() throws Exception {
        checkSystemCmdPresence(GIT_COMMAND_LINE);
        CheckOutScmResult result = checkoutRepo();
        assertEquals(0, result.getCheckedOutFiles().size());
    }

    @Test
    public void testDoubleCheckoutNoBranch() throws Exception {
        checkSystemCmdPresence(GIT_COMMAND_LINE);
        CheckOutScmResult result = checkoutRepo();
        assertEquals(0, result.getCheckedOutFiles().size());
        CheckOutScmResult result2 = checkoutRepo();
        assertEquals(0, result2.getCheckedOutFiles().size());
    }

    protected CheckOutScmResult checkoutRepo() throws Exception {
        CheckOutScmResult result =
                getScmManager().checkOut(scmRepository, new ScmFileSet(workingDirectory), (ScmVersion) null);

        assertResultIsSuccess(result);
        return result;
    }
}
