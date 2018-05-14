package org.apache.maven.scm.provider.git.gitexe.command.changelog;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 *
 */
public class GitChangeLogCommand
    extends AbstractChangeLogCommand
    implements GitCommand
{
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          ScmVersion startVersion, ScmVersion endVersion,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand(
                repo, fileSet, null, null, null, datePattern, startVersion, endVersion
        );
    }

    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          ScmVersion endVersion, String datePattern,
                                                          boolean startFromRoot )
            throws ScmException
    {
        return executeChangeLogCommand(
                repo, fileSet, null, null, null, datePattern, null, endVersion, null, startFromRoot
        );
    }

    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, startDate, endDate, branch, datePattern, null, null );
    }

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern, ScmVersion startVersion,
                                                          ScmVersion endVersion )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, startDate, endDate, branch, datePattern, startVersion,
                                        endVersion, null );
    }

    @Override
    protected ChangeLogScmResult executeChangeLogCommand( ChangeLogScmRequest request )
        throws ScmException
    {
        final ScmVersion startVersion = request.getStartRevision();
        final ScmVersion endVersion = request.getEndRevision();
        final ScmFileSet fileSet = request.getScmFileSet();
        final String datePattern = request.getDatePattern();
        return executeChangeLogCommand( request.getScmRepository().getProviderRepository(), fileSet,
            request.getStartDate(), request.getEndDate(), request.getScmBranch(), datePattern, startVersion,
                endVersion, request.getLimit(), request.getStartFromRoot() );
    }

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern, ScmVersion startVersion,
                                                          ScmVersion endVersion, Integer limit )
            throws ScmException
    {
        return executeChangeLogCommand(
                repo, fileSet, startDate, endDate, branch, datePattern, startVersion, endVersion, limit, false
        );
    }

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern, ScmVersion startVersion,
                                                          ScmVersion endVersion, Integer limit, boolean startFromRoot )
        throws ScmException
    {
        Commandline cl = createCommandLine( (GitScmProviderRepository) repo, fileSet.getBasedir(), branch, startDate,
                                            endDate, startVersion, endVersion, limit, startFromRoot );

        GitChangeLogConsumer consumer = new GitChangeLogConsumer( getLogger(), datePattern );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        exitCode = GitCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new ChangeLogScmResult( cl.toString(), "The git-log command failed.", stderr.getOutput(), false );
        }
        ChangeLogSet changeLogSet = new ChangeLogSet( consumer.getModifications(), startDate, endDate );
        changeLogSet.setStartVersion( startVersion );
        changeLogSet.setEndVersion( endVersion );

        return new ChangeLogScmResult( cl.toString(), changeLogSet );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * this constructs creates the commandline for the git-whatchanged command.
     * Since it uses --since and --until for the start and end date, the branch
     * and version parameters can be used simultanously. 
     */
    public static Commandline createCommandLine( GitScmProviderRepository repository, File workingDirectory,
                                                 ScmBranch branch, Date startDate, Date endDate,
                                                 ScmVersion startVersion, ScmVersion endVersion )
    {
        return createCommandLine( repository, workingDirectory, branch, startDate, endDate, startVersion, endVersion,
                                  null );
    }

    static Commandline createCommandLine( GitScmProviderRepository repository, File workingDirectory,
                                          ScmBranch branch, Date startDate, Date endDate,
                                          ScmVersion startVersion, ScmVersion endVersion, Integer limit )
    {
        return createCommandLine(
                repository, workingDirectory, branch, startDate, endDate, startVersion, endVersion, limit, false
        );
    }
    static Commandline createCommandLine( GitScmProviderRepository repository, File workingDirectory,
                                                 ScmBranch branch, Date startDate, Date endDate,
                                                 ScmVersion startVersion, ScmVersion endVersion, Integer limit,
                                                 boolean startFromRoot )
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat( DATE_FORMAT );
        dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "whatchanged" );

        if ( startDate != null || endDate != null )
        {
            if ( startDate != null )
            {
                cl.createArg().setValue( "--since=" + StringUtils.escape( dateFormat.format( startDate ) ) );
            }

            if ( endDate != null )
            {
                cl.createArg().setValue( "--until=" + StringUtils.escape( dateFormat.format( endDate ) ) );
            }

        }

        // since this parameter is also used for the output formatting, we need it also if no start nor end date is
        // given
        cl.createArg().setValue( "--date=iso" );

        if ( startVersion != null || endVersion != null )
        {
            StringBuilder versionRange = new StringBuilder();

            if ( startVersion != null )
            {
                versionRange.append( StringUtils.escape( startVersion.getName() ) );
            }

            if ( startVersion != null || !startFromRoot )
            {
                versionRange.append( ".." );
            }

            if ( endVersion != null )
            {
                versionRange.append( StringUtils.escape( endVersion.getName() ) );
            }
            
            cl.createArg().setValue( versionRange.toString() ); 

        }

        if ( limit != null && limit > 0 )
        {
            cl.createArg().setValue( "--max-count=" + limit );
        }

        if ( branch != null && branch.getName() != null && branch.getName().length() > 0 )
        {
            cl.createArg().setValue( branch.getName() );
        }

        // Insert a separator to make sure that files aren't interpreted as part of the version spec
        cl.createArg().setValue( "--" );
        
        // We have to report only the changes of the current project.
        // This is needed for child projects, otherwise we would get the changelog of the 
        // whole parent-project including all childs.
        cl.createArg().setFile( workingDirectory );
        
        return cl;
    }
}
