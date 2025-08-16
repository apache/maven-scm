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
package org.apache.maven.scm.provider.svn.svnexe.command.untag;

import java.io.File;
import java.io.IOException;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmUntagParameters;
import org.apache.maven.scm.command.untag.AbstractUntagCommand;
import org.apache.maven.scm.command.untag.UntagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnCommandUtils;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * scm:untag for provider svn is done by removing the tag dir
 *
 * @since 1.11.2
 */
public class SvnUntagCommand extends AbstractUntagCommand implements SvnCommand {

    private final boolean interactive;

    public SvnUntagCommand(boolean interactive) {
        this.interactive = interactive;
    }

    /** {@inheritDoc} */
    @Override
    public ScmResult executeUntagCommand(
            ScmProviderRepository repo, ScmFileSet fileSet, ScmUntagParameters scmUntagParameters) throws ScmException {
        String tag = scmUntagParameters.getTag();
        if (tag == null || tag.trim().isEmpty()) {
            throw new ScmException("tag must be specified");
        }

        SvnScmProviderRepository repository = (SvnScmProviderRepository) repo;

        File messageFile = FileUtils.createTempFile("maven-scm-", ".commit", null);

        String message = scmUntagParameters.getMessage();
        try {
            FileUtils.fileWrite(messageFile.getAbsolutePath(), "UTF-8", message);
        } catch (IOException ex) {
            return new UntagScmResult(
                    null,
                    "Error while making a temporary file for the commit message: " + ex.getMessage(),
                    null,
                    false);
        }

        Commandline cl = createCommandline(repository, fileSet, tag, messageFile);

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        if (logger.isDebugEnabled()) {
            logger.debug("Executing: " + SvnCommandLineUtils.cryptPassword(cl));

            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                logger.debug("Working directory: " + cl.getWorkingDirectory().getAbsolutePath());
            }
        }

        int exitCode;

        try {
            exitCode = SvnCommandLineUtils.execute(cl, stdout, stderr);
        } catch (CommandLineException ex) {
            throw new ScmException("Error while executing svn remove command.", ex);
        } finally {
            try {
                FileUtils.forceDelete(messageFile);
            } catch (IOException ex) {
                // ignore
            }
        }

        if (exitCode == 0) {
            return new UntagScmResult(
                    cl.toString(), "The svn remove command was successful.", stderr.getOutput(), true);
        } else {
            return new UntagScmResult(cl.toString(), "The svn remove command failed.", stderr.getOutput(), false);
        }
    }

    /**
     * create command line from parameters
     *
     * @param repo        svn repo tu delete tag from
     * @param fileSet     file set containing base dir
     * @param tag         tag to delete
     * @param messageFile file containing commit message
     * @return            command line instance
     */
    Commandline createCommandline(SvnScmProviderRepository repo, ScmFileSet fileSet, String tag, File messageFile) {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine(fileSet.getBasedir(), repo, interactive);

        cl.createArg().setValue("--file");

        cl.createArg().setValue(messageFile.getAbsolutePath());

        cl.createArg().setValue("remove");

        String tagUrl = SvnTagBranchUtils.resolveTagUrl(repo, new ScmTag(tag));
        tagUrl = SvnCommandUtils.fixUrl(tagUrl, repo.getUser());
        cl.createArg().setValue(tagUrl + "@");

        return cl;
    }
}
