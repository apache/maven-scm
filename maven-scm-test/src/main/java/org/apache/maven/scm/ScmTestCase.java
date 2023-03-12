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
package org.apache.maven.scm;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

/**
 * Base class for all SCM tests. Consumers will typically
 * extend this class while tck test would extend ScmTckTestCase.
 * <br>
 * This class basically defines default locations for the
 * test environment and implements convenience methods.
 *
 * @author <a href="mailto:jason@maven.org">Jason van Zyl</a>
 *
 */
public abstract class ScmTestCase extends PlexusJUnit4TestSupport {
    protected static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone("GMT");

    private static boolean debugExecute;

    private ScmManager scmManager;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        deleteDirectory(getRepositoryRoot());
        assertFalse(getRepositoryRoot().exists());
        deleteDirectory(getWorkingCopy());
        assertFalse(getWorkingCopy().exists());
        deleteDirectory(getWorkingDirectory());
        assertFalse(getWorkingDirectory().exists());
        deleteDirectory(getAssertionCopy());
        assertFalse(getAssertionCopy().exists());
        deleteDirectory(getUpdatingCopy());
        assertFalse(getUpdatingCopy().exists());

        scmManager = null;
    }

    @Override
    protected void customizeContainerConfiguration(final ContainerConfiguration configuration) {
        configuration.setClassPathScanning(PlexusConstants.SCANNING_INDEX).setAutoWiring(true);
    }

    /**
     * @return default location of the test read/write repository
     */
    protected File getRepositoryRoot() {
        return PlexusJUnit4TestSupport.getTestFile("target/scm-test/repository");
    }

    /**
     * @return Location of the revisioned (read only) repository
     */
    protected File getRepository() {
        return PlexusJUnit4TestSupport.getTestFile("/src/test/repository");
    }

    /**
     * @return location of the working copy (always checkout)
     */
    protected File getWorkingCopy() {
        return PlexusJUnit4TestSupport.getTestFile("target/scm-test/working-copy");
    }

    /**
     * Legacy method - same as getWorkingCopy()
     *
     * @return location of the working copy (always checkout)
     */
    protected File getWorkingDirectory() {
        return getWorkingCopy();
    }

    /**
     * @return default location for doing assertions on a working tree
     */
    protected File getAssertionCopy() {
        return PlexusJUnit4TestSupport.getTestFile("target/scm-test/assertion-copy");
    }

    /**
     * @return default location for doing update operations on a working tree
     */
    protected File getUpdatingCopy() {
        return PlexusJUnit4TestSupport.getTestFile("target/scm-test/updating-copy");
    }

    protected ScmManager getScmManager() throws Exception {
        if (scmManager == null) {
            scmManager = lookup(ScmManager.class);
        }

        return scmManager;
    }

    protected ScmRepository makeScmRepository(String scmUrl) throws Exception {
        return getScmManager().makeScmRepository(scmUrl);
    }

    public void assertPath(String expectedPath, String actualPath) throws Exception {
        assertEquals(expectedPath.replace('\\', '/'), actualPath.replace('\\', '/'));
    }

    protected void assertFile(File root, String fileName) throws Exception {
        File file = new File(root, fileName);

        assertTrue("Missing file: '" + file.getAbsolutePath() + "'.", file.exists());

        assertTrue("File isn't a file: '" + file.getAbsolutePath() + "'.", file.isFile());

        String expected = fileName;

        String actual = FileUtils.fileRead(file);

        assertEquals(
                "The file doesn't contain the expected contents. File: " + file.getAbsolutePath(), expected, actual);
    }

    protected void assertResultIsSuccess(ScmResult result) {
        if (result.isSuccess()) {
            return;
        }

        printOutputError(result);

        fail("The command result success flag was false.");
    }

    protected void printOutputError(ScmResult result) {
        System.err.println("----------------------------------------------------------------------");
        System.err.println("Provider message");
        System.err.println("----------------------------------------------------------------------");
        System.err.println(result.getProviderMessage());
        System.err.println("----------------------------------------------------------------------");

        System.err.println("----------------------------------------------------------------------");
        System.err.println("Command output");
        System.err.println("----------------------------------------------------------------------");
        System.err.println(result.getCommandOutput());
        System.err.println("----------------------------------------------------------------------");
    }

    protected ScmFileSet getScmFileSet() {
        return new ScmFileSet(getWorkingCopy());
    }

    protected static void setDebugExecute(boolean debugExecute) {
        ScmTestCase.debugExecute = debugExecute;
    }

    /**
     * Execute the command line
     *
     * @param workingDirectory not null
     * @param executable       not null, should be a system command
     * @param arguments        not null
     * @throws Exception if any
     * @see CommandLineUtils#executeCommandLine(Commandline, org.codehaus.plexus.util.cli.StreamConsumer,
     *      org.codehaus.plexus.util.cli.StreamConsumer)
     */
    public static void execute(File workingDirectory, String executable, String arguments) throws Exception {
        Commandline cl = new Commandline();

        cl.setExecutable(executable);

        cl.setWorkingDirectory(workingDirectory.getAbsolutePath());

        cl.addArguments(CommandLineUtils.translateCommandline(arguments));

        StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        System.out.println("Test command line: " + cl);

        int exitValue = CommandLineUtils.executeCommandLine(cl, stdout, stderr);

        if (debugExecute || exitValue != 0) {
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
            fail("Exit value wasn't 0, was:" + exitValue);
        }
    }

    protected static void makeDirectory(File basedir, String fileName) {
        File dir = new File(basedir, fileName);

        if (!dir.exists()) {
            assertTrue(dir.mkdirs());
        }
    }

    protected static void makeFile(File basedir, String fileName) throws IOException {
        makeFile(basedir, fileName, fileName);
    }

    public static void makeFile(File basedir, String fileName, String contents) throws IOException {
        File file = new File(basedir, fileName);

        File parent = file.getParentFile();

        if (!parent.exists()) {
            assertTrue(parent.mkdirs());
        }

        try (FileWriter writer = new FileWriter(file)) {
            writer.write(contents);
        }
    }

    protected void deleteDirectory(File directory) throws IOException {
        FileUtils.deleteDirectory(directory);
    }

    public static Date getDate(int year, int month, int day) {
        return getDate(year, month, day, 0, 0, 0, null);
    }

    protected static Date getDate(int year, int month, int day, TimeZone tz) {
        return getDate(year, month, day, 0, 0, 0, tz);
    }

    protected static Date getDate(int year, int month, int day, int hourOfDay, int minute, int second, TimeZone tz) {
        Calendar cal = Calendar.getInstance();

        if (tz != null) {
            cal.setTimeZone(tz);
        }
        cal.set(year, month, day, hourOfDay, minute, second);
        cal.set(Calendar.MILLISECOND, 0);

        return cal.getTime();
    }

    public void assertCommandLine(String expectedCommand, File expectedWorkingDirectory, Commandline actualCommand)
            throws IOException {
        Commandline cl = new Commandline(expectedCommand);
        if (expectedWorkingDirectory != null) {
            cl.setWorkingDirectory(expectedWorkingDirectory.getAbsolutePath());
        }
        String expectedCommandLineAsExecuted = StringUtils.join(cl.getShellCommandline(), " ");
        String actualCommandLineAsExecuted = StringUtils.join(actualCommand.getShellCommandline(), " ");
        assertEquals(expectedCommandLineAsExecuted, actualCommandLineAsExecuted);
    }

    public static void checkScmPresence(String scmProviderCommand) {
        assumeTrue(
                "Skipping tests because the required command '" + scmProviderCommand + "' is not available.",
                ScmTestCase.isSystemCmd(scmProviderCommand));
    }

    /**
     * @param cmd the executable to run, not null.
     * @return true if and only if the command is on the path
     */
    public static boolean isSystemCmd(String cmd) {
        try {
            Runtime.getRuntime().exec(cmd);

            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void printSystemCmdUnavail(String cmd, String testName) {
        System.err.printf("'%s' is not a system command. Ignored %s.%n", cmd, testName);
    }
}
