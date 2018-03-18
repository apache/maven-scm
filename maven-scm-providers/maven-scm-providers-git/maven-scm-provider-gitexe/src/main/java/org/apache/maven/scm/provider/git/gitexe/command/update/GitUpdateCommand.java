package org.apache.maven.scm.provider.git.gitexe.command.update;

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

import java.io.File;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.update.UpdateScmResultWithRevision;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.gitexe.command.changelog.GitChangeLogCommand;
import org.apache.maven.scm.provider.git.gitexe.command.diff.GitDiffCommand;
import org.apache.maven.scm.provider.git.gitexe.command.diff.GitDiffRawConsumer;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author Olivier Lamy
 * @author <a href="mailto:struberg@yahoo.de">struberg</a>
 * @since 10 august 2008
 *
 */
public class GitUpdateCommand
    extends AbstractUpdateCommand
    implements GitCommand
{
    /** {@inheritDoc} */
    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                    ScmVersion scmVersion )
        throws ScmException
    {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        if ( GitScmProviderRepository.PROTOCOL_FILE.equals( repository.getFetchInfo().getProtocol() )
            && repository.getFetchInfo().getPath().indexOf( fileSet.getBasedir().getPath() ) >= 0 )
        {
            throw new ScmException( "remote repository must not be the working directory" );
        }

        int exitCode;

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        // fir we need to get the current reversion
        Commandline clRev = createLatestRevisionCommandLine( repository, fileSet.getBasedir(), scmVersion );
        GitLatestRevisionCommandConsumer consumerRev = new GitLatestRevisionCommandConsumer( getLogger() );
        exitCode = GitCommandLineUtils.execute( clRev, consumerRev, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new UpdateScmResult( clRev.toString(), "The git-log command failed.",
                    stderr.getOutput(), false );
        }
        String origSha1 = consumerRev.getLatestRevision();

        Commandline cl = createCommandLine( repository, fileSet.getBasedir(), scmVersion );
        exitCode = GitCommandLineUtils.execute( cl, stdout, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new UpdateScmResult( cl.toString(), "The git-pull command failed.",
                                        stderr.getOutput(), false );
        }

        // we also need to log exactly what has been updated
        GitDiffRawConsumer diffRawConsumer = new GitDiffRawConsumer( getLogger() );
        Commandline clDiffRaw = GitDiffCommand.createDiffRawCommandLine( fileSet.getBasedir(), origSha1 );
        exitCode = GitCommandLineUtils.execute( clDiffRaw, diffRawConsumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new UpdateScmResult( clDiffRaw.toString(), "The git-diff --raw command failed.",
                    stderr.getOutput(), false );
        }


        // now let's get the latest version
        consumerRev = new GitLatestRevisionCommandConsumer( getLogger() );
        exitCode = GitCommandLineUtils.execute( clRev, consumerRev, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new UpdateScmResult( clRev.toString(), "The git-log command failed.",
                                        stderr.getOutput(), false );
        }
        String latestRevision = consumerRev.getLatestRevision();

        return new UpdateScmResultWithRevision( cl.toString(), diffRawConsumer.getChangedFiles(), latestRevision );
    }

    /** {@inheritDoc} */
    protected ChangeLogCommand getChangeLogCommand()
    {
        GitChangeLogCommand changelogCmd = new GitChangeLogCommand();
        changelogCmd.setLogger( getLogger() );

        return changelogCmd;
    }

    /**
     * create the command line for updating the current branch with the info from the foreign repository.
     */
    public static Commandline createCommandLine( GitScmProviderRepository repository, File workingDirectory,
                                                 ScmVersion scmVersion )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "pull" );

        cl.createArg().setLine( repository.getFetchUrl() );

        // now set the branch where we would like to pull from
        if ( scmVersion instanceof ScmBranch )
        {
            cl.createArg().setLine( scmVersion.getName() );
        }

        return cl;
    }

    /**
     * @param scmVersion a valid branch or <code>null</code> if the master branch should be taken
     * @return CommandLine for getting the latest commit on the given branch
     */
    public static Commandline createLatestRevisionCommandLine( GitScmProviderRepository repository,
                                                               File workingDirectory, ScmVersion scmVersion )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "log" );

        // only show exactly 1 commit
        cl.createArg().setValue( "-n1" );

        // same as --topo-order, but ensure ordering of merges
        cl.createArg().setValue( "--date-order" );

        if ( scmVersion != null && scmVersion instanceof ScmBranch && scmVersion.getName() != null
            && scmVersion.getName().length() > 0 )
        {
            // if any branch is given, lets take em
            cl.createArg().setValue( scmVersion.getName() );
        }
	// otherwise we work on HEAD/current branch

        return cl;
    }
}
