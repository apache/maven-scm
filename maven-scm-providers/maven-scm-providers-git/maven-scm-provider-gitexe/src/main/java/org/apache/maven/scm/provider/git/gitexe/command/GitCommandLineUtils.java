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
package org.apache.maven.scm.provider.git.gitexe.command;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.util.GitUtil;
import org.apache.maven.scm.providers.gitlib.settings.Settings;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command line construction utility.
 *
 * @author Brett Porter
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 *
 */
public final class GitCommandLineUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(GitCommandLineUtils.class);

    // https://git-scm.com/docs/git#Documentation/git.txt-codeGITSSHCOMMANDcode, requires git 2.3.0 or newer
    public static final String VARIABLE_GIT_SSH_COMMAND = "GIT_SSH_COMMAND";

    private GitCommandLineUtils() {}

    public static void addTarget(Commandline commandLine, List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        final File workingDirectory = commandLine.getWorkingDirectory();
        try {
            final String canonicalWorkingDirectory = workingDirectory.getCanonicalPath();
            for (File file : files) {
                String relativeFile = file.getPath();

                final String canonicalFile = file.getCanonicalPath();
                if (canonicalFile.startsWith(canonicalWorkingDirectory)) {
                    // so we can omit the starting characters
                    relativeFile = canonicalFile.substring(canonicalWorkingDirectory.length());

                    if (relativeFile.startsWith(File.separator)) {
                        relativeFile = relativeFile.substring(File.separator.length());
                    }
                }

                // no setFile() since this screws up the working directory!
                commandLine.createArg().setValue(FilenameUtils.separatorsToUnix(relativeFile));
            }
        } catch (IOException ex) {
            throw new IllegalArgumentException(
                    "Could not get canonical paths for workingDirectory = " + workingDirectory + " or files=" + files,
                    ex);
        }
    }

    /**
     * Use this only for commands not requiring environment variables (i.e. local commands).
     */
    public static Commandline getBaseGitCommandLine(File workingDirectory, String command) {
        return getBaseGitCommandLine(workingDirectory, command, null, null);
    }

    /**
     * Use this for commands requiring environment variables (i.e. remote commands).
     */
    public static Commandline getBaseGitCommandLine(
            File workingDirectory,
            String command,
            GitScmProviderRepository repository,
            Map<String, String> environment) {
        Commandline commandLine = getAnonymousBaseGitCommandLine(workingDirectory, command);
        if (repository != null) {
            prepareEnvVariablesForRepository(repository, environment).forEach(commandLine::addEnvironment);
        } else if (environment != null) {
            environment.forEach(commandLine::addEnvironment);
        }
        return commandLine;
    }

    /**
     * Creates a {@link Commandline} for which toString() does not display
     * the password.
     *
     * @param workingDirectory
     * @param command
     * @return CommandLine with anonymous output
     */
    private static Commandline getAnonymousBaseGitCommandLine(File workingDirectory, String command) {
        if (command == null || command.isEmpty()) {
            return null;
        }

        Commandline commandLine = new AnonymousCommandLine();

        composeCommand(workingDirectory, command, commandLine);

        return commandLine;
    }

    private static void composeCommand(File workingDirectory, String command, Commandline commandLine) {
        Settings settings = GitUtil.getSettings();

        commandLine.setExecutable(settings.getGitCommand());

        commandLine.createArg().setValue(command);

        if (workingDirectory != null) {
            commandLine.setWorkingDirectory(workingDirectory.getAbsolutePath());
        }
    }

    public static int execute(
            Commandline commandline, StreamConsumer consumer, CommandLineUtils.StringStreamConsumer stderr)
            throws ScmException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Executing: " + commandline);
            LOGGER.info(
                    "Working directory: " + commandline.getWorkingDirectory().getAbsolutePath());
        }

        int exitCode;
        try {
            exitCode = CommandLineUtils.executeCommandLine(commandline, consumer, stderr);
        } catch (CommandLineException ex) {
            throw new ScmException("Error while executing command.", ex);
        }

        return exitCode;
    }

    public static int execute(
            Commandline commandLine,
            CommandLineUtils.StringStreamConsumer stdout,
            CommandLineUtils.StringStreamConsumer stderr)
            throws ScmException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Executing: " + commandLine);
            LOGGER.info(
                    "Working directory: " + commandLine.getWorkingDirectory().getAbsolutePath());
        }

        int exitCode;
        try {
            exitCode = CommandLineUtils.executeCommandLine(commandLine, stdout, stderr);
        } catch (CommandLineException ex) {
            throw new ScmException("Error while executing command.", ex);
        }

        return exitCode;
    }

    static Map<String, String> prepareEnvVariablesForRepository(
            GitScmProviderRepository repository, Map<String, String> environmentVariables) {
        Map<String, String> effectiveEnvironmentVariables = new HashMap<>();
        if (environmentVariables != null) {
            effectiveEnvironmentVariables.putAll(environmentVariables);
        }
        if (StringUtils.isNotBlank(repository.getPrivateKey())) {
            if (effectiveEnvironmentVariables.putIfAbsent(
                            VARIABLE_GIT_SSH_COMMAND,
                            "ssh -o IdentitiesOnly=yes -i "
                                    + FilenameUtils.separatorsToUnix(repository.getPrivateKey()))
                    != null) {
                LOGGER.warn(
                        "Ignore GitScmProviderRepository.privateKey as environment variable {} is already set",
                        VARIABLE_GIT_SSH_COMMAND);
            }
        }
        if (StringUtils.isNotBlank(repository.getPassphrase())) {
            LOGGER.warn("GitScmProviderRepository.passphrase currently not supported by provider 'git'");
        }
        return effectiveEnvironmentVariables;
    }
}
