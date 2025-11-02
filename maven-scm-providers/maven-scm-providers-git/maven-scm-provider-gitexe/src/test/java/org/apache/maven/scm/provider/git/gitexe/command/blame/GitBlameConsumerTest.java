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
package org.apache.maven.scm.provider.git.gitexe.command.blame;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.util.ConsumerUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test the {@link GitBlameConsumer} in various different situations.
 * Depending on the underlying operating system we might get
 * slightly different output from a <pre>git blame</pre> commandline invocation.
 */
public class GitBlameConsumerTest extends ScmTestCase {
    @Test
    public void testConsumerEasy() throws Exception {
        GitBlameConsumer consumer = consumeFile("/src/test/resources/git/blame/git-blame-3.out");

        Assertions.assertEquals(36, consumer.getLines().size());

        BlameLine blameLine = consumer.getLines().get(11);
        Assertions.assertEquals("e670863b2b03e158c59f34af1fee20f91b2bd852", blameLine.getRevision());
        Assertions.assertEquals("Mark Struberg", blameLine.getAuthor());
        Assertions.assertNotNull(blameLine.getDate());
    }

    @Test
    public void testConsumer() throws Exception {
        GitBlameConsumer consumer = consumeFile("/src/test/resources/git/blame/git-blame.out");

        Assertions.assertEquals(187, consumer.getLines().size());

        BlameLine blameLine = consumer.getLines().get(11);
        Assertions.assertEquals("e670863b2b03e158c59f34af1fee20f91b2bd852", blameLine.getRevision());
        Assertions.assertEquals("Mark Struberg", blameLine.getAuthor());
        Assertions.assertNotNull(blameLine.getDate());
    }

    /**
     * Test what happens if a git-blame command got invoked on a
     * file which has no content.
     */
    @Test
    public void testConsumerEmptyFile() throws Exception {
        GitBlameConsumer consumer = consumeFile("/src/test/resources/git/blame/git-blame-empty.out");

        Assertions.assertEquals(0, consumer.getLines().size());
    }

    /**
     * Test what happens if a git-blame command got invoked on a
     * file which didn't got added to the git repo yet.
     */
    @Test
    public void testConsumerOnNewFile() throws Exception {
        GitBlameConsumer consumer = consumeFile("/src/test/resources/git/blame/git-blame-new-file.out");

        Assertions.assertEquals(3, consumer.getLines().size());
        BlameLine blameLine = consumer.getLines().get(0);
        Assertions.assertNotNull(blameLine);
        Assertions.assertEquals("0000000000000000000000000000000000000000", blameLine.getRevision());
        Assertions.assertEquals("Not Committed Yet", blameLine.getAuthor());
    }

    /**
     * Test a case where the committer and author are different persons
     */
    @Test
    public void testConsumerWithDifferentAuthor() throws Exception {
        GitBlameConsumer consumer = consumeFile("/src/test/resources/git/blame/git-blame-different-author.out");

        Assertions.assertEquals(93, consumer.getLines().size());
        BlameLine blameLine = consumer.getLines().get(0);
        Assertions.assertNotNull(blameLine);
        Assertions.assertEquals("39574726d20f62023d39311e6032c7ab0a9d3cdb", blameLine.getRevision());
        Assertions.assertEquals("Mark Struberg", blameLine.getAuthor());
        Assertions.assertEquals("Mark Struberg", blameLine.getCommitter());

        blameLine = consumer.getLines().get(12);
        Assertions.assertNotNull(blameLine);
        Assertions.assertEquals("41e5bc05953781a5702f597a1a36c55371b517d3", blameLine.getRevision());
        Assertions.assertEquals("Another User", blameLine.getAuthor());
        Assertions.assertEquals("Mark Struberg", blameLine.getCommitter());
    }

    /**
     * This unit test compares the output of our new parsing with a
     * simplified git blame output.
     */
    @Test
    public void testConsumerCompareWithOriginal() throws Exception {
        GitBlameConsumer consumer = consumeFile("/src/test/resources/git/blame/git-blame-2.out");
        Assertions.assertNotNull(consumer);

        List<BlameLine> consumerLines = consumer.getLines();
        Iterator<BlameLine> consumerLineIt = consumerLines.iterator();

        File compareWithFile = getTestFile("/src/test/resources/git/blame/git-blame-2.orig");
        Assertions.assertNotNull(compareWithFile);

        try (BufferedReader r = new BufferedReader(new FileReader(compareWithFile))) {
            String line;
            SimpleDateFormat blameDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            blameDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

            int lineNr = 0;

            while ((line = r.readLine()) != null && line.trim().length() > 0) {
                if (!consumerLineIt.hasNext()) {
                    fail("GitBlameConsumer lines do not match the original output!");
                }
                BlameLine blameLine = consumerLineIt.next();
                Assertions.assertNotNull(blameLine);

                String[] parts = line.split("\t");
                Assertions.assertEquals(3, parts.length);

                Assertions.assertEquals(parts[0], blameLine.getRevision(), "error in line " + lineNr);
                Assertions.assertEquals(parts[1], blameLine.getAuthor(), "error in line " + lineNr);
                Assertions.assertEquals(parts[2], blameDateFormat.format(blameLine.getDate()), "error in line " + lineNr);

                lineNr++;
            }
        }

        if (consumerLineIt.hasNext()) {
            fail("GitBlameConsumer found more lines than in the original output!");
        }
    }

    /**
     * Consume all lines in the given file with a fresh {@link GitBlameConsumer}.
     *
     * @param fileName
     * @return the resulting {@link GitBlameConsumer}
     * @throws IOException
     */
    private GitBlameConsumer consumeFile(String fileName) throws IOException {
        GitBlameConsumer consumer = new GitBlameConsumer();

        File f = getTestFile(fileName);

        ConsumerUtils.consumeFile(f, consumer);

        return consumer;
    }
}
