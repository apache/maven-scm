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
package org.apache.maven.scm.provider.svn.svnexe.command.tag;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnCommandUtils;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author Olivier Lamy
 *
 * TODO since this is just a copy, use that instead.
 */
public class SvnTagCommand extends AbstractTagCommand implements SvnCommand {

    public ScmResult executeTagCommand(ScmProviderRepository repo, ScmFileSet fileSet, String tag, String message)
            throws ScmException {
        ScmTagParameters scmTagParameters = new ScmTagParameters(message);
        // force false to preserve backward comp
        scmTagParameters.setRemoteTagging(false);
        scmTagParameters.setPinExternals(false);
        return executeTagCommand(repo, fileSet, tag, scmTagParameters);
    }

    /** {@inheritDoc} */
    public ScmResult executeTagCommand(
            ScmProviderRepository repo, ScmFileSet fileSet, String tag, ScmTagParameters scmTagParameters)
            throws ScmException {
        // NPE free
        if (scmTagParameters == null) {
            logger.debug("SvnTagCommand :: scmTagParameters is null create an empty one");
            scmTagParameters = new ScmTagParameters();
            scmTagParameters.setRemoteTagging(false);
            scmTagParameters.setPinExternals(false);
        } else {
            logger.debug("SvnTagCommand :: scmTagParameters.remoteTagging : " + scmTagParameters.isRemoteTagging());
        }
        if (tag == null || StringUtils.isEmpty(tag.trim())) {
            throw new ScmException("tag must be specified");
        }

        if (!fileSet.getFileList().isEmpty()) {
            throw new ScmException("This provider doesn't support tagging subsets of a directory");
        }

        SvnScmProviderRepository repository = (SvnScmProviderRepository) repo;

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

        Commandline cl = createCommandLine(repository, fileSet.getBasedir(), tag, messageFile, scmTagParameters);

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        if (logger.isInfoEnabled()) {
            logger.info("Executing: " + SvnCommandLineUtils.cryptPassword(cl));

            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                logger.info("Working directory: " + cl.getWorkingDirectory().getAbsolutePath());
            }
        }

        int exitCode;

        try {
            exitCode = SvnCommandLineUtils.execute(cl, stdout, stderr);
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
            // TODO: Improve this error message
            return new TagScmResult(cl.toString(), "The svn tag command failed.", stderr.getOutput(), false);
        }

        List<ScmFile> fileList = new ArrayList<>();

        List<File> files = null;

        try {
            if (StringUtils.isNotEmpty(fileSet.getExcludes())) {
                files = FileUtils.getFiles(
                        fileSet.getBasedir(),
                        (StringUtils.isEmpty(fileSet.getIncludes()) ? "**" : fileSet.getIncludes()),
                        fileSet.getExcludes() + ",**/.svn/**",
                        false);
            } else {
                files = FileUtils.getFiles(
                        fileSet.getBasedir(),
                        (StringUtils.isEmpty(fileSet.getIncludes()) ? "**" : fileSet.getIncludes()),
                        "**/.svn/**",
                        false);
            }
        } catch (IOException e) {
            throw new ScmException("Error while executing command.", e);
        }

        for (Iterator<File> i = files.iterator(); i.hasNext(); ) {
            File f = i.next();

            fileList.add(new ScmFile(f.getPath(), ScmFileStatus.TAGGED));
        }

        return new TagScmResult(cl.toString(), fileList);
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * @deprecated
     * @param repository
     * @param workingDirectory
     * @param tag
     * @param messageFile
     * @return TODO
     */
    public static Commandline createCommandLine(
            SvnScmProviderRepository repository, File workingDirectory, String tag, File messageFile) {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine(workingDirectory, repository);

        cl.createArg().setValue("copy");

        cl.createArg().setValue("--parents");

        cl.createArg().setValue("--file");

        cl.createArg().setValue(messageFile.getAbsolutePath());

        cl.createArg().setValue(".");

        // Note: this currently assumes you have the tag base checked out too
        String tagUrl = SvnTagBranchUtils.resolveTagUrl(repository, new ScmTag(tag));
        tagUrl = SvnCommandUtils.fixUrl(tagUrl, repository.getUser());
        cl.createArg().setValue(tagUrl + "@");

        return cl;
    }

    public static Commandline createCommandLine(
            SvnScmProviderRepository repository,
            File workingDirectory,
            String tag,
            File messageFile,
            ScmTagParameters scmTagParameters) {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine(workingDirectory, repository);

        cl.createArg().setValue("copy");

        cl.createArg().setValue("--file");

        cl.createArg().setValue(messageFile.getAbsolutePath());

        cl.createArg().setValue("--encoding");

        cl.createArg().setValue("UTF-8");

        cl.createArg().setValue("--parents");

        if (scmTagParameters != null && scmTagParameters.getScmRevision() != null) {
            cl.createArg().setValue("--revision");

            cl.createArg().setValue(scmTagParameters.getScmRevision());
        }

        if (scmTagParameters != null && scmTagParameters.isPinExternals()) {
            cl.createArg().setValue("--pin-externals");
        }

        if (scmTagParameters != null && scmTagParameters.isRemoteTagging()) {
            String url = SvnCommandUtils.fixUrl(repository.getUrl(), repository.getUser());
            cl.createArg().setValue(url + "@");
        } else {
            cl.createArg().setValue(".");
        }

        // Note: this currently assumes you have the tag base checked out too
        String tagUrl = SvnTagBranchUtils.resolveTagUrl(repository, new ScmTag(tag));
        tagUrl = SvnCommandUtils.fixUrl(tagUrl, repository.getUser());
        cl.createArg().setValue(tagUrl + "@");

        return cl;
    }
}
