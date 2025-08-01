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
package org.apache.maven.scm.provider.git.gitexe.command.checkout;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.gitexe.command.list.GitListCommand;
import org.apache.maven.scm.provider.git.gitexe.command.list.GitListConsumer;
import org.apache.maven.scm.provider.git.gitexe.command.remoteinfo.GitRemoteInfoCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 *
 */
public class GitCheckOutCommand extends AbstractCheckOutCommand implements GitCommand<CheckOutScmResult> {
    private final Map<String, String> environmentVariables;

    public GitCheckOutCommand(Map<String, String> environmentVariables) {
        super();
        this.environmentVariables = environmentVariables;
    }

    /**
     * For git, the given repository is a remote one.
     * We have to clone it first if the working directory does not contain a git repo yet,
     * otherwise we have to git-pull it.
     * <p>
     * TODO We currently assume a '.git' directory, so this does not work for --bare repos
     * {@inheritDoc}
     */
    @Override
    public CheckOutScmResult executeCommand(
            ScmProviderRepository repo, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {
        ScmVersion version = parameters.getScmVersion(CommandParameter.SCM_VERSION, null);
        boolean binary = parameters.getBoolean(CommandParameter.BINARY, false);
        boolean shallow = parameters.getBoolean(CommandParameter.SHALLOW, false);

        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        if (GitScmProviderRepository.PROTOCOL_FILE.equals(
                        repository.getFetchInfo().getProtocol())
                && repository
                                .getFetchInfo()
                                .getPath()
                                .indexOf(fileSet.getBasedir().getPath())
                        >= 0) {
            throw new ScmException("remote repository must not be the working directory");
        }

        int exitCode;

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        String lastCommandLine = "git-nothing-to-do";

        if (!fileSet.getBasedir().exists() || !(new File(fileSet.getBasedir(), ".git").exists())) {
            if (fileSet.getBasedir().exists()) {
                // git refuses to clone otherwise
                fileSet.getBasedir().delete();
            }

            // no git repo seems to exist, let's clone the original repo
            Commandline gitClone = createCloneCommand(repository, fileSet.getBasedir(), version, binary, shallow);

            exitCode = GitCommandLineUtils.execute(gitClone, stdout, stderr);
            if (exitCode != 0) {
                return new CheckOutScmResult(
                        gitClone.toString(), "The git clone command failed.", stderr.getOutput(), false);
            }
            lastCommandLine = gitClone.toString();
        }

        GitRemoteInfoCommand gitRemoteInfoCommand = new GitRemoteInfoCommand(environmentVariables);

        RemoteInfoScmResult result = gitRemoteInfoCommand.executeRemoteInfoCommand(repository, null, null);

        if (fileSet.getBasedir().exists()
                && new File(fileSet.getBasedir(), ".git").exists()
                && result.getBranches().size() > 0) {
            // git repo exists, so we must git-pull the changes
            Commandline gitPull = createPullCommand(repository, fileSet.getBasedir(), version);

            exitCode = GitCommandLineUtils.execute(gitPull, stdout, stderr);
            if (exitCode != 0) {
                return new CheckOutScmResult(
                        gitPull.toString(), "The git pull command failed.", stderr.getOutput(), false);
            }

            // and now let's do the git-checkout itself
            Commandline gitCheckout = createCommandLine(repository, fileSet.getBasedir(), version);

            exitCode = GitCommandLineUtils.execute(gitCheckout, stdout, stderr);
            if (exitCode != 0) {
                return new CheckOutScmResult(
                        gitCheckout.toString(), "The git checkout command failed.", stderr.getOutput(), false);
            }
            lastCommandLine = gitCheckout.toString();
        }

        // and now search for the files
        GitListConsumer listConsumer = new GitListConsumer(fileSet.getBasedir(), ScmFileStatus.CHECKED_IN);

        Commandline gitList = GitListCommand.createCommandLine(repository, fileSet.getBasedir());

        exitCode = GitCommandLineUtils.execute(gitList, listConsumer, stderr);
        if (exitCode != 0) {
            return new CheckOutScmResult(
                    gitList.toString(), "The git ls-files command failed.", stderr.getOutput(), false);
        }

        return new CheckOutScmResult(lastCommandLine, listConsumer.getListedFiles());
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine(
            GitScmProviderRepository repository, File workingDirectory, ScmVersion version) {
        Commandline gitCheckout = GitCommandLineUtils.getBaseGitCommandLine(workingDirectory, "checkout");

        if (version != null && StringUtils.isNotEmpty(version.getName())) {
            gitCheckout.createArg().setValue(version.getName());
        }

        return gitCheckout;
    }

    /**
     * create a git-clone repository command
     */
    private Commandline createCloneCommand(
            GitScmProviderRepository repository,
            File workingDirectory,
            ScmVersion version,
            boolean binary,
            boolean shallow) {
        Commandline gitClone = GitCommandLineUtils.getBaseGitCommandLine(
                workingDirectory.getParentFile(), "clone", repository, environmentVariables);

        forceBinary(gitClone, binary);

        if (shallow) {
            gitClone.createArg().setValue("--depth");

            gitClone.createArg().setValue("1");
        }

        if (version != null && (version instanceof ScmBranch)) {

            gitClone.createArg().setValue("--branch");

            gitClone.createArg().setValue(version.getName());
        }

        gitClone.createArg().setValue(repository.getFetchUrl());

        gitClone.createArg().setValue(workingDirectory.getName());

        return gitClone;
    }

    private void forceBinary(Commandline commandLine, boolean binary) {
        if (binary) {
            commandLine.createArg().setValue("-c");
            commandLine.createArg().setValue("core.autocrlf=false");
        }
    }

    /**
     * Create a git fetch or git pull repository command.
     */
    private Commandline createPullCommand(
            GitScmProviderRepository repository, File workingDirectory, ScmVersion version) {

        if (version != null && StringUtils.isNotEmpty(version.getName())) {
            if (version instanceof ScmTag) {
                // A tag will not be pulled but we only fetch all the commits from the upstream repo
                // This is done because checking out a tag might not happen on the current branch
                // but create a 'detached HEAD'.
                // In fact, a tag in git may be in multiple branches. This occurs if
                // you create a branch after the tag has been created
                Commandline gitFetch = GitCommandLineUtils.getBaseGitCommandLine(
                        workingDirectory, "fetch", repository, environmentVariables);

                gitFetch.createArg().setValue(repository.getFetchUrl());
                return gitFetch;
            } else {
                Commandline gitPull = GitCommandLineUtils.getBaseGitCommandLine(
                        workingDirectory, "pull", repository, environmentVariables);
                gitPull.createArg().setValue(repository.getFetchUrl());
                gitPull.createArg().setValue(version.getName() + ":" + version.getName());
                return gitPull;
            }
        } else {
            Commandline gitPull = GitCommandLineUtils.getBaseGitCommandLine(
                    workingDirectory, "pull", repository, environmentVariables);
            gitPull.createArg().setValue(repository.getFetchUrl());
            gitPull.createArg().setValue("master");
            return gitPull;
        }
    }

    /**
     * The overridden {@link #executeCommand(ScmProviderRepository, ScmFileSet, CommandParameters)} in this class will
     * not call this method!
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected CheckOutScmResult executeCheckOutCommand(
            ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion version, boolean recursive, boolean shallow)
            throws ScmException {
        throw new UnsupportedOperationException("Should not get here");
    }
}
