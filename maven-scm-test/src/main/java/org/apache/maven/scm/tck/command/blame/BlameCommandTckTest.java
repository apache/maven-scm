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
package org.apache.maven.scm.tck.command.blame;

import java.util.Date;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.command.blame.BlameScmRequest;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Evgeny Mandrikov
 */
public abstract class BlameCommandTckTest extends ScmTckTestCase {
    private static final String COMMIT_MSG = "Second changelog";

    @Test
    public void blameCommand() throws Exception {
        ScmRepository repository = getScmRepository();
        ScmManager manager = getScmManager();
        ScmProvider provider = manager.getProviderByRepository(getScmRepository());
        ScmFileSet fileSet = new ScmFileSet(getWorkingCopy());

        BlameScmResult result;
        BlameLine line;

        // === readme.txt ===
        BlameScmRequest blameScmRequest = new BlameScmRequest(repository, fileSet);
        blameScmRequest.setFilename("readme.txt");
        // result = manager.blame( repository, fileSet, "readme.txt" );
        result = manager.blame(blameScmRequest);
        assertNotNull(result, "The command returned a null result.");
        assertResultIsSuccess(result);
        assertEquals(1, result.getLines().size(), "Expected 1 line in blame");
        line = result.getLines().get(0);
        String initialRevision = line.getRevision();

        // Make a timestamp that we know are after initial revision but before the second
        Date timeBeforeSecond = new Date(); // Current time
        // pause a couple seconds...
        Thread.sleep(2000);
        // Make a change to the readme.txt and commit the change
        this.edit(getWorkingCopy(), "readme.txt", null, getScmRepository());
        ScmTestCase.makeFile(getWorkingCopy(), "/readme.txt", "changed readme.txt");
        CheckInScmResult checkInResult = provider.checkIn(getScmRepository(), fileSet, COMMIT_MSG);
        assertTrue(checkInResult.isSuccess(), "Unable to checkin changes to the repository");

        result = manager.blame(repository, fileSet, "readme.txt");

        // pause a couple seconds...
        Thread.sleep(2000);
        Date timeAfterSecond = new Date(); // Current time

        assertNotNull(result, "The command returned a null result.");
        assertResultIsSuccess(result);

        assertEquals(1, result.getLines().size(), "Expected 1 line in blame");
        line = result.getLines().get(0);

        assertNotNull(line.getAuthor(), "Expected not null author");
        assertNotNull(line.getRevision(), "Expected not null revision");
        assertNotNull(line.getDate(), "Expected not null date");

        assertNotEquals(initialRevision, line.getRevision(), "Expected another revision");
        if (isTestDateTime()) {
            assertDateBetween(timeBeforeSecond, timeAfterSecond, line.getDate());
        }

        // === pom.xml ===
        result = manager.blame(repository, fileSet, "pom.xml");

        assertNotNull(result, "The command returned a null result.");

        assertResultIsSuccess(result);

        verifyResult(result);
    }

    protected boolean isTestDateTime() {
        return true;
    }

    protected void assertDateBetween(Date start, Date end, Date actual) {
        assertTrue(
                start.before(actual) && actual.before(end),
                "Expected date between " + start + " and " + end + ", but was " + actual);
    }

    protected abstract void verifyResult(BlameScmResult result);
}
