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
package org.apache.maven.scm.provider.svn.svnexe.command.mkdir;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.mkdir.AbstractMkdirCommand;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 *
 */
public class SvnMkdirCommand extends AbstractMkdirCommand implements SvnCommand {
    private final boolean interactive;

    public SvnMkdirCommand(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * {@inheritDoc}
     */
    protected MkdirScmResult executeMkdirCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, String message, boolean createInLocal)
            throws ScmException {
        File messageFile = FileUtils.createTempFile("maven-scm-", ".commit", null);

        try {
            FileUtils.fileWrite(messageFile.getAbsolutePath(), "UTF-8", message);
        } catch (IOException ex) {
            return new MkdirScmResult(
                    null, "Error while making a temporary file for the mkdir message: " + ex.getMessage(), null, false);
        }

        Commandline cl = createCommandLine((SvnScmProviderRepository) repository, fileSet, messageFile, createInLocal);

        SvnMkdirConsumer consumer = new SvnMkdirConsumer();

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
        } finally {
            try {
                FileUtils.forceDelete(messageFile);
            } catch (IOException ex) {
                // ignore
            }
        }

        if (exitCode != 0) {
            return new MkdirScmResult(cl.toString(), "The svn command failed.", stderr.getOutput(), false);
        }

        if (createInLocal) {
            return new MkdirScmResult(cl.toString(), consumer.getCreatedDirs());
        } else {
            return new MkdirScmResult(cl.toString(), Integer.toString(consumer.getRevision()));
        }
    }

    protected Commandline createCommandLine(
            SvnScmProviderRepository repository, ScmFileSet fileSet, File messageFile, boolean createInLocal) {
        // as we want to be able to create path remote only create this directory if not here

        if (!fileSet.getBasedir().exists() && !createInLocal) {
            fileSet.getBasedir().mkdirs();
        }
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine(fileSet.getBasedir(), repository, interactive);

        cl.createArg().setValue("mkdir");

        cl.createArg().setValue("--parents");

        Iterator<File> it = fileSet.getFileList().iterator();
        String dirPath = it.next().getPath();
        // replacing \ with / for windauze
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            dirPath = StringUtils.replace(dirPath, "\\", "/");
        }

        if (!createInLocal) {
            cl.createArg().setValue(repository.getUrl() + "/" + dirPath + "@");

            if (messageFile != null) {
                cl.createArg().setValue("--file");
                cl.createArg().setValue(messageFile.getAbsolutePath());

                cl.createArg().setValue("--encoding");
                cl.createArg().setValue("UTF-8");
            }
        } else {
            cl.createArg().setValue(dirPath);
        }

        return cl;
    }
}
