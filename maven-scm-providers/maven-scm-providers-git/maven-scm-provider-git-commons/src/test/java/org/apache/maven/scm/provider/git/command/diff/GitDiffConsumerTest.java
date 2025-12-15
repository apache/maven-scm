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
package org.apache.maven.scm.provider.git.command.diff;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.util.ConsumerUtils;
import org.junit.jupiter.api.Test;

import static org.codehaus.plexus.testing.PlexusExtension.getTestFile;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
class GitDiffConsumerTest extends ScmTestCase {
    @Test
    void emptyLogConsumer() throws Exception {
        GitDiffConsumer consumer = new GitDiffConsumer(null);

        File f = getTestFile("/src/test/resources/git/diff/git-diff-empty.log");

        ConsumerUtils.consumeFile(f, consumer);

        List<ScmFile> changedFiles = consumer.getChangedFiles();

        assertEquals(0, changedFiles.size());
    }

    @Test
    void log1Consumer() throws Exception {
        GitDiffConsumer consumer = new GitDiffConsumer(null);

        File f = getTestFile("src/test/resources/git/diff/git-diff1.log");

        ConsumerUtils.consumeFile(f, consumer);

        List<ScmFile> changedFiles = consumer.getChangedFiles();

        assertEquals(1, changedFiles.size());

        checkScmFile(changedFiles.get(0), "olamy.test", ScmFileStatus.MODIFIED);

        Map<String, CharSequence> differences = consumer.getDifferences();
        assertNotNull(differences);

        StringBuilder readmeDiffs = new StringBuilder(differences.get("olamy.test"));
        assertNotNull(readmeDiffs);
        assertTrue(readmeDiffs.indexOf("+new line") >= 0);
    }

    @Test
    void log2Consumer() throws Exception {
        GitDiffConsumer consumer = new GitDiffConsumer(null);

        File f = getTestFile("src/test/resources/git/diff/git-diff2.log");

        ConsumerUtils.consumeFile(f, consumer);

        List<ScmFile> changedFiles = consumer.getChangedFiles();

        assertEquals(2, changedFiles.size());

        checkScmFile(changedFiles.get(0), "pom.xml", ScmFileStatus.MODIFIED);

        checkScmFile(changedFiles.get(1), "test.txt", ScmFileStatus.MODIFIED);

        Map<String, CharSequence> differences = consumer.getDifferences();
        assertNotNull(differences);

        StringBuilder addDiffs = new StringBuilder(differences.get("pom.xml"));
        assertNotNull(addDiffs);
        assertTrue(addDiffs.indexOf("+  <!-- test -->") >= 0);

        addDiffs = new StringBuilder(differences.get("test.txt"));
        assertNotNull(addDiffs);
        assertTrue(addDiffs.indexOf("+maven-scm git provider works fine :-)") >= 0);
    }

    @Test
    void log3Consumer() throws Exception {
        GitDiffConsumer consumer = new GitDiffConsumer(null);

        File f = getTestFile("src/test/resources/git/diff/git-diff3.log");

        ConsumerUtils.consumeFile(f, consumer);

        List<ScmFile> changedFiles = consumer.getChangedFiles();

        assertEquals(1, changedFiles.size());

        checkScmFile(changedFiles.get(0), "pom.xml", ScmFileStatus.MODIFIED);

        Map<String, CharSequence> differences = consumer.getDifferences();
        assertNotNull(differences);

        CharSequence addDiffs = differences.get("pom.xml");
        assertNotNull(addDiffs);
        assertEquals("", addDiffs.toString());
    }

    @Test
    void log4Consumer() throws Exception {
        GitDiffConsumer consumer = new GitDiffConsumer(null);

        File f = getTestFile("src/test/resources/git/diff/git-diff4.log");

        ConsumerUtils.consumeFile(f, consumer);

        List<ScmFile> changedFiles = consumer.getChangedFiles();

        assertEquals(1, changedFiles.size());

        checkScmFile(changedFiles.get(0), "pom.xml", ScmFileStatus.MODIFIED);

        Map<String, CharSequence> differences = consumer.getDifferences();
        assertNotNull(differences);

        StringBuilder addDiffs = new StringBuilder(differences.get("pom.xml"));
        assertNotNull(addDiffs);
        assertTrue(addDiffs.indexOf("+  <!-- test -->") >= 0);
    }

    private void checkScmFile(ScmFile fileToTest, String expectedFilePath, ScmFileStatus expectedStatus) {
        assertEquals(expectedFilePath, fileToTest.getPath());
        assertEquals(expectedStatus, fileToTest.getStatus());
    }
}
