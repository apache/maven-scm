package org.apache.maven.scm.provider.starteam.command.checkout;

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
import java.util.ArrayList;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;

import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamCheckOutCommand
    extends AbstractCheckOutCommand
    implements StarteamCommand
{
    // ----------------------------------------------------------------------
    // AbstractCheckOutCommand Implementation
    // ----------------------------------------------------------------------

    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        // TODO: Implement
//        StarteamCheckOutConsumer consumer = new StarteamCheckOutConsumer( getLogger(), fileSet.getBasedir().getParentFile() );

        Commandline cl = createCommandLine( repository, fileSet.getBasedir(), tag );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );
        getLogger().info( "Command line: " + cl );

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, null, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new CheckOutScmResult( "The svn command failed.", stderr.getOutput(), false );
        }

        return new CheckOutScmResult( new ArrayList( 0 ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( StarteamScmProviderRepository repo, File workingDirectory, String tag )
    {
        Commandline command = new Commandline();

        command.setExecutable( "stcmd" );

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        command.createArgument().setValue( "co" );

        command.createArgument().setValue( "-x" );

        command.createArgument().setValue( "-nologo" );

        command.createArgument().setValue( "-is" );

        command.createArgument().setValue( "-p" );

        String p = repo.getUser();

        if ( repo.getPassword() != null )
        {
            p += ":" + repo.getPassword();
        }

        p += "@" + repo.getUrl();

        command.createArgument().setValue( p );

        if ( tag != null )
        {
            command.createArgument().setValue( "-vl" );

            command.createArgument().setValue( tag );
        }

        return command;
    }
}
