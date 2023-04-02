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
package org.apache.maven.scm.provider.git.gitexe.command.untag;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmUntagParameters;
import org.apache.maven.scm.command.untag.AbstractUntagCommand;
import org.apache.maven.scm.command.untag.UntagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/** {@inheritDoc} */
public class GitUntagCommand extends AbstractUntagCommand implements GitCommand {

    /** {@inheritDoc} */
    public ScmResult executeUntagCommand(
            ScmProviderRepository repo, ScmFileSet fileSet, ScmUntagParameters scmUntagParameters) throws ScmException {
        String tag = scmUntagParameters.getTag();
        if (tag == null || StringUtils.isEmpty(tag.trim())) {
            throw new ScmException("tag name must be specified");
        }

        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        Commandline clTag = createCommandLine(repository, fileSet.getBasedir(), tag);

        exitCode = GitCommandLineUtils.execute(clTag, stdout, stderr);
        if (exitCode != 0) {
            return new UntagScmResult(clTag.toString(), "The git-tag command failed.", stderr.getOutput(), false);
        }

        if (repo.isPushChanges()) {
            // and now push the tag to the configured upstream repository
            Commandline clPush = createPushCommandLine(repository, fileSet, tag);

            exitCode = GitCommandLineUtils.execute(clPush, stdout, stderr);
            if (exitCode != 0) {
                return new UntagScmResult(clPush.toString(), "The git-push command failed.", stderr.getOutput(), false);
            }
        }

        return new UntagScmResult(clTag.toString());
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine(
            GitScmProviderRepository repository, File workingDirectory, String tag) {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(workingDirectory, "tag");

        cl.createArg().setValue("-d");
        cl.createArg().setValue(tag);

        return cl;
    }

    public static Commandline createPushCommandLine(
            GitScmProviderRepository repository, ScmFileSet fileSet, String tag) {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(fileSet.getBasedir(), "push");

        cl.createArg().setValue("--delete");
        cl.createArg().setValue(repository.getPushUrl());
        cl.createArg().setValue("refs/tags/" + tag);

        return cl;
    }
}
