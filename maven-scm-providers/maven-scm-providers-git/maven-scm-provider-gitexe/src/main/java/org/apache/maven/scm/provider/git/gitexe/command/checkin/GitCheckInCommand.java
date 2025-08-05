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
package org.apache.maven.scm.provider.git.gitexe.command.checkin;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.gitexe.command.add.GitAddCommand;
import org.apache.maven.scm.provider.git.gitexe.command.branch.GitBranchCommand;
import org.apache.maven.scm.provider.git.gitexe.command.status.GitStatusCommand;
import org.apache.maven.scm.provider.git.gitexe.command.status.GitStatusConsumer;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.util.GitUtil;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Olivier Lamy
 *
 */
public class GitCheckInCommand extends AbstractCheckInCommand implements GitCommand<CheckInScmResult> {
    private final Map<String, String> environmentVariables;

    public GitCheckInCommand(Map<String, String> environmentVariables) {
        super();
        this.environmentVariables = environmentVariables;
    }

    /** {@inheritDoc} */
    protected CheckInScmResult executeCheckInCommand(
            ScmProviderRepository repo, ScmFileSet fileSet, String message, ScmVersion version) throws ScmException {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        int exitCode = -1;

        File messageFile = FileUtils.createTempFile("maven-scm-", ".commit", null);
        try {
            FileUtils.fileWrite(messageFile.getAbsolutePath(), "UTF-8", message);
        } catch (IOException ex) {
            return new CheckInScmResult(
                    null,
                    "Error while making a temporary file for the commit message: " + ex.getMessage(),
                    null,
                    false);
        }

        try {
            if (!fileSet.getFileList().isEmpty()) {
                // if specific fileSet is given, we have to git-add them first
                // otherwise we will use 'git-commit -a' later

                Commandline clAdd = null;

                // SCM-714: Workaround for the Windows terminal command limit
                if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                    for (File file : fileSet.getFileList()) {
                        clAdd = GitAddCommand.createCommandLine(fileSet.getBasedir(), Collections.singletonList(file));
                        exitCode = GitCommandLineUtils.execute(clAdd, stdout, stderr);

                        if (exitCode != 0) {
                            break;
                        }
                    }
                } else {
                    clAdd = GitAddCommand.createCommandLine(fileSet.getBasedir(), fileSet.getFileList());
                    exitCode = GitCommandLineUtils.execute(clAdd, stdout, stderr);
                }

                if (exitCode != 0) {
                    return new CheckInScmResult(
                            clAdd.toString(), "The git-add command failed.", stderr.getOutput(), false);
                }
            }

            // SCM-709: statusCommand uses repositoryRoot instead of workingDirectory, adjust it with
            // relativeRepositoryPath
            URI relativeRepositoryPath = GitStatusCommand.getRelativeCWD(logger, fileSet);

            // git-commit doesn't show single files, but only summary :/
            // so we must run git-status and consume the output
            // borrow a few things from the git-status command
            Commandline clStatus = GitStatusCommand.createCommandLine(repository, fileSet);

            GitStatusConsumer statusConsumer =
                    new GitStatusConsumer(fileSet.getBasedir(), relativeRepositoryPath, fileSet);
            exitCode = GitCommandLineUtils.execute(clStatus, statusConsumer, stderr);
            if (exitCode != 0) {
                // git-status returns non-zero if nothing to do
                if (logger.isInfoEnabled()) {
                    logger.info("nothing added to commit but untracked files present (use \"git add\" to " + "track)");
                }
            }

            if (statusConsumer.getChangedFiles().isEmpty()) {
                return new CheckInScmResult(null, statusConsumer.getChangedFiles());
            }

            Commandline clCommit = createCommitCommandLine(repository, fileSet, messageFile, environmentVariables);

            exitCode = GitCommandLineUtils.execute(clCommit, stdout, stderr);
            if (exitCode != 0) {
                return new CheckInScmResult(
                        clCommit.toString(), "The git-commit command failed.", stderr.getOutput(), false);
            }

            if (repo.isPushChanges()) {
                Commandline cl = createPushCommandLine(repository, fileSet, version);

                exitCode = GitCommandLineUtils.execute(cl, stdout, stderr);
                if (exitCode != 0) {
                    return new CheckInScmResult(
                            cl.toString(), "The git-push command failed.", stderr.getOutput(), false);
                }
            }

            List<ScmFile> checkedInFiles =
                    new ArrayList<>(statusConsumer.getChangedFiles().size());

            // rewrite all detected files to now have status 'checked_in'
            for (ScmFile changedFile : statusConsumer.getChangedFiles()) {
                ScmFile scmfile = new ScmFile(changedFile.getPath(), ScmFileStatus.CHECKED_IN);

                if (fileSet.getFileList().isEmpty()) {
                    checkedInFiles.add(scmfile);
                } else {
                    // if a specific fileSet is given, we have to check if the file is really tracked
                    for (File f : fileSet.getFileList()) {
                        if (FilenameUtils.separatorsToUnix(f.getPath()).equals(scmfile.getPath())) {
                            checkedInFiles.add(scmfile);
                        }
                    }
                }
            }

            return new CheckInScmResult(clCommit.toString(), checkedInFiles);
        } finally {
            try {
                FileUtils.forceDelete(messageFile);
            } catch (IOException ex) {
                // ignore
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public Commandline createPushCommandLine(
            GitScmProviderRepository repository, ScmFileSet fileSet, ScmVersion version) throws ScmException {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(
                fileSet.getBasedir(), "push", repository, environmentVariables);

        String branch = GitBranchCommand.getCurrentBranch(repository, fileSet);

        if (branch == null || branch.length() == 0) {
            throw new ScmException("Could not detect the current branch. Don't know where I should push to!");
        }

        cl.createArg().setValue(repository.getPushUrl());

        cl.createArg().setValue("refs/heads/" + branch + ":" + "refs/heads/" + branch);

        return cl;
    }

    public static Commandline createCommitCommandLine(
            GitScmProviderRepository repository, ScmFileSet fileSet, File messageFile) throws ScmException {
        return createCommitCommandLine(repository, fileSet, messageFile, Collections.emptyMap());
    }

    public static Commandline createCommitCommandLine(
            GitScmProviderRepository repository,
            ScmFileSet fileSet,
            File messageFile,
            Map<String, String> environmentVariables)
            throws ScmException {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(fileSet.getBasedir(), "commit");

        cl.createArg().setValue("--verbose");

        cl.createArg().setValue("-F");

        cl.createArg().setValue(messageFile.getAbsolutePath());

        if (fileSet.getFileList().isEmpty()) {
            // commit all tracked files
            cl.createArg().setValue("-a");
        }

        if (GitUtil.getSettings().isCommitNoVerify()) {
            cl.createArg().setValue("--no-verify");
        }

        return cl;
    }
}
