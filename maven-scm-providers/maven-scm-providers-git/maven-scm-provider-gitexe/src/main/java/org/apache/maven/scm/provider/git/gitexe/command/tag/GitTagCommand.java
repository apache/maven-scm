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
package org.apache.maven.scm.provider.git.gitexe.command.tag;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.gitexe.command.list.GitListCommand;
import org.apache.maven.scm.provider.git.gitexe.command.list.GitListConsumer;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 *
 */
public class GitTagCommand extends AbstractTagCommand implements GitCommand {
    private final Map<String, String> environmentVariables;

    public GitTagCommand(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public ScmResult executeTagCommand(ScmProviderRepository repo, ScmFileSet fileSet, String tag, String message)
            throws ScmException {
        return executeTagCommand(repo, fileSet, tag, new ScmTagParameters(message));
    }

    /** {@inheritDoc} */
    public ScmResult executeTagCommand(
            ScmProviderRepository repo, ScmFileSet fileSet, String tag, ScmTagParameters scmTagParameters)
            throws ScmException {
        if (tag == null || tag.trim().isEmpty()) {
            throw new ScmException("tag name must be specified");
        }

        if (!fileSet.getFileList().isEmpty()) {
            throw new ScmException("This provider doesn't support tagging subsets of a directory");
        }

        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        File messageFile = FileUtils.createTempFile("maven-scm-", ".commit", null);

        try {
            FileUtils.fileWrite(messageFile.getAbsolutePath(), "UTF-8", scmTagParameters.getMessage());
        } catch (IOException ex) {
            return new TagScmResult(
                    null,
                    "Error while making a temporary file for the commit message: " + ex.getMessage(),
                    null,
                    false);
        }

        try {
            CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
            CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

            int exitCode;

            Commandline clTag = createCommandLine(
                    repository,
                    fileSet.getBasedir(),
                    tag,
                    messageFile,
                    scmTagParameters.isSign(),
                    scmTagParameters.isForceNoSign());

            exitCode = GitCommandLineUtils.execute(clTag, stdout, stderr);
            if (exitCode != 0) {
                return new TagScmResult(clTag.toString(), "The git-tag command failed.", stderr.getOutput(), false);
            }

            if (repo.isPushChanges()) {
                // and now push the tag to the configured upstream repository
                Commandline clPush = createPushCommandLine(repository, fileSet, tag);

                exitCode = GitCommandLineUtils.execute(clPush, stdout, stderr);
                if (exitCode != 0) {
                    return new TagScmResult(
                            clPush.toString(), "The git-push command failed.", stderr.getOutput(), false);
                }
            }

            // plus search for the tagged files
            GitListConsumer listConsumer = new GitListConsumer(fileSet.getBasedir(), ScmFileStatus.TAGGED);

            Commandline clList = GitListCommand.createCommandLine(repository, fileSet.getBasedir());

            exitCode = GitCommandLineUtils.execute(clList, listConsumer, stderr);
            if (exitCode != 0) {
                return new CheckOutScmResult(
                        clList.toString(), "The git-ls-files command failed.", stderr.getOutput(), false);
            }

            return new TagScmResult(clTag.toString(), listConsumer.getListedFiles());
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

    static Commandline createCommandLine(
            GitScmProviderRepository repository,
            File workingDirectory,
            String tag,
            File messageFile,
            boolean sign,
            boolean forceNoSign) {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(workingDirectory, "tag");

        if (sign) {
            cl.createArg().setValue("-s");
        }
        if (forceNoSign) {
            cl.createArg().setValue("--no-sign");
        }

        cl.createArg().setValue("-F");
        cl.createArg().setValue(messageFile.getAbsolutePath());

        // Note: this currently assumes you have the tag base checked out too
        cl.createArg().setValue(tag);

        return cl;
    }

    public Commandline createPushCommandLine(GitScmProviderRepository repository, ScmFileSet fileSet, String tag) {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(
                fileSet.getBasedir(), "push", repository, environmentVariables);

        cl.createArg().setValue(repository.getPushUrl());
        cl.createArg().setValue("refs/tags/" + tag);

        return cl;
    }
}
