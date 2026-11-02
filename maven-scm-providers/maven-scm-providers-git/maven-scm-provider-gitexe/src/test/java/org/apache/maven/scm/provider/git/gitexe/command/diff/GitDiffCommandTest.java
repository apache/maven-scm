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
package org.apache.maven.scm.provider.git.gitexe.command.diff;

import java.io.File;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.Test;

import static org.apache.maven.scm.provider.git.GitScmTestUtils.GIT_COMMAND_LINE;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Reproduces SCM-1027: {@code git diff --cached} rejects two revision arguments, so a diff requested with
 * both a start and an end revision failed instead of comparing the two.
 */
public class GitDiffCommandTest extends ScmTestCase {

    @Test
    void testDiffAcceptsBothStartAndEndRevision() throws Exception {
        checkSystemCmdPresence(GIT_COMMAND_LINE);

        File repo = getRepositoryRoot();
        File checkedOutRepo = getWorkingCopy();

        GitScmTestUtils.initRepo("src/test/resources/repository/", repo, getWorkingDirectory());

        String scmUrl = "scm:git:" + repo.toPath().toAbsolutePath().toUri().toASCIIString();
        ScmRepository scmRepository = getScmManager().makeScmRepository(scmUrl);

        FileUtils.deleteDirectory(checkedOutRepo);
        assertTrue(checkedOutRepo.mkdirs());

        CheckOutScmResult checkOutResult = getScmManager().checkOut(scmRepository, new ScmFileSet(checkedOutRepo));
        assertResultIsSuccess(checkOutResult);

        GitScmTestUtils.setDefaultGitConfig(checkedOutRepo);

        ScmProvider provider = getScmManager().getProviderByUrl(scmUrl);
        ScmFileSet checkedOutFileSet = new ScmFileSet(checkedOutRepo);

        File file = new File(checkedOutRepo, "scm-1027.txt");

        FileUtils.fileWrite(file.getAbsolutePath(), "first revision");
        CheckInScmResult firstCheckin = getScmManager().checkIn(scmRepository, checkedOutFileSet, "first revision");
        assertResultIsSuccess(firstCheckin);
        ScmRevision firstRevision = new ScmRevision(currentRevision(provider, scmRepository, checkedOutFileSet));

        FileUtils.fileWrite(file.getAbsolutePath(), "second revision");
        CheckInScmResult secondCheckin = getScmManager().checkIn(scmRepository, checkedOutFileSet, "second revision");
        assertResultIsSuccess(secondCheckin);
        ScmRevision secondRevision = new ScmRevision(currentRevision(provider, scmRepository, checkedOutFileSet));

        DiffScmResult result = getScmManager().diff(scmRepository, checkedOutFileSet, firstRevision, secondRevision);

        assertResultIsSuccess(result);
    }

    private static String currentRevision(ScmProvider provider, ScmRepository repository, ScmFileSet fileSet)
            throws Exception {
        InfoScmResult result = provider.info(repository.getProviderRepository(), fileSet, new CommandParameters());
        assertTrue(result.isSuccess(), "info command should succeed: " + result.getProviderMessage());
        return result.getInfoItems().get(0).getRevision();
    }
}
