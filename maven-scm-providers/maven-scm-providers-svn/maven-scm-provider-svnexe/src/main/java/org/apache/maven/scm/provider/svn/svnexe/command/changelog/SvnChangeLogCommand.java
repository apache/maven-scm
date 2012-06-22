package org.apache.maven.scm.provider.svn.svnexe.command.changelog;

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
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class SvnChangeLogCommand
    extends AbstractChangeLogCommand
    implements SvnCommand
{
    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    /** {@inheritDoc} */
    @Deprecated
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          ScmVersion startVersion, ScmVersion endVersion,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, null, null, null, datePattern, startVersion, endVersion, null );
    }

    /** {@inheritDoc} */
    @Deprecated
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, startDate, endDate, branch, datePattern, null, null, null );
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
                endVersion, request.getLimit() );
    }

    private ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern, ScmVersion startVersion,
                                                          ScmVersion endVersion, Integer limit )
        throws ScmException
    {
        Commandline cl = createCommandLine( (SvnScmProviderRepository) repo, fileSet.getBasedir(), branch, startDate,
                                            endDate, startVersion, endVersion, limit );

        SvnChangeLogConsumer consumer = new SvnChangeLogConsumer( getLogger(), datePattern );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + SvnCommandLineUtils.cryptPassword( cl ) );
            getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
        }

        int exitCode;

        try
        {
            exitCode = SvnCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing svn command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new ChangeLogScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }
        ChangeLogSet changeLogSet = new ChangeLogSet( consumer.getModifications(), startDate, endDate );
        changeLogSet.setStartVersion( startVersion );
        changeLogSet.setEndVersion( endVersion );

        return new ChangeLogScmResult( cl.toString(), changeLogSet );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 ScmBranch branch, Date startDate, Date endDate,
                                                 ScmVersion startVersion, ScmVersion endVersion )
    {
        return createCommandLine(repository, workingDirectory, branch,
            startDate, endDate, startVersion, endVersion, null);
    }

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 ScmBranch branch, Date startDate, Date endDate,
                                                 ScmVersion startVersion, ScmVersion endVersion, Integer limit )
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat( DATE_FORMAT );

        dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory, repository );

        cl.createArg().setValue( "log" );

        cl.createArg().setValue( "-v" );

        // TODO: May want to add some kind of support for --stop-on-copy and --limit NUM

        if (limit != null && limit > 0)
        {
            cl.createArg().setValue( "--limit" );
            cl.createArg().setValue( Integer.toString( limit ) );
        }

        if ( startDate != null )
        {
            cl.createArg().setValue( "-r" );

            if ( endDate != null )
            {
                cl.createArg().setValue(
                    "{" + dateFormat.format( startDate ) + "}" + ":" + "{" + dateFormat.format( endDate ) + "}" );
            }
            else
            {
                cl.createArg().setValue( "{" + dateFormat.format( startDate ) + "}:HEAD" );
            }
        }

        if ( startVersion != null )
        {
            cl.createArg().setValue( "-r" );

            if ( endVersion != null )
            {
                if ( startVersion.getName().equals( endVersion.getName() ) )
                {
                    cl.createArg().setValue( startVersion.getName() );
                }
                else
                {
                    cl.createArg().setValue( startVersion.getName() + ":" + endVersion.getName() );
                }
            }
            else
            {
                cl.createArg().setValue( startVersion.getName() + ":HEAD" );
            }
        }

        if ( branch != null && StringUtils.isNotEmpty( branch.getName() ) )
        {
            // By specifying a branch and this repository url below, subversion should show
            // the changelog of that branch, but limit it to paths that also occur in this repository.
            if ( branch instanceof ScmTag )
            {
                cl.createArg().setValue( SvnTagBranchUtils.resolveTagUrl( repository, (ScmTag) branch ) );
            }
            else
            {
                cl.createArg().setValue( SvnTagBranchUtils.resolveBranchUrl( repository, branch ) );
            }
        }

        if (endVersion == null || !StringUtils.equals("BASE", endVersion.getName()))
        {
            cl.createArg().setValue( repository.getUrl() );
        }

        return cl;
    }
}
