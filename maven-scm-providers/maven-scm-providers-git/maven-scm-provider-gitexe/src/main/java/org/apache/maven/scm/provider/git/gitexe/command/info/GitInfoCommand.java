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
package org.apache.maven.scm.provider.git.gitexe.command.info;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Uses {@code git log} command to retrieve info about the most recent commits related to specific files.
 * @author Olivier Lamy
 * @since 1.5
 */
public class GitInfoCommand extends AbstractCommand implements GitCommand {

    public static final int NO_REVISION_LENGTH = -1;

    @Override
    protected ScmResult executeCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters) throws ScmException {

        Commandline baseCli = GitCommandLineUtils.getBaseGitCommandLine(fileSet.getBasedir(), "log");
        baseCli.createArg().setValue("-1"); // only most recent commit matters
        baseCli.createArg().setValue("--no-merges"); // skip merge commits
        baseCli.addArg(GitInfoConsumer.getFormatArgument());

        List<InfoItem> infoItems = new LinkedList<>();
        if (fileSet.getFileList().isEmpty()) {
            infoItems.add(executeInfoCommand(baseCli, parameters, fileSet.getBasedir()));
        } else {
            // Insert a separator to make sure that files aren't interpreted as part of the version spec
            baseCli.createArg().setValue("--");
            // iterate over files
            for (File scmFile : fileSet.getFileList()) {
                Commandline cliClone = (Commandline) baseCli.clone();
                GitCommandLineUtils.addTarget(cliClone, Collections.singletonList(scmFile));
                infoItems.add(executeInfoCommand(cliClone, parameters, scmFile));
            }
        }
        return new InfoScmResult(baseCli.toString(), infoItems);
    }

    protected InfoItem executeInfoCommand(Commandline cli, CommandParameters parameters, File scmFile)
            throws ScmException {
        GitInfoConsumer consumer = new GitInfoConsumer(scmFile.toPath(), getRevisionLength(parameters));
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        int exitCode = GitCommandLineUtils.execute(cli, consumer, stderr);
        if (exitCode != 0) {
            throw new ScmException("The git log command failed: " + cli.toString() + " returned " + stderr.getOutput());
        }
        return consumer.getInfoItem();
    }

    /**
     * Get the revision length from the parameters
     *
     * @param parameters
     * @return -1 if parameter {@link CommandParameter.SCM_SHORT_REVISION_LENGTH} is absent, <br>
     *         and otherwise - the length to be applied for the revision formatting
     * @throws ScmException
     * @since 1.7
     */
    private static int getRevisionLength(final CommandParameters parameters) throws ScmException {
        if (parameters == null) {
            return NO_REVISION_LENGTH;
        } else {
            return parameters.getInt(CommandParameter.SCM_SHORT_REVISION_LENGTH, NO_REVISION_LENGTH);
        }
    }
}
