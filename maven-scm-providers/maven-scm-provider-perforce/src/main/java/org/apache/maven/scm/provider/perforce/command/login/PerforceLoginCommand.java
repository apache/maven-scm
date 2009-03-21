package org.apache.maven.scm.provider.perforce.command.login;

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

import java.io.File;
import java.io.StringBufferInputStream;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.login.AbstractLoginCommand;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author Mike Perham
 * @version $Id$
 */
public class PerforceLoginCommand
    extends AbstractLoginCommand
    implements PerforceCommand
{
    /** {@inheritDoc} */
    public LoginScmResult executeLoginCommand( ScmProviderRepository repo, ScmFileSet files, CommandParameters params )
        throws ScmException
    {
        Commandline cl = createCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir() );
        PerforceLoginConsumer consumer = new PerforceLoginConsumer();
        boolean isSuccess = false;

        try
        {
            String password = repo.getPassword();
            if ( StringUtils.isEmpty( password ) )
            {
                if ( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "No password found, proceeding without it." );
                }
                isSuccess = true;
            }
            else
            {
                CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
                int exitCode = CommandLineUtils.executeCommandLine( cl, new StringBufferInputStream( password ),
                                                                    consumer, err );
                isSuccess = consumer.isSuccess();

                if ( isSuccess )
                {
                    String cmdLine = CommandLineUtils.toString( cl.getCommandline() );

                    StringBuffer msg = new StringBuffer( "Exit code: " + exitCode + " - " + err.getOutput() );
                    msg.append( '\n' );
                    msg.append( "Command line was:" + cmdLine );

                    throw new CommandLineException( msg.toString() );
                }
            }
        }
        catch ( CommandLineException e )
        {
            throw new ScmException( "", e );
        }

        return new LoginScmResult( cl.toString(), isSuccess ? "Login successful" : "Login failed",
                                   consumer.getOutput(), isSuccess );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDir )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDir );

        command.createArg().setValue( "login" );
        if ( !StringUtils.isEmpty( repo.getUser() ) )
        {
            command.createArg().setValue( repo.getUser() );
        }
        return command;
    }
}
