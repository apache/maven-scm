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

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.ReaderFactory;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitDiffRawConsumerTest extends ScmTestCase {
    @Test
    public void testUpToDate() throws Exception {
        GitDiffRawConsumer consumer = buildGitDiffRawConsumer("/src/test/resources/git/diff/git-diff-raw_long.out");

        List<ScmFile> changedFiles = consumer.getChangedFiles();

        assertEquals(62, changedFiles.size());
    }

    // ----------------------------------------------------------------------
    // private helper functions
    // ----------------------------------------------------------------------

    private GitDiffRawConsumer buildGitDiffRawConsumer(String fileName) throws Exception {
        GitDiffRawConsumer consumer = new GitDiffRawConsumer();

        File f = getTestFile(fileName);
        Reader reader = ReaderFactory.newReader(f, "UTF-8");
        BufferedReader r = new BufferedReader(reader);

        String line;

        while ((line = r.readLine()) != null) {
            // System.out.println(" line " + line );
            consumer.consumeLine(line);
        }
        return consumer;
    }
}
