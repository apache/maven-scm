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
package org.apache.maven.scm.provider.git.gitexe.command.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;

import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.ReaderFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @since 1.2
 *
 */
public class GitLatestRevisionCommandConsumerTest extends ScmTestCase {
    @Test
    void testUpToDate() throws Exception {

        GitLatestRevisionCommandConsumer consumer =
                buildGitLatestRevisionCommandConsumer("/src/test/resources/git/update/git-update-latest-rev.out");

        String latestRev = consumer.getLatestRevision();

        assertNotNull(latestRev);
        assertEquals("a300c56a341bae8d0eb5ec4ed5551a11c75a5a6e", latestRev);
    }

    // utils methods

    private GitLatestRevisionCommandConsumer buildGitLatestRevisionCommandConsumer(String fileName) throws Exception {
        GitLatestRevisionCommandConsumer consumer = new GitLatestRevisionCommandConsumer();

        BufferedReader r = getGitLogBufferedReader(fileName);

        String line;

        while ((line = r.readLine()) != null) {
            // System.out.println(" line " + line );
            consumer.consumeLine(line);
        }
        return consumer;
    }

    private BufferedReader getGitLogBufferedReader(String fileName) throws Exception {
        File f = getTestFile(fileName);
        Reader reader = ReaderFactory.newReader(f, "UTF-8");
        return new BufferedReader(reader);
    }
}
