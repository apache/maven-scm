package org.apache.maven.scm.provider.perforce.command.checkin;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogCommand.java 264804 2005-08-30 16:09:04Z
 *          evenisse $
 */
public class PerforceCheckInCommand
    extends AbstractCheckInCommand
    implements PerforceCommand
{
    /** {@inheritDoc} */
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet files, String message,
                                                      ScmVersion version )
        throws ScmException
    {
        Commandline cl = createCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir() );
        PerforceCheckInConsumer consumer = new PerforceCheckInConsumer();
        try
        {
            String jobs = System.getProperty( "maven.scm.jobs" );

            getLogger().debug( PerforceScmProvider.clean( "Executing " + cl.toString() ) );
            Process proc = cl.execute();
            OutputStream out = proc.getOutputStream();
            DataOutputStream dos = new DataOutputStream( out );
            PerforceScmProviderRepository prepo = (PerforceScmProviderRepository) repo;
            String changes = createChangeListSpecification( prepo, files, message, PerforceScmProvider.getRepoPath(
                getLogger(), prepo, files.getBasedir() ), jobs );
            getLogger().debug( "Sending changelist:\n" + changes );
            dos.write( changes.getBytes() );
            dos.close();
            out.close();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            BufferedReader brErr = new BufferedReader( new InputStreamReader( proc.getErrorStream() ) );
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                getLogger().debug( "Consuming: " + line );
                consumer.consumeLine( line );
            }
            while ( ( line = brErr.readLine() ) != null )
            {
                getLogger().debug( "Consuming stderr: " + line );
                consumer.consumeLine( line );
            }
        }
        catch ( CommandLineException e )
        {
            getLogger().error( e );
        }
        catch ( IOException e )
        {
            getLogger().error( e );
        }

        return new CheckInScmResult( cl.toString(), consumer.isSuccess() ? "Checkin successful" : "Unable to submit",
                                     consumer.getOutput(), consumer.isSuccess() );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArgument().setValue( "submit" );
        command.createArgument().setValue( "-i" );
        return command;
    }

    private static final String NEWLINE = "\r\n";

    public static String createChangeListSpecification( PerforceScmProviderRepository repo, ScmFileSet files,
                                                        String msg, String canonicalPath, String jobs )
    {
        StringBuffer buf = new StringBuffer();
        buf.append( "Change: new" ).append( NEWLINE ).append( NEWLINE );
        buf.append( "Description:" ).append( NEWLINE ).append( "\t" ).append( msg ).append( NEWLINE ).append( NEWLINE );
        if ( jobs != null && jobs.length() != 0 )
        {
            // Multiple jobs are not handled with this implementation
            buf.append( "Jobs:" ).append( NEWLINE ).append( "\t" ).append( jobs ).append( NEWLINE ).append( NEWLINE );
        }

        buf.append( "Files:" ).append( NEWLINE );
        try
        {
            Set dupes = new HashSet();
            File workingDir = files.getBasedir();
            String candir = workingDir.getCanonicalPath();
            List fs = files.getFileList();
            for ( int i = 0; i < fs.size(); i++ )
            {
                File file = (File) fs.get( i );
                // XXX Submit requires the canonical repository path for each
                // file.
                // It is unclear how to get that from a File object.
                // We assume the repo object has the relative prefix
                // "//depot/some/project"
                // and canfile has the relative path "src/foo.xml" to be added
                // to that prefix.
                // "//depot/some/project/src/foo.xml"
                String canfile = file.getCanonicalPath();
                if ( dupes.contains( canfile ) )
                {
                    // XXX I am seeing duplicate files in the ScmFileSet.
                    // I don't know why this is but we have to weed them out
                    // or Perforce will barf
                    System.err.println( "Skipping duplicate file: " + file );
                    continue;
                }
                dupes.add( canfile );
                if ( canfile.startsWith( candir ) )
                {
                    canfile = canfile.substring( candir.length() + 1 );
                }
                buf.append( "\t" ).append( canonicalPath ).append( "/" ).append( canfile.replace( '\\', '/' ) )
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
