package org.apache.maven.scm.provider.perforce.command.unedit;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.unedit.AbstractUnEditCommand;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogCommand.java 264804 2005-08-30 16:09:04Z
 *          evenisse $
 */
public class PerforceUnEditCommand
    extends AbstractUnEditCommand
    implements PerforceCommand
{

    protected ScmResult executeUnEditCommand( ScmProviderRepository repo, ScmFileSet files )
        throws ScmException
    {
        Commandline cl = createCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir(), files );
        PerforceUnEditConsumer consumer = new PerforceUnEditConsumer();
        try
        {
            Process proc = cl.execute();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line = null;
            while ( ( line = br.readLine() ) != null )
            {
                consumer.consumeLine( line );
            }
        }
        catch ( CommandLineException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        if ( consumer.isSuccess() )
        {
            return new UnEditScmResult( cl.toString(), consumer.getEdits() );
        }
        else
        {
            return new UnEditScmResult( cl.toString(), "Unable to revert", consumer.getOutput(), consumer.isSuccess() );
        }
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                ScmFileSet files )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArgument().setValue( "revert" );

        try
        {
            String candir = workingDirectory.getCanonicalPath();
            File[] fs = files.getFiles();
            for ( int i = 0; i < fs.length; i++ )
            {
                File file = fs[i];
                // I want to use relative paths to add files to make testing
                // simpler.
                // Otherwise the absolute path will be different on everyone's
                // machine
                // and testing will be a little more painful.
                String canfile = file.getCanonicalPath();
                if ( canfile.startsWith( candir ) )
                {
                    canfile = canfile.substring( candir.length() + 1 );
                }
                command.createArgument().setValue( file.getName() );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return command;
    }
}