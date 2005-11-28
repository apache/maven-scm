package org.apache.maven.scm.provider.perforce.command.login;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.login.AbstractLoginCommand;
import org.apache.maven.scm.login.LoginScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
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

    public LoginScmResult executeLoginCommand( ScmProviderRepository repo, ScmFileSet files, CommandParameters params )
        throws ScmException
    {
        Commandline cl = createCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir(), params );
        PerforceLoginConsumer consumer = new PerforceLoginConsumer();
        // In Perforce the user logs in once and then has a ticket good for 12 hours.
        // We assume the user has logged in already so we don't have to deal with
        // password management.
//        try
//        {
//            Process proc = cl.execute();
//            DataOutputStream dos = new DataOutputStream( proc.getOutputStream() );
//            dos.writeUTF( "TODO???\r\n" );
//            dos.close();
//            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
//            String line = null;
//            while ( ( line = br.readLine() ) != null )
//            {
//                consumer.consumeLine( line );
//            }
//        }
//        catch ( CommandLineException e )
//        {
//            e.printStackTrace();
//        }
//        catch ( IOException e )
//        {
//            e.printStackTrace();
//        }

        return new LoginScmResult( cl.toString(), consumer.isSuccess() ? "Login successful" : "Login failed", consumer
            .getOutput(), consumer.isSuccess() );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDir,
                                                CommandParameters params )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDir );

        command.createArgument().setValue( "login" );
        return command;
    }
}