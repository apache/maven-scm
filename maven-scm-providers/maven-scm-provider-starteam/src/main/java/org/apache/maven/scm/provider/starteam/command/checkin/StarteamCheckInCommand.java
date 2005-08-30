package org.apache.maven.scm.provider.starteam.command.checkin;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;

import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;

import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id: $
 */
public class StarteamCheckInCommand
    extends AbstractCheckInCommand
    implements StarteamCommand
{
    // ----------------------------------------------------------------------
    // AbstractCheckInCommand Implementation
    // ----------------------------------------------------------------------

    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      String tag )
        throws ScmException
    {
        //work around until maven-scm-api allow this
        String issue = System.getProperty( "maven.scm.issue" );

        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        StarteamCheckInConsumer consumer = new StarteamCheckInConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        File[] checkInFiles = fileSet.getFiles();

        if ( checkInFiles.length == 0 )
        {
            Commandline cl = createCommandLine( repository, fileSet.getBasedir(), message, tag, issue );

            int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

            if ( exitCode != 0 )
            {
                return new CheckInScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(), false );
            }
        }
        else
        {
            //update only interested files already on the local disk
            for ( int i = 0; i < checkInFiles.length; ++i )
            {
                Commandline cl = createCommandLine( repository, checkInFiles[i], message, tag, issue );

                int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

                if ( exitCode != 0 )
                {
                    return new CheckInScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(), false );
                }
            }
        }

        return new CheckInScmResult( null, consumer.getCheckedInFiles() );

    }

    public static Commandline createCommandLine( StarteamScmProviderRepository repo, File dirOrFile, String message,
                                                 String tag, String issue )
    {
        Commandline cl = StarteamCommandLineUtils.createStarteamBaseCommandLine( "ci", dirOrFile, repo );

        if ( message != null && message.length() > 0 )
        {
            cl.createArgument().setValue( "-r" );

            cl.createArgument().setValue( message );
        }

        if ( tag != null && tag.length() > 0 )
        {
            cl.createArgument().setValue( "-vl" );

            cl.createArgument().setValue( tag );
        }

        if ( issue != null && issue.length() > 0 )
        {
            cl.createArgument().setValue( "-cr" );

            cl.createArgument().setValue( issue );
        }

        if ( dirOrFile.isDirectory() )
        {
            cl.createArgument().setValue( "-f" );

            cl.createArgument().setValue( "NCI" );

            cl.createArgument().setValue( "-is" );
        }
        else
        {
            cl.createArgument().setValue( dirOrFile.getName() );
        }

        return cl;
    }
}
