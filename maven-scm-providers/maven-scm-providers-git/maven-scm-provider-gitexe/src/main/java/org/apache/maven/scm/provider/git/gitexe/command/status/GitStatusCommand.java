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
package org.apache.maven.scm.provider.git.gitexe.command.status;

import java.net.URI;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class GitStatusCommand extends AbstractStatusCommand implements GitCommand<StatusScmResult> {
    /** {@inheritDoc} */
    protected StatusScmResult executeStatusCommand(ScmProviderRepository repo, ScmFileSet fileSet) throws ScmException {
        int exitCode;
        CommandLineUtils.StringStreamConsumer stderr;

        URI relativeRepositoryPath = getRelativeCWD(logger, fileSet);

        Commandline cl = createCommandLine((GitScmProviderRepository) repo, fileSet);

        GitStatusConsumer consumer = new GitStatusConsumer(fileSet.getBasedir(), relativeRepositoryPath, fileSet);

        stderr = new CommandLineUtils.StringStreamConsumer();

        exitCode = GitCommandLineUtils.execute(cl, consumer, stderr);
        if (exitCode != 0) {
            // git-status returns non-zero if nothing to do
            if (logger.isInfoEnabled()) {
                logger.info("nothing added to commit but untracked files present (use \"git add\" to track)");
            }
        }

        return new StatusScmResult(cl.toString(), consumer.getChangedFiles());
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Get the dir relative to the repository root.
     *
     * @param logger the caller command logger.
     * @param fileSet in which subdir to execute.
     * @return the relative URI.
     * @throws ScmException if execute() fails.
     */
    public static URI getRelativeCWD(Logger logger, ScmFileSet fileSet) throws ScmException {
        Commandline clRevparse = createRevparseShowPrefix(fileSet);

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        URI relativeRepositoryPath = null;

        int exitCode = GitCommandLineUtils.execute(clRevparse, stdout, stderr);
        if (exitCode != 0) {
            // git-status returns non-zero if nothing to do
            if (logger.isInfoEnabled()) {
                logger.info("Could not resolve prefix");
            }
        } else {
            relativeRepositoryPath =
                    GitStatusConsumer.uriFromPath(stdout.getOutput().trim());
        }
        return relativeRepositoryPath;
    }

    public static Commandline createCommandLine(GitScmProviderRepository repository, ScmFileSet fileSet) {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(fileSet.getBasedir(), "status");
        cl.addArguments(new String[] {"--porcelain", "."});
        return cl;
    }

    public static Commandline createRevparseShowPrefix(ScmFileSet fileSet) {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(fileSet.getBasedir(), "rev-parse");
        cl.addArguments(new String[] {"--show-prefix"});
        return cl;
    }
}
