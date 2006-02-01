package org.apache.maven.scm.provider.svn.command.changelog;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.SvnCommandLineUtils;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeLogCommand
    extends AbstractChangeLogCommand
    implements SvnCommand
{
    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, String branch )
        throws ScmException
    {
        Commandline cl =
            createCommandLine( (SvnScmProviderRepository) repo, fileSet.getBasedir(), branch, startDate, endDate );

        SvnChangeLogConsumer consumer = new SvnChangeLogConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

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

        return new ChangeLogScmResult( cl.toString(),
                                       new ChangeLogSet( consumer.getModifications(), startDate, endDate ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 String branch, Date startDate, Date endDate )
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat( DATE_FORMAT );

        dateFormat.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory, repository );

        cl.createArgument().setValue( "log" );

        cl.createArgument().setValue( "-v" );

        // TODO: May want to add some kind of support for --stop-on-copy and --limit NUM

        if ( startDate != null )
        {
            cl.createArgument().setValue( "-r" );

            if ( endDate != null )
            {
                cl.createArgument().setValue(
                    "{" + dateFormat.format( startDate ) + "}" + ":" + "{" + dateFormat.format( endDate ) + "}" );
            }
            else
            {
                cl.createArgument().setValue( "{" + dateFormat.format( startDate ) + "}:HEAD" );
            }
        }

        if ( branch != null )
        {
            // By specifying a branch and this repository url below, subversion should show 
            // the changelog of that branch, but limit it to paths that also occur in this repository.
            cl.createArgument().setValue( SvnTagBranchUtils.resolveBranchUrl( repository, branch ) );
        }

        cl.createArgument().setValue( repository.getUrl() );

        return cl;
    }
}
