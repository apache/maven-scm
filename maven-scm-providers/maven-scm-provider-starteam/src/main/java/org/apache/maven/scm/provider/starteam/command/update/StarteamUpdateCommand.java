package org.apache.maven.scm.provider.starteam.command.update;

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
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.command.checkout.StarteamCheckOutConsumer;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;

import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.provider.starteam.command.changelog.StarteamChangeLogCommand;

import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id: $
 */
public class StarteamUpdateCommand
    extends AbstractUpdateCommand
    implements StarteamCommand
{
    // ----------------------------------------------------------------------
    // AbstractUpdateCommand Implementation
    // ----------------------------------------------------------------------

    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {

        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        StarteamCheckOutConsumer consumer = new StarteamCheckOutConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        File[] checkInFiles = fileSet.getFiles();

        if ( checkInFiles.length == 0 )
        {
            //update everything
            Commandline cl = createCommandLine( repository, fileSet.getBasedir(), tag );

            int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

            if ( exitCode != 0 )
            {
                return new UpdateScmResult( "The starteam command failed.", stderr.getOutput(), false );
            }
        }
        else
        {
            //update only interested files already on the local disk
            for ( int i = 0; i < checkInFiles.length; ++i )
            {
                Commandline cl = createCommandLine( repository, checkInFiles[i], tag );

                int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

                if ( exitCode != 0 )
                {
                    return new UpdateScmResult( "The starteam command failed.", stderr.getOutput(), false );
                }
            }
        }

        return new UpdateScmResult( consumer.getCheckedOutFiles() );

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( StarteamScmProviderRepository repo, File dirOrFile, String tag )
    {
        File workingDir;
        Commandline cl = StarteamCommandLineUtils.createStarteamBaseCommandLine( "co", dirOrFile, repo );

        cl.createArgument().setValue( "-merge" );

        cl.createArgument().setValue( "-neverprompt" );

        if ( tag != null && tag.length() != 0 )
        {
            cl.createArgument().setValue( "-vl" );

            cl.createArgument().setValue( tag );
        }

        if ( dirOrFile.isDirectory() )
        {
            cl.createArgument().setValue( "-is" );
        }
        else
        {
            cl.createArgument().setValue( dirOrFile.getName() );
        }

        return cl;
    }

    /**
     * @see org.apache.maven.scm.command.update.AbstractUpdateCommand#getChangeLogCommand()
     */
    protected ChangeLogCommand getChangeLogCommand()
    {
        StarteamChangeLogCommand command = new StarteamChangeLogCommand();

        command.setLogger( getLogger() );

        return command;
    }

}
