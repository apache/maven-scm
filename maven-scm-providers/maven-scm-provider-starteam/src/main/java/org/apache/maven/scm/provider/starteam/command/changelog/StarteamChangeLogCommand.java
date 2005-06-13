package org.apache.maven.scm.provider.starteam.command.changelog;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import java.io.File;
import java.util.Date;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamChangeLogCommand
    extends AbstractChangeLogCommand
    implements StarteamCommand
{
    // ----------------------------------------------------------------------
    // AbstractChangeLogCommand Implementation
    // ----------------------------------------------------------------------

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, int numDays, String branch )
        throws ScmException
    {
        if ( startDate != null || endDate != null )
        {
            throw new ScmException( "This provider doesn't support start and end dates." );
        }

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        // TODO: revision
        Commandline cl = createCommandLine( repository, fileSet.getBasedir(), branch );

        StarteamChangeLogConsumer consumer = new StarteamChangeLogConsumer( getLogger(), startDate, endDate );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );
        getLogger().debug( "Command line: " + cl );

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new ChangeLogScmResult( "The 'stcmd' command failed.", stderr.getOutput(), false );
        }

        return new ChangeLogScmResult( consumer.getModifications() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( StarteamScmProviderRepository repo, File workingDirectory, String tag )
    {
		String workingDir =  workingDirectory.getAbsolutePath();

        Commandline cl = StarteamCommandLineUtils.createStarteamBaseCommandLine("hist", workingDirectory, repo);

        cl.createArgument().setValue( "-is" );

        if ( tag != null && tag.length() != 0 )
        {
            cl.createArgument().setValue( "-cfgl" );

            cl.createArgument().setValue( tag );
        }

        return cl;
    }
}
