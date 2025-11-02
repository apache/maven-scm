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
package org.apache.maven.scm.provider.svn;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.jupiter.api.Assertions;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public final class SvnScmTestUtils {
    /** 'svn' command line. */
    public static final String SVN_COMMAND_LINE = "svn";

    /** 'svnadmin' command line. */
    public static final String SVNADMIN_COMMAND_LINE = "svnadmin";

    private SvnScmTestUtils() {}

    public static void initializeRepository(File repositoryRoot) throws Exception {
        if (repositoryRoot.exists()) {
            FileUtils.deleteDirectory(repositoryRoot);
        }

        Assertions.assertFalse(repositoryRoot.exists(), "repositoryRoot still exists");

        Assertions.assertTrue(
                repositoryRoot.mkdirs(),
                "Could not make repository root directory: " + repositoryRoot.getAbsolutePath());

        ScmTestCase.execute(
                repositoryRoot.getParentFile(), SVNADMIN_COMMAND_LINE, "create " + repositoryRoot.getName());

        loadSvnDump(
                repositoryRoot,
                new SvnScmTestUtils().getClass().getClassLoader().getResourceAsStream("tck/tck.dump"));
    }

    public static void initializeRepository(File repositoryRoot, File dump) throws Exception {
        if (repositoryRoot.exists()) {
            FileUtils.deleteDirectory(repositoryRoot);
        }

        Assertions.assertTrue(
                repositoryRoot.mkdirs(),
                "Could not make repository root directory: " + repositoryRoot.getAbsolutePath());

        ScmTestCase.execute(
                repositoryRoot.getParentFile(), SVNADMIN_COMMAND_LINE, "create " + repositoryRoot.getName());

        Assertions.assertTrue(dump.exists(), "The dump file doesn't exist: " + dump.getAbsolutePath());

        loadSvnDump(repositoryRoot, new FileInputStream(dump));
    }

    private static void loadSvnDump(File repositoryRoot, InputStream dumpStream) throws Exception {
        Commandline cl = new Commandline();

        cl.setExecutable(SVNADMIN_COMMAND_LINE);

        cl.setWorkingDirectory(repositoryRoot.getParentFile().getAbsolutePath());

        cl.createArg().setValue("load");

        cl.createArg().setValue(repositoryRoot.getAbsolutePath());

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitValue = CommandLineUtils.executeCommandLine(cl, dumpStream, stdout, stderr);

        if (exitValue != 0) {
            System.err.println("-----------------------------------------");
            System.err.println("Command line: " + cl);
            System.err.println("Working directory: " + cl.getWorkingDirectory());
            System.err.println("-----------------------------------------");
            System.err.println("Standard output: ");
            System.err.println("-----------------------------------------");
            System.err.println(stdout.getOutput());
            System.err.println("-----------------------------------------");

            System.err.println("Standard error: ");
            System.err.println("-----------------------------------------");
            System.err.println(stderr.getOutput());
            System.err.println("-----------------------------------------");
        }

        if (exitValue != 0) {
            Assertions.fail("Exit value wasn't 0, was:" + exitValue);
        }
    }

    public static String getScmUrl(File repositoryRootFile) throws CommandLineException {
        return "scm:svn:" + repositoryRootFile.toPath().toAbsolutePath().toUri().toASCIIString();
    }
}
