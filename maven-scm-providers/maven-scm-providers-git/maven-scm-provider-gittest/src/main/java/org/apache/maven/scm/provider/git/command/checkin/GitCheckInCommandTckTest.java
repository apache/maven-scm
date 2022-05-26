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
package org.apache.maven.scm.provider.git.command.checkin;

import java.io.File;
import java.io.IOException;

import org.apache.maven.scm.PlexusJUnit4TestCase;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.checkin.CheckInCommandTckTest;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 *
 */
public abstract class GitCheckInCommandTckTest extends CheckInCommandTckTest {

    /** {@inheritDoc} */
    public void initRepo() throws Exception {
        GitScmTestUtils.initRepo("src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory());
    }

    @Override
    protected CheckOutScmResult checkOut(File workingDirectory, ScmRepository repository) throws Exception {
        try {
            return super.checkOut(workingDirectory, repository);
        } finally {
            GitScmTestUtils.setDefaulGitConfig(workingDirectory);
        }
    }

    @Test
    public void testUpToDatePush() throws Exception {
        File checkedOutRepo = getWorkingCopy();

        ScmRepository scmRepository = getScmManager().makeScmRepository(getScmUrl());
        checkoutRepoInto(checkedOutRepo, scmRepository);

        // Add a default user to the config
        GitScmTestUtils.setDefaulGitConfig(checkedOutRepo);

        CheckInScmResult result =
                getScmManager().checkIn(scmRepository, new ScmFileSet(checkedOutRepo), "No change commit message");

        assertResultIsSuccess(result);
    }

    @Test
    public void testRejectedNonFastForwardPush() throws Exception {
        File blockingRepo = PlexusJUnit4TestCase.getTestFile("target/scm-test/blocking-repo");
        File rejectedRepo = PlexusJUnit4TestCase.getTestFile("target/scm-test/rejected-repo");

        ScmRepository scmRepository = getScmManager().makeScmRepository(getScmUrl());
        checkoutRepoInto(rejectedRepo, scmRepository);
        checkoutRepoInto(blockingRepo, scmRepository);

        // Add a default user to the config
        GitScmTestUtils.setDefaulGitConfig(rejectedRepo);
        GitScmTestUtils.setDefaulGitConfig(blockingRepo);

        ScmFileSet blockingFileSet = createWorkspaceChange(rejectedRepo);

        CheckInScmResult blockingResult = getScmManager().checkIn(scmRepository, blockingFileSet, "Blocking commit");
        assertResultIsSuccess(blockingResult);

        ScmFileSet rejectedFileSet = createWorkspaceChange(blockingRepo);

        CheckInScmResult checkInScmResult = getScmManager().checkIn(scmRepository, rejectedFileSet, "Rejected commit");
        assertFalse(
                "check-in should have been rejected since fast forward was not possible", checkInScmResult.isSuccess());
    }

    private CheckOutScmResult checkoutRepoInto(File workingCopy, ScmRepository scmRepository) throws Exception {
        FileUtils.deleteDirectory(workingCopy);
        workingCopy.mkdir();

        CheckOutScmResult result = getScmManager().checkOut(scmRepository, new ScmFileSet(workingCopy), null);

        assertResultIsSuccess(result);
        return result;
    }

    private ScmFileSet createWorkspaceChange(File repo) throws IOException {
        File beerFile = new File(repo.getAbsolutePath(), "beer.xml");
        FileUtils.fileWrite(beerFile.getAbsolutePath(), "1 litre");
        return new ScmFileSet(repo, beerFile.getName());
    }
}
