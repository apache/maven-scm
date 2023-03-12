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
package org.apache.maven.scm.provider.git.gitexe.command.changelog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 *
 */
public class GitChangeLogCommand extends AbstractChangeLogCommand implements GitCommand {
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    @Override
    public ScmResult executeCommand(ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters)
            throws ScmException {
        return executeChangeLogCommand(
                repository,
                fileSet,
                parameters.getDate(CommandParameter.START_DATE, null),
                parameters.getDate(CommandParameter.END_DATE, null),
                (ScmBranch) parameters.getScmVersion(CommandParameter.BRANCH, null),
                parameters.getString(CommandParameter.CHANGELOG_DATE_PATTERN, null),
                parameters.getScmVersion(CommandParameter.START_SCM_VERSION, null),
                parameters.getScmVersion(CommandParameter.END_SCM_VERSION, null),
                parameters.getInt(CommandParameter.LIMIT, -1),
                parameters.getScmVersion(CommandParameter.SCM_VERSION, null));
    }

    /** {@inheritDoc} */
    @Override
    protected ChangeLogScmResult executeChangeLogCommand(
            ScmProviderRepository repo,
            ScmFileSet fileSet,
            ScmVersion startVersion,
            ScmVersion endVersion,
            String datePattern)
            throws ScmException {
        return executeChangeLogCommand(repo, fileSet, null, null, null, datePattern, startVersion, endVersion);
    }

    /** {@inheritDoc} */
    @Override
    protected ChangeLogScmResult executeChangeLogCommand(
            ScmProviderRepository repo,
            ScmFileSet fileSet,
            Date startDate,
            Date endDate,
            ScmBranch branch,
            String datePattern)
            throws ScmException {
        return executeChangeLogCommand(repo, fileSet, startDate, endDate, branch, datePattern, null, null);
    }

    @Override
    protected ChangeLogScmResult executeChangeLogCommand(
            ScmProviderRepository repository, ScmFileSet fileSet, ScmVersion version, String datePattern)
            throws ScmException {
        return executeChangeLogCommand(repository, fileSet, null, null, null, datePattern, null, null, null, version);
    }

    protected ChangeLogScmResult executeChangeLogCommand(
            ScmProviderRepository repo,
            ScmFileSet fileSet,
            Date startDate,
            Date endDate,
            ScmBranch branch,
            String datePattern,
            ScmVersion startVersion,
            ScmVersion endVersion)
            throws ScmException {
        return executeChangeLogCommand(
                repo, fileSet, startDate, endDate, branch, datePattern, startVersion, endVersion, null, null);
    }

    @Override
    protected ChangeLogScmResult executeChangeLogCommand(ChangeLogScmRequest request) throws ScmException {
        final ScmVersion startVersion = request.getStartRevision();
        final ScmVersion endVersion = request.getEndRevision();
        final ScmVersion revision = request.getRevision();
        final ScmFileSet fileSet = request.getScmFileSet();
        final String datePattern = request.getDatePattern();
        final ScmProviderRepository providerRepository =
                request.getScmRepository().getProviderRepository();
        final Date startDate = request.getStartDate();
        final Date endDate = request.getEndDate();
        final ScmBranch branch = request.getScmBranch();
        final Integer limit = request.getLimit();

        return executeChangeLogCommand(
                providerRepository,
                fileSet,
                startDate,
                endDate,
                branch,
                datePattern,
                startVersion,
                endVersion,
                limit,
                revision);
    }

    protected ChangeLogScmResult executeChangeLogCommand(
            ScmProviderRepository repo,
            ScmFileSet fileSet,
            Date startDate,
            Date endDate,
            ScmBranch branch,
            String datePattern,
            ScmVersion startVersion,
            ScmVersion endVersion,
            Integer limit)
            throws ScmException {
        return executeChangeLogCommand(
                repo, fileSet, startDate, endDate, branch, datePattern, startVersion, endVersion, limit, null);
    }

