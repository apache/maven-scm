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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

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
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogCommand.java 264804 2005-08-30 16:09:04Z
 *          evenisse $
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

        try
        {
            Process proc = cl.execute();
            DataOutputStream dos = new DataOutputStream( proc.getOutputStream() );
            if ( StringUtils.isEmpty( repo.getPassword() ) )
            {
                throw new ScmException( "password is required for the perforce scm plugin." );
            }
            dos.writeUTF( repo.getPassword() );
            dos.close();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line = null;
            while ( ( line = br.readLine() ) != null )
            {
                getLogger().debug( "Consuming: " + line );
                consumer.consumeLine( line );
            }
            // Read errors from STDERR
            BufferedReader brErr = new BufferedReader( new InputStreamReader( proc.getErrorStream() ) );
            while ( ( line = brErr.readLine() ) != null )
            {
                getLogger().debug( "Consuming stderr: " + line );
                consumer.consumeLine( line );
            }
            brErr.close();
        }
        catch ( CommandLineException e )
        {
            throw new ScmException( "", e );
        }
        catch ( IOException e )
        {
            throw new ScmException( "", e );
        }

        return new LoginScmResult( cl.toString(), consumer.isSuccess() ? "Login successful" : "Login failed",
                        consumer.getOutput(), consumer.isSuccess() );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDir )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDir );

        command.createArgument().setValue( "login" );
        if ( !StringUtils.isEmpty( repo.getUser() ) )
        {
            command.createArgument().setValue( repo.getUser() );
        }
        return command;
    }
}