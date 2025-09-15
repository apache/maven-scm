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
package org.apache.maven.scm.provider.hg;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.command.inventory.HgChangeSet;
import org.apache.maven.scm.provider.hg.command.inventory.HgOutgoingConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common code for executing hg commands.
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public final class HgUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HgUtils.class);

    public static final String DEFAULT = "default";

    private HgUtils() {
        // no op
    }

    /**
     * Map between command and its valid exit codes.
     */
    private static final Map<String, List<Integer>> EXIT_CODE_MAP = new HashMap<>();

    /**
     * Default exit codes for entries not in exitCodeMap.
     */
    private static final List<Integer> DEFAULT_EXIT_CODES = new ArrayList<>();

    /** Setup exit codes. */
    static {
        DEFAULT_EXIT_CODES.add(Integer.valueOf(0));

        // Diff is different
        List<Integer> diffExitCodes = new ArrayList<>(3);
        diffExitCodes.add(Integer.valueOf(0)); // No difference
        diffExitCodes.add(Integer.valueOf(1)); // Conflicts in merge-like or changes in diff-like
        diffExitCodes.add(Integer.valueOf(2)); // Unrepresentable diff changes
        EXIT_CODE_MAP.put(HgCommandConstants.DIFF_CMD, diffExitCodes);
        // Outgoing is different
        List<Integer> outgoingExitCodes = new ArrayList<>(2);
        outgoingExitCodes.add(Integer.valueOf(0)); // There are changes
        outgoingExitCodes.add(Integer.valueOf(1)); // No changes
        EXIT_CODE_MAP.put(HgCommandConstants.OUTGOING_CMD, outgoingExitCodes);
    }

    public static ScmResult execute(HgConsumer consumer, File workingDir, String[] cmdAndArgs) throws ScmException {
        try {
            // Build commandline
            Commandline cmd = buildCmd(workingDir, cmdAndArgs);
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("EXECUTING: " + maskPassword(cmd));
            }

            // Execute command
            int exitCode = executeCmd(consumer, cmd);

            // Return result
            List<Integer> exitCodes = DEFAULT_EXIT_CODES;
            if (EXIT_CODE_MAP.containsKey(cmdAndArgs[0])) {
                exitCodes = EXIT_CODE_MAP.get(cmdAndArgs[0]);
            }
            boolean success = exitCodes.contains(Integer.valueOf(exitCode));

            // On failure (and not due to exceptions) - run diagnostics
            String providerMsg = "Execution of hg command succeded";
            if (!success) {
                HgConfig config = new HgConfig(workingDir);
                providerMsg = "\nEXECUTION FAILED" + "\n  Execution of cmd : " + cmdAndArgs[0]
                        + " failed with exit code: "
                        + exitCode + "." + "\n  Working directory was: " + "\n    " + workingDir.getAbsolutePath()
                        + config.toString(workingDir) + "\n";
                if (LOGGER.isErrorEnabled()) {
                    LOGGER.error(providerMsg);
                }
            }

            return new ScmResult(cmd.toString(), providerMsg, consumer.getStdErr(), success);
        } catch (ScmException se) {
            String msg = "EXECUTION FAILED" + "\n  Execution failed before invoking the Hg command. Last exception:"
                    + "\n    " + se.getMessage();

            // Add nested cause if any
            if (se.getCause() != null) {
                msg += "\n  Nested exception:" + "\n    " + se.getCause().getMessage();
            }

            // log and return
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error(msg);
            }
            throw se;
        }
    }

    static Commandline buildCmd(File workingDir, String[] cmdAndArgs) throws ScmException {
        Commandline cmd = new Commandline();
        cmd.setExecutable(HgCommandConstants.EXEC);
        cmd.addArguments(cmdAndArgs);
        if (workingDir != null) {
            cmd.setWorkingDirectory(workingDir.getAbsolutePath());

            if (!workingDir.exists()) {
                boolean success = workingDir.mkdirs();
                if (!success) {
                    String msg = "Working directory did not exist" + " and it couldn't be created: " + workingDir;
                    throw new ScmException(msg);
                }
            }
        }
        return cmd;
    }

    static int executeCmd(HgConsumer consumer, Commandline cmd) throws ScmException {
        final int exitCode;
        try {
            exitCode = CommandLineUtils.executeCommandLine(cmd, consumer, consumer);
        } catch (CommandLineException ex) {
            throw new ScmException("Command could not be executed: " + cmd, ex);
        }
        return exitCode;
    }

    public static ScmResult execute(File workingDir, String[] cmdAndArgs) throws ScmException {
        return execute(new HgConsumer(), workingDir, cmdAndArgs);
    }

    public static String[] expandCommandLine(String[] cmdAndArgs, ScmFileSet additionalFiles) {
        List<File> filesList = additionalFiles.getFileList();
        String[] cmd = new String[filesList.size() + cmdAndArgs.length];

        // Copy command into array
        System.arraycopy(cmdAndArgs, 0, cmd, 0, cmdAndArgs.length);

        // Add files as additional parameter into the array
        int i = 0;
        for (File scmFile : filesList) {
            String file = scmFile.getPath().replace('\\', File.separatorChar);
            cmd[i + cmdAndArgs.length] = file;
            i++;
        }

        return cmd;
    }

    public static int getCurrentRevisionNumber(File workingDir) throws ScmException {

        String[] revCmd = new String[] {HgCommandConstants.REVNO_CMD};
        HgRevNoConsumer consumer = new HgRevNoConsumer();
        HgUtils.execute(consumer, workingDir, revCmd);

        return consumer.getCurrentRevisionNumber();
    }

    public static String getCurrentBranchName(File workingDir) throws ScmException {
        String[] branchnameCmd = new String[] {HgCommandConstants.BRANCH_NAME_CMD};
        HgBranchnameConsumer consumer = new HgBranchnameConsumer();
        HgUtils.execute(consumer, workingDir, branchnameCmd);
        return consumer.getBranchName();
    }

    /**
     * Get current (working) revision.
     * <p>
     * Resolve revision to the last integer found in the command output.
     */
    private static class HgRevNoConsumer extends HgConsumer {

        private int revNo;

        public void doConsume(ScmFileStatus status, String line) {
            try {
                revNo = Integer.valueOf(line).intValue();
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        int getCurrentRevisionNumber() {
            return revNo;
        }
    }

    /**
     * Get current (working) branch name.
     */
    private static class HgBranchnameConsumer extends HgConsumer {

        private String branchName;

        public void doConsume(ScmFileStatus status, String trimmedLine) {
            branchName = String.valueOf(trimmedLine);
        }

        String getBranchName() {
            return branchName;
        }

        /**
         * {@inheritDoc}
         */
        public void consumeLine(String line) {
            if (logger.isDebugEnabled()) {
                logger.debug(line);
            }
            String trimmedLine = line.trim();

            doConsume(null, trimmedLine);
        }
    }

    /**
     * Check if there are outgoing changes on a different branch. If so, Mercurial default behaviour
     * is to block the push and warn using a 'push creates new remote branch !' message.
     * We also warn, and return true if a different outgoing branch was found
     * <p>
     * Method users should not stop the push on a negative return, instead, they should
     * hg push -r(branch being released)
     *
     * @param workingDir        the working dir
     * @param workingbranchName the working branch name
     * @return true if a different outgoing branch was found
     * @throws ScmException on outgoing command error
     */
    public static boolean differentOutgoingBranchFound(File workingDir, String workingbranchName) throws ScmException {
        String[] outCmd = new String[] {HgCommandConstants.OUTGOING_CMD};
        HgOutgoingConsumer outConsumer = new HgOutgoingConsumer();
        ScmResult outResult = HgUtils.execute(outConsumer, workingDir, outCmd);
        List<HgChangeSet> changes = outConsumer.getChanges();
        if (outResult.isSuccess()) {
            for (HgChangeSet set : changes) {
                if (!getBranchName(workingbranchName).equals(getBranchName(set.getBranch()))) {
                    LOGGER.warn("A different branch than " + getBranchName(workingbranchName)
                            + " was found in outgoing changes, branch name was " + getBranchName(set.getBranch())
                            + ". Only local branch named " + getBranchName(workingbranchName) + " will be pushed.");
                    return true;
                }
            }
        }
        return false;
    }

    private static String getBranchName(String branch) {
        return branch == null ? DEFAULT : branch;
    }

    public static String maskPassword(Commandline cl) {
        String clString = cl.toString();

        int pos = clString.indexOf('@');

        if (pos > 0) {
            clString = clString.replaceAll(":\\w+@", ":*****@");
        }

        return clString;
    }
}
