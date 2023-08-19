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
package org.apache.maven.scm.provider.git.gitexe.command.add;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Test;

/**
 * Check if the {@code GitAddCommand#createCommandLine(File, List)} returns the correct
 * command line execution string.
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 *
 */
public class GitExeAddCommandTest extends ScmTestCase {

    @Test
    public void testAddCommandSingleFile() throws Exception {
        List<File> files = new ArrayList<>();

        files.add(new File("myFile.java"));

        testCommandLine("scm:git:http://foo.com/git", files, "git add -- myFile.java");
    }

    @Test
    public void testAddCommandMultipleFiles() throws Exception {
        List<File> files = new ArrayList<>();

        files.add(new File("myFile.java"));
        files.add(new File("myFile2.java"));
        files.add(new File("myFile3.java"));

        testCommandLine("scm:git:http://foo.com/git", files, "git add -- myFile.java myFile2.java myFile3.java");
    }

    // ----------------------------------------------------------------------
    // private helper functions
    // ----------------------------------------------------------------------

    private void testCommandLine(String scmUrl, List<File> files, String commandLine) throws Exception {
        File workingDirectory = getTestFile("target/git-add-command-test");

        Commandline cl = GitAddCommand.createCommandLine(workingDirectory, files);

        assertCommandLine(commandLine, workingDirectory, cl);
    }
}
