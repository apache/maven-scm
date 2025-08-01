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
package org.apache.maven.scm.provider.svn.svnexe.command.remove;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author Olivier Lamy
 *
 */
public class SvnRemoveCommand extends AbstractRemoveCommand implements SvnCommand<RemoveScmResult> {
    /** {@inheritDoc} */
    protected RemoveScmResult executeRemoveCommand(ScmProviderRepository repository, ScmFileSet fileSet, String message)
            throws ScmException {
        if (fileSet.getFileList().isEmpty()) {
            throw new ScmException("You must provide at least one file/directory to remove");
        }

        Commandline cl = createCommandLine(fileSet.getBasedir(), fileSet.getFileList());

        SvnRemoveConsumer consumer = new SvnRemoveConsumer();

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
            return new RemoveScmResult(cl.toString(), "The svn command failed.", stderr.getOutput(), false);
        }

        return new RemoveScmResult(cl.toString(), consumer.getRemovedFiles());
    }

    private static Commandline createCommandLine(File workingDirectory, List<File> files) throws ScmException {
        // Base command line doesn't make sense here - username/password not needed, and non-interactive/non-recusive is
        // not valid

        Commandline cl = new Commandline();

        cl.setExecutable("svn");

        cl.setWorkingDirectory(workingDirectory.getAbsolutePath());

        cl.createArg().setValue("remove");

        try {
            SvnCommandLineUtils.addTarget(cl, files);
        } catch (IOException e) {
            throw new ScmException("Can't create the targets file", e);
        }

        return cl;
    }
}
