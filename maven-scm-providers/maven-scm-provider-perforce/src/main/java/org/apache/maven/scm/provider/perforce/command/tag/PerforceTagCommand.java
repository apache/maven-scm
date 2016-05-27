package org.apache.maven.scm.provider.perforce.command.tag;

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
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.command.PerforceInfoCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

/**
 * @author Mike Perham
 * @author Olivier Lamy
 *
 */
public class PerforceTagCommand
    extends AbstractTagCommand
    implements PerforceCommand
{
    private String actualRepoLocation = null;


    protected ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet files, String tag, String message )
        throws ScmException
    {
        return executeTagCommand( repo, files, tag, new ScmTagParameters( message ) );
    }

    /**
     * {@inheritDoc}
     */
    protected ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet files, String tag,
                                           ScmTagParameters scmTagParameters )
        throws ScmException
    {
        PerforceScmProviderRepository prepo = (PerforceScmProviderRepository) repo;
        actualRepoLocation = PerforceScmProvider.getRepoPath( getLogger(), prepo, files.getBasedir() );

        PerforceTagConsumer consumer = new PerforceTagConsumer();
        createLabel( repo, files, tag, consumer, false );
        if ( consumer.isSuccess() )
        {
            syncLabel( repo, files, tag, consumer );
        }
        if ( consumer.isSuccess() )
        {
            // Now update the label if we need to lock it
            if ( shouldLock() )
            {
                consumer = new PerforceTagConsumer();
                createLabel( repo, files, tag, consumer, true );
            }
        }

        if ( consumer.isSuccess() )
        {
            // Unclear what to pass as the first arg
            return new TagScmResult( "p4 label -i", consumer.getTagged() );
        }

        // Unclear what to pass as the first arg
        return new TagScmResult( "p4 label -i", "Tag failed", consumer.getOutput(), false );
    }

    private boolean shouldLock()
    {
        return Boolean.valueOf( System.getProperty( "maven.scm.locktag", "true" ) ).booleanValue();
    }

    private void syncLabel( ScmProviderRepository repo, ScmFileSet files, String tag, PerforceTagConsumer consumer )
    {
        Commandline cl =
            createLabelsyncCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir(), files, tag );
        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( PerforceScmProvider.clean( "Executing: " + cl.toString() ) );
            }
            CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
            int exitCode = CommandLineUtils.executeCommandLine( cl, consumer, err );

            if ( exitCode != 0 )
            {
                String cmdLine = CommandLineUtils.toString( cl.getCommandline() );

                StringBuilder msg = new StringBuilder( "Exit code: " + exitCode + " - " + err.getOutput() );
                msg.append( '\n' );
                msg.append( "Command line was:" + cmdLine );

                throw new CommandLineException( msg.toString() );
            }
        }
        catch ( CommandLineException e )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "CommandLineException " + e.getMessage(), e );
            }
        }
    }

    private void createLabel( ScmProviderRepository repo, ScmFileSet files, String tag, PerforceTagConsumer consumer,
                              boolean lock )
    {
        Commandline cl = createLabelCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir() );
        DataOutputStream dos = null;
        InputStreamReader isReader = null;
        InputStreamReader isReaderErr = null;
        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( PerforceScmProvider.clean( "Executing: " + cl.toString() ) );
            }
            Process proc = cl.execute();
            OutputStream out = proc.getOutputStream();
            dos = new DataOutputStream( out );
            String label = createLabelSpecification( (PerforceScmProviderRepository) repo, tag, lock );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "LabelSpec: " + NEWLINE + label );
            }
            dos.write( label.getBytes() );
            dos.close();
            out.close();
            // TODO find & use a less naive InputStream multiplexer
            isReader = new InputStreamReader( proc.getInputStream() );
            isReaderErr = new InputStreamReader( proc.getErrorStream() );
            BufferedReader stdout = new BufferedReader( isReader );
            BufferedReader stderr = new BufferedReader( isReaderErr );
            String line;
            while ( ( line = stdout.readLine() ) != null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Consuming stdout: " + line );
                }
                consumer.consumeLine( line );
            }
            while ( ( line = stderr.readLine() ) != null )
            {
                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "Consuming stderr: " + line );
                }
                consumer.consumeLine( line );
            }
            stderr.close();
            stdout.close();
        }
        catch ( CommandLineException e )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "CommandLineException " + e.getMessage(), e );
            }
        }
        catch ( IOException e )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "IOException " + e.getMessage(), e );
            }
        }
        finally
        {
            IOUtil.close( dos );
            IOUtil.close( isReader );
            IOUtil.close( isReaderErr );
        }
    }

    public static Commandline createLabelCommandLine( PerforceScmProviderRepository repo, File workingDirectory )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArg().setValue( "label" );
        command.createArg().setValue( "-i" );
        return command;
    }

    public static Commandline createLabelsyncCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                          ScmFileSet files, String tag )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArg().setValue( "labelsync" );
        command.createArg().setValue( "-l" );
        command.createArg().setValue( tag );

        List<File> fs = files.getFileList();
        for ( File file : fs )
        {
            command.createArg().setValue( file.getPath() );
        }
        return command;
    }

    private static final String NEWLINE = "\r\n";

    /*
     * Label: foo-label
     * View: //depot/path/to/repos/...
     * Owner: mperham
     */
    public String createLabelSpecification( PerforceScmProviderRepository repo, String tag, boolean lock )
    {
        StringBuilder buf = new StringBuilder();
        buf.append( "Label: " ).append( tag ).append( NEWLINE );
        buf.append( "View: " ).append( PerforceScmProvider.getCanonicalRepoPath( actualRepoLocation ) ).append(
            NEWLINE );
        String username = repo.getUser();
        if ( username == null )
        {
            // I have no idea why but Perforce doesn't default the owner to the current user.
            // Since the user is not explicitly set, we use 'p4 info' to query for the current user.
            username = PerforceInfoCommand.getInfo( getLogger(), repo ).getEntry( "User name" );
        }
        buf.append( "Owner: " ).append( username ).append( NEWLINE );
        buf.append( "Options: " ).append( lock ? "" : "un" ).append( "locked" ).append( NEWLINE );
        return buf.toString();
    }
}
