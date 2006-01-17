package org.apache.maven.scm.provider.starteam.command.edit;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.edit.AbstractEditCommand;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamEditCommand
    extends AbstractEditCommand
    implements StarteamCommand
{
    // ----------------------------------------------------------------------
    // AbstractEditCommand Implementation
    // ----------------------------------------------------------------------

    protected ScmResult executeEditCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        StarteamEditConsumer consumer = new StarteamEditConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        File[] editFiles = fileSet.getFiles();

        if ( editFiles.length == 0 )
        {
            Commandline cl = createCommandLine( repository, fileSet.getBasedir() );

            int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

            if ( exitCode != 0 )
            {
                return new EditScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(), false );
            }
        }
        else
        {
            //edit only interested files already on the local disk
            for ( int i = 0; i < editFiles.length; ++i )
            {
                Commandline cl = createCommandLine( repository, editFiles[i] );

                int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

                if ( exitCode != 0 )
                {
                    return new EditScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(),
                                              false );
                }
            }
        }

        return new EditScmResult( null, consumer.getEditedFiles() );

    }

    public static Commandline createCommandLine( StarteamScmProviderRepository repo, File dirOrFile )
    {
        Commandline cl = StarteamCommandLineUtils.createStarteamBaseCommandLine( "lck", dirOrFile, repo );

        cl.createArgument().setValue( "-l" );

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
}