    protected ChangeLogScmResult executeChangeLogCommand(
            ScmProviderRepository repo,
            ScmFileSet fileSet,
            Date startDate,
            Date endDate,
            ScmBranch branch,
            String datePattern,
            ScmVersion startVersion,
            ScmVersion endVersion,
            Integer limit,
            ScmVersion version)
            throws ScmException {
        Commandline cl = createCommandLine(
                (GitScmProviderRepository) repo,
                fileSet.getBasedir(),
                branch,
                startDate,
                endDate,
                startVersion,
                endVersion,
                limit,
                version);

        GitChangeLogConsumer consumer = new GitChangeLogConsumer(datePattern);

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        exitCode = GitCommandLineUtils.execute(cl, consumer, stderr);
        if (exitCode != 0) {
            return new ChangeLogScmResult(cl.toString(), "The git-log command failed.", stderr.getOutput(), false);
        }
        ChangeLogSet changeLogSet = new ChangeLogSet(consumer.getModifications(), startDate, endDate);
        changeLogSet.setStartVersion(startVersion);
        changeLogSet.setEndVersion(endVersion);

        return new ChangeLogScmResult(cl.toString(), changeLogSet);
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * This method creates the commandline for the git-whatchanged command.
     * <p>
     * Since it uses --since and --until for the start and end date, the branch
     * and version parameters can be used simultanously.
     *
     * @param repository Provider repositry to use.
     * @param workingDirectory Working copy directory.
     * @param branch Branch to run command on.
     * @param startDate Start date of log entries.
     * @param endDate End date of log entries.
     * @param startVersion Start version of log entries.
     * @param endVersion End version of log entries.
     * @return Command line.
     */
    public static Commandline createCommandLine(
            GitScmProviderRepository repository,
            File workingDirectory,
            ScmBranch branch,
            Date startDate,
            Date endDate,
            ScmVersion startVersion,
            ScmVersion endVersion) {
        return createCommandLine(
                repository, workingDirectory, branch, startDate, endDate, startVersion, endVersion, null);
    }

    static Commandline createCommandLine(
            GitScmProviderRepository repository,
            File workingDirectory,
            ScmBranch branch,
            Date startDate,
            Date endDate,
            ScmVersion startVersion,
            ScmVersion endVersion,
            Integer limit) {
        return createCommandLine(
                repository, workingDirectory, branch, startDate, endDate, startVersion, endVersion, limit, null);
    }

    static Commandline createCommandLine(
            GitScmProviderRepository repository,
            File workingDirectory,
            ScmBranch branch,
            Date startDate,
            Date endDate,
            ScmVersion startVersion,
            ScmVersion endVersion,
            Integer limit,
            ScmVersion version) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine(workingDirectory, "whatchanged");
        cl.createArg().setValue("--format=medium");
        cl.createArg().setValue("--decorate=short");
        cl.createArg().setValue("--raw");
        cl.createArg().setValue("--no-merges");

        if (startDate != null || endDate != null) {
            if (startDate != null) {
                cl.createArg().setValue("--since=" + StringUtils.escape(dateFormat.format(startDate)));
            }

            if (endDate != null) {
                cl.createArg().setValue("--until=" + StringUtils.escape(dateFormat.format(endDate)));
            }
        }

        // since this parameter is also used for the output formatting, we need it also if no start nor end date is
        // given
        cl.createArg().setValue("--date=iso");

        if (startVersion != null || endVersion != null) {
            StringBuilder versionRange = new StringBuilder();

            if (startVersion != null) {
                versionRange.append(StringUtils.escape(startVersion.getName()));
            }

            versionRange.append("..");

            if (endVersion != null) {
                versionRange.append(StringUtils.escape(endVersion.getName()));
            }

            cl.createArg().setValue(versionRange.toString());

        } else if (version != null) {
            cl.createArg().setValue(StringUtils.escape(version.getName()));
        }

        if (limit != null && limit > 0) {
            cl.createArg().setValue("--max-count=" + limit);
        }

        if (branch != null && branch.getName() != null && branch.getName().length() > 0) {
            cl.createArg().setValue(branch.getName());
        }

        // Insert a separator to make sure that files aren't interpreted as part of the version spec
        cl.createArg().setValue("--");

        // We have to report only the changes of the current project.
        // This is needed for child projects, otherwise we would get the changelog of the
        // whole parent-project including all childs.
        cl.createArg().setValue(".");

        return cl;
    }
}
