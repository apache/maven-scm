package org.apache.maven.scm.provider.perforce.command.checkout;

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
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mike Perham
 *
 */
public class PerforceCheckOutCommand
    extends AbstractCheckOutCommand
    implements PerforceCommand
{
    private String actualLocation;

    /**
     * Check out the depot code at <code>repo.getPath()</code> into the target
     * directory at <code>files.getBasedir</code>. Perforce does not support
     * arbitrary checkout of versioned source so we need to set up a well-known
     * clientspec which will hold the required info.
     * <p/>
     * 1) A clientspec will be created or updated which holds a temporary
     * mapping from the repo path to the target directory.
     * 2) This clientspec is sync'd to pull all the files onto the client
     * <p/>
     * {@inheritDoc}
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet files,
                                                       ScmVersion version, boolean recursive, boolean shallow )
        throws ScmException
    {
        PerforceScmProviderRepository prepo = (PerforceScmProviderRepository) repo;
        File workingDirectory = new File( files.getBasedir().getAbsolutePath() );

        actualLocation = PerforceScmProvider.getRepoPath( getLogger(), prepo, files.getBasedir() );

        String specname = PerforceScmProvider.getClientspecName( getLogger(), prepo, workingDirectory );
        PerforceCheckOutConsumer consumer = new PerforceCheckOutConsumer( specname, actualLocation );
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Checkout working directory: " + workingDirectory );
        }
        Commandline cl = null;

        // Create or update a clientspec so we can checkout the code to a particular location
        try
        {
            // Ahhh, glorious Perforce.  Create and update of clientspecs is the exact
            // same operation so we don't need to distinguish between the two modes.
            cl = PerforceScmProvider.createP4Command( prepo, workingDirectory );
            cl.createArg().setValue( "client" );
            cl.createArg().setValue( "-i" );
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "Executing: " + PerforceScmProvider.clean( cl.toString() ) );
            }

            String client =
                PerforceScmProvider.createClientspec( getLogger(), prepo, workingDirectory, actualLocation );

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Updating clientspec:\n" + client );
            }

            CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
            int exitCode =
                CommandLineUtils.executeCommandLine( cl, new ByteArrayInputStream( client.getBytes() ), consumer, err );

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

        boolean clientspecExists = consumer.isSuccess();

        // Perform the actual checkout using that clientspec
        try
        {
            if ( clientspecExists )
            {
                try
                {
                    getLastChangelist( prepo, workingDirectory, specname );
                    cl = createCommandLine( prepo, workingDirectory, version, specname );
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Executing: " + PerforceScmProvider.clean( cl.toString() ) );
                    }
                    Process proc = cl.execute();
                    BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
                    String line;
                    while ( ( line = br.readLine() ) != null )
                    {
                        if ( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( "Consuming: " + line );
                        }
                        consumer.consumeLine( line );
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
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Perforce sync complete." );
                    }
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
            }

            if ( consumer.isSuccess() )
            {
                return new CheckOutScmResult( cl.toString(), consumer.getCheckedout() );
            }
            else
            {
                return new CheckOutScmResult( cl.toString(), "Unable to sync.  Are you logged in?",
                                              consumer.getOutput(), consumer.isSuccess() );
            }
        }
        finally
        {
            // See SCM-113
            // Support transient clientspecs as we don't want to create 1000s of permanent clientspecs
            if ( clientspecExists && !prepo.isPersistCheckout() )
            {
                // Delete the clientspec
                InputStreamReader isReader = null;
                InputStreamReader isReaderErr = null;
                try
                {
                    cl = PerforceScmProvider.createP4Command( prepo, workingDirectory );
                    cl.createArg().setValue( "client" );
                    cl.createArg().setValue( "-d" );
                    cl.createArg().setValue( specname );
                    if ( getLogger().isInfoEnabled() )
                    {
                        getLogger().info( "Executing: " + PerforceScmProvider.clean( cl.toString() ) );
                    }
                    Process proc = cl.execute();
                    isReader = new InputStreamReader( proc.getInputStream() );
                    BufferedReader br = new BufferedReader( isReader );
                    String line;
                    while ( ( line = br.readLine() ) != null )
                    {
                        if ( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( "Consuming: " + line );
                        }
                        consumer.consumeLine( line );
                    }
                    br.close();
                    // Read errors from STDERR
                    isReaderErr = new InputStreamReader( proc.getErrorStream() );
                    BufferedReader brErr = new BufferedReader( isReaderErr );
                    while ( ( line = brErr.readLine() ) != null )
                    {
                        if ( getLogger().isDebugEnabled() )
                        {
                            getLogger().debug( "Consuming stderr: " + line );
                        }
                        consumer.consumeLine( line );
                    }
                    brErr.close();
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
                    IOUtil.close( isReader );
                    IOUtil.close( isReaderErr );
                }
            }
            else if ( clientspecExists )
            {
                // SCM-165 Save clientspec in memory so we can reuse it with further commands in this VM.
                System.setProperty( PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY, specname );
            }
        }
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                 ScmVersion version, String specname )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArg().setValue( "-c" + specname );
        command.createArg().setValue( "sync" );

        // Use a simple heuristic to determine if we should use the Force flag
        // on sync.  Forcing sync is a HUGE performance hit but is required in
        // rare instances where source is somehow deleted.  If the target
        // directory is completely empty, assume a force is required.  If
        // not empty, we assume a previous checkout was already done and a normal
        // sync will suffice.
        // SCM-110
        String[] files = workingDirectory.list();
        if ( files == null || files.length == 0 )
        {
            // We need to force so checkout to an empty directory will work.
            command.createArg().setValue( "-f" );
        }

        // Not sure what to do here. I'm unclear whether we should be
        // sync'ing each file individually to the label or just sync the
        // entire contents of the workingDir. I'm going to assume the
        // latter until the exact semantics are clearer.
        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            command.createArg().setValue( "@" + version.getName() );
        }
        return command;
    }

    private int getLastChangelist( PerforceScmProviderRepository repo, File workingDirectory, String specname )
    {
        int lastChangelist = 0;
        try
        {
            Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

            command.createArg().setValue( "-c" + specname );
            command.createArg().setValue( "changes" );
            command.createArg().setValue( "-m1" );
            command.createArg().setValue( "-ssubmitted" );
            command.createArg().setValue( "//" + specname + "/..." );
            getLogger().debug( "Executing: " + PerforceScmProvider.clean( command.toString() ) );
            Process proc = command.execute();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line;

            String lastChangelistStr = "";
            while ( ( line = br.readLine() ) != null )
            {
                getLogger().debug( "Consuming: " + line );
                Pattern changeRegexp = Pattern.compile( "Change (\\d+)" );
                Matcher matcher = changeRegexp.matcher( line );
                if ( matcher.find() )
                {
                    lastChangelistStr = matcher.group( 1 );
                }
            }
            br.close();
            // TODO: Read errors from STDERR?

            try
            {
                lastChangelist = Integer.parseInt( lastChangelistStr );
            }
            catch ( NumberFormatException nfe )
            {
                getLogger().debug( "Could not parse changelist from line " + line );
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

        return lastChangelist;
    }
}
