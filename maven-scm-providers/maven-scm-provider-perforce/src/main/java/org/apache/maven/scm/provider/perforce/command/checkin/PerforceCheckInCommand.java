package org.apache.maven.scm.provider.perforce.command.checkin;

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
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
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
public class PerforceCheckInCommand
    extends AbstractCheckInCommand
    implements PerforceCommand
{

    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet files, String message,
                                                     String something )
        throws ScmException
    {
        Commandline cl = createCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir(), files );
        PerforceCheckInConsumer consumer = new PerforceCheckInConsumer();
        try
        {
            Process proc = cl.execute();
            DataOutputStream dos = new DataOutputStream( proc.getOutputStream() );
            dos.writeUTF( createChangeListSpecification( (PerforceScmProviderRepository) repo, files, message ) );
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

        return new CheckInScmResult( cl.toString(), consumer.isSuccess() ? "Checkin successful" : "Unable to submit",
                                     consumer.getOutput(), consumer.isSuccess() );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                ScmFileSet files )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArgument().setValue( "submit" );
        command.createArgument().setValue( "-i" );
        return command;
    }

    private static final String NEWLINE = "\r\n";

    public static String createChangeListSpecification( PerforceScmProviderRepository repo, ScmFileSet files, String msg )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "Change: new" ).append( NEWLINE ).append( NEWLINE );
        buf.append( "Description:" ).append( NEWLINE ).append( "\t" ).append( msg ).append( NEWLINE ).append( NEWLINE );
        buf.append( "Files:" ).append( NEWLINE );
        try
        {
            File workingDir = files.getBasedir();
            String candir = workingDir.getCanonicalPath();
            File[] fs = files.getFiles();
            for ( int i = 0; i < fs.length; i++ )
            {
                File file = fs[i];
                // XXX Submit requires the canonical repository path for each
                // file.
                // It is unclear how to get that from a File object.
                // We assume the repo object has the relative prefix
                // "//depot/some/project"
                // and canfile has the relative path "src/foo.xml" to be added
                // to that prefix.
                // "//depot/some/project/src/foo.xml"
                String canfile = file.getCanonicalPath();
                if ( canfile.startsWith( candir ) )
                {
                    canfile = canfile.substring( candir.length() + 1 );
                }
                buf.append( "\t" ).append( repo.getPath() ).append( "/" ).append( canfile.replace( '\\', '/' ) )
                    .append( NEWLINE );
            }
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
        return buf.toString();
    }
}