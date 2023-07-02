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
import java.util.Collections;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Before;
import org.junit.Test;

import static org.apache.maven.scm.provider.git.GitScmTestUtils.GIT_COMMAND_LINE;
import static org.junit.Assert.assertEquals;

/**
 * @author Wen Wu
 *
 */
public class GitExeCheckOutCommandRecursiveTest extends ScmTestCase {
    private File workingDirectory;

    private File repo;

    private ScmRepository scmRepository;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        workingDirectory = new File("target/checkout-recursive");
        FileUtils.deleteDirectory(workingDirectory);
        repo = new File("src/test/resources/repository_submodule");

        scmRepository = getScmManager()
                .makeScmRepository(
                        "scm:git:" + repo.toPath().toAbsolutePath().toUri().toASCIIString());
    }

    @Test
    public void testCheckoutNoBranch() throws Exception {
        checkScmPresence(GIT_COMMAND_LINE);
        CheckOutScmResult result = checkoutRepo(false);

        assertEquals(5, result.getCheckedOutFiles().size());

        // git submodule set-url sub-prj file:///...
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(
                workingDirectory,
                "submodule",
                (GitScmProviderRepository) scmRepository.getProviderRepository(),
                Collections.emptyMap());

        String repoUrl = repo.toPath().toAbsolutePath().toUri().toASCIIString();
        cl.createArg().setValue("set-url");
        cl.createArg().setValue("sub-prj");
        cl.createArg().setValue(repoUrl);

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        GitCommandLineUtils.execute(cl, stdout, stderr);

        result = checkoutRepo(true);
        assertEquals(9, result.getCheckedOutFiles().size());
    }

    protected CheckOutScmResult checkoutRepo(boolean recursive) throws Exception {
        CheckOutScmResult result =
                getScmManager().checkOut(scmRepository, new ScmFileSet(workingDirectory), (ScmVersion) null, recursive);

        assertResultIsSuccess(result);
        return result;
    }
}
