package org.apache.maven.scm.provider.perforce.command.checkout;

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
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
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
public class PerforceCheckOutCommand
    extends AbstractCheckOutCommand
    implements PerforceCommand
{

    /**
     * Check out the depot code at <code>repo.getPath()</code> into the target
     * directory at <code>files.getBasedir</code>. Perforce does not support
     * arbitrary checkout of versioned source so we need to set up a well-known
     * clientspec which will hold the required info.
     * 
     * 1) A clientspec will be created or updated which holds a temporary
     * mapping from the repo path to the target directory. 
     * 2) This clientspec is sync'd to pull all the files onto the client
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet files, String tag )
        throws ScmException
    {
        PerforceScmProviderRepository prepo = (PerforceScmProviderRepository) repo;
        String specname = getClientspecName();
        PerforceCheckOutConsumer consumer = new PerforceCheckOutConsumer( specname, prepo.getPath() );
        Commandline cl = null;
        
        try
        {
            // Ahhh, glorious Perforce.  Create and update of clientspecs is the exact
            // same operation so we don't need to distinguish between the two modes. 
            cl = PerforceScmProvider.createP4Command( prepo, files.getBasedir() );
            cl.createArgument().setValue( "client" );
            cl.createArgument().setValue( "-i" );
            Process proc = cl.execute();

            // Write clientspec to STDIN
            OutputStream out = proc.getOutputStream();
            DataOutputStream dos = new DataOutputStream( out );
            String client = createClientspec( specname, files.getBasedir().getCanonicalPath(), prepo.getPath() );
            getLogger().debug( "Updating clientspec:\n" + client );
            dos.write( client.getBytes() );
            dos.close();
            out.close();

            // Read result from STDOUT
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line = null;
            while ( ( line = br.readLine() ) != null )
            {
                getLogger().debug( "Consuming: " + line );
                consumer.consumeLine( line );
            }
        }
        catch ( IOException e )
        {
            getLogger().error( e );
        }
        catch ( CommandLineException e )
        {
            getLogger().error( e );
        }

        if ( consumer.isSuccess() )
        {
            cl = createCommandLine( prepo, files.getBasedir(), tag, specname );
            try
            {
                getLogger().debug( "Executing " + cl.toString() );
                Process proc = cl.execute();
                BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
                String line = null;
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

        if ( consumer.isSuccess() )
        {
            return new CheckOutScmResult( cl.toString(), consumer.getCheckedout() );
        }
        else
        {
            return new CheckOutScmResult( cl.toString(), "Unable to sync", consumer.getOutput(), consumer.isSuccess() );
        }
    }

    private static final String NEWLINE = "\r\n";

    /* 
     * Clientspec name can be overridden with the system property below.  I don't
     * know of any way for this code to get access to maven's settings.xml so this
     * is the best I can do.
     * 
     * Sample clientspec:

     Client: mperham-mikeperham-dt-maven
     Root:
     d:\temp\target
     View:
     //depot/sandbox/mperham/tsa/tsa-domain/... //mperham-mikeperham-dt-maven/...

     */
    public static String createClientspec( String specname, String workDir, String repoPath )
    {
        String clientspecName = getClientspecName();
        StringBuffer buf = new StringBuffer();
        buf.append( "Client: " ).append( clientspecName ).append( NEWLINE );
        buf.append( "Root:" ).append( NEWLINE );
        buf.append( "\t" ).append( workDir ).append( NEWLINE );
        buf.append( "View:" ).append( NEWLINE );
        buf.append( "\t" ).append( repoPath ).append( "/... //" ).append( clientspecName ).append( "/..." )
            .append( NEWLINE );
        return buf.toString();
    }

    private static String getClientspecName()
    {
        String clientspecName = System.getProperty( "maven.scm.perforce.clientspec.name",
                                                    generateDefaultClientspecName() );
        return clientspecName;
    }

    private static String generateDefaultClientspecName()
    {
        String username = System.getProperty( "user.name", "nouser" );
        String hostname = "nohost";
        try
        {
            hostname = InetAddress.getLocalHost().getHostName();
        }
        catch ( UnknownHostException e )
        {
            // Should never happen
            throw new RuntimeException( e );
        }
        return username + "-" + hostname + "-maven";
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory, String tag,
                                                String specname )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArgument().setValue( "-c" + specname );
        command.createArgument().setValue( "sync" );
        // Not sure what to do here. I'm unclear whether we should be
        // sync'ing each file individually to the label or just sync the
        // entire contents of the workingDir. I'm going to assume the
        // latter until the exact semantics are clearer.
        command.createArgument().setValue( "..." + ( tag != null ? "@" + tag : "" ) );
        return command;
    }

}
