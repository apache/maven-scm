package org.apache.maven.scm.provider.perforce.command.edit;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.edit.AbstractEditCommand;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogCommand.java 264804 2005-08-30 16:09:04Z
 *          evenisse $
 */
public class PerforceEditCommand
    extends AbstractEditCommand
    implements PerforceCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeEditCommand( ScmProviderRepository repo, ScmFileSet files )
        throws ScmException
    {
        Commandline cl = createCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir(), files );
        PerforceEditConsumer consumer = new PerforceEditConsumer();
        try
        {
            getLogger().debug( PerforceScmProvider.clean( "Executing " + cl.toString() ) );
            Process proc = cl.execute();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line;
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
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }

        if(consumer.isSuccess()) {
            return new EditScmResult( cl.toString(), consumer.getEdits() );
        } else {
            return new EditScmResult( cl.toString(), "Unable to edit file(s)", consumer.getErrorMessage(), false);
        }
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                 ScmFileSet files )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArgument().setValue( "edit" );

        try
        {
            String candir = workingDirectory.getCanonicalPath();
            List fs = files.getFileList();
            for ( int i = 0; i < fs.size(); i++ )
            {
                File file = (File) fs.get( i );
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
                command.createArgument().setValue( canfile );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return command;
    }
}
