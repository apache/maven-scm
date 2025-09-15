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
package org.apache.maven.scm.provider.svn.svnexe.command.diff;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.diff.SvnDiffConsumer;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author Olivier Lamy
 */
public class SvnDiffCommand extends AbstractDiffCommand implements SvnCommand {
    private final boolean interactive;

    public SvnDiffCommand(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * {@inheritDoc}
     */
    protected DiffScmResult executeDiffCommand(
            ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion startVersion, ScmVersion endVersion)
            throws ScmException {
        Commandline cl =
                createCommandLine((SvnScmProviderRepository) repo, fileSet.getBasedir(), startVersion, endVersion);

        SvnDiffConsumer consumer = new SvnDiffConsumer(fileSet.getBasedir());

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        if (logger.isInfoEnabled()) {
            logger.info("Executing: " + SvnCommandLineUtils.cryptPassword(cl));

            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                logger.info("Working directory: " + cl.getWorkingDirectory().getAbsolutePath());
            }
        }

        int exitCode;

        try {
            exitCode = SvnCommandLineUtils.execute(cl, consumer, stderr);
        } catch (CommandLineException ex) {
            throw new ScmException("Error while executing command.", ex);
        }

        if (exitCode != 0) {
            return new DiffScmResult(cl.toString(), "The svn command failed.", stderr.getOutput(), false);
        }

        return new DiffScmResult(
                cl.toString(), consumer.getChangedFiles(), consumer.getDifferences(), consumer.getPatch());
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public Commandline createCommandLine(
            SvnScmProviderRepository repository,
            File workingDirectory,
            ScmVersion startVersion,
            ScmVersion endVersion) {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine(workingDirectory, repository, interactive);

        cl.createArg().setValue("diff");

        cl.createArg().setValue("--internal-diff");

        if (startVersion != null && StringUtils.isNotEmpty(startVersion.getName())) {
            cl.createArg().setValue("-r");

            if (endVersion != null && StringUtils.isNotEmpty(endVersion.getName())) {
                cl.createArg().setValue(startVersion.getName() + ":" + endVersion.getName());
            } else {
                cl.createArg().setValue(startVersion.getName());
            }
        }

        return cl;
    }
}
