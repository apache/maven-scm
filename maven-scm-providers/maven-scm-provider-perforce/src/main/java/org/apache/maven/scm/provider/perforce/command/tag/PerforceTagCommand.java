package org.apache.maven.scm.provider.perforce.command.tag;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.command.PerforceInfoCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
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
 * @version $Id: PerforceChangeLogCommand.java 264804 2005-08-30 16:09:04Z
 *          evenisse $
 */
public class PerforceTagCommand
    extends AbstractTagCommand
    implements PerforceCommand
{
    private String actualRepoLocation = null;

    protected ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet files, String tag )
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
        else
        {
            // Unclear what to pass as the first arg
            return new TagScmResult( "p4 label -i", "Tag failed", consumer.getOutput(), false );
        }
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
            getLogger().debug( PerforceScmProvider.clean( "Executing: " + cl.toString() ) );
            Process proc = cl.execute();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                getLogger().debug( "Consuming: " + line );
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
    }

    private void createLabel( ScmProviderRepository repo, ScmFileSet files, String tag, PerforceTagConsumer consumer, boolean lock )
    {
        Commandline cl = createLabelCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir() );
        try
        {
            getLogger().debug( PerforceScmProvider.clean( "Executing: " + cl.toString() ) );
            Process proc = cl.execute();
            OutputStream out = proc.getOutputStream();
            DataOutputStream dos = new DataOutputStream( out );
            String label = createLabelSpecification( (PerforceScmProviderRepository) repo, tag, lock );
            getLogger().debug( "LabelSpec: " + NEWLINE + label );
            dos.write( label.getBytes() );
            dos.close();
            out.close();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line;
            while ( ( line = br.readLine() ) != null )
            {
                getLogger().debug( "Consuming: " + line );
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
    }

    public static Commandline createLabelCommandLine( PerforceScmProviderRepository repo, File workingDirectory )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArgument().setValue( "label" );
        command.createArgument().setValue( "-i" );
        return command;
    }

    public static Commandline createLabelsyncCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                          ScmFileSet files, String tag )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArgument().setValue( "labelsync" );
        command.createArgument().setValue( "-l" );
        command.createArgument().setValue( tag );

        List fs = files.getFileList();
        for ( int i = 0; i < fs.size(); i++ )
        {
            File file = (File) fs.get(i);
            command.createArgument().setValue( file.getName() );
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
        StringBuffer buf = new StringBuffer();
        buf.append( "Label: " ).append( tag ).append( NEWLINE );
        buf.append( "View: " ).append( PerforceScmProvider.getCanonicalRepoPath( actualRepoLocation ) ).append( NEWLINE );
        String username = repo.getUser();
        if ( username == null )
        {
            // I have no idea why but Perforce doesn't default the owner to the current user.
            // Since the user is not explicitly set, we use 'p4 info' to query for the current user.
            username = PerforceInfoCommand.getInfo( this, repo ).getEntry( "User name" );
        }
        buf.append( "Owner: " ).append( username ).append( NEWLINE );
        buf.append( "Options: " ).append( lock ? "" : "un" ).append( "locked" ).append( NEWLINE );
        return buf.toString();
    }
}