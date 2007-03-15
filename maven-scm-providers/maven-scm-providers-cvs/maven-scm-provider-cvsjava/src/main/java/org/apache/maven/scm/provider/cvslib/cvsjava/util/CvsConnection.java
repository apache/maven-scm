package org.apache.maven.scm.provider.cvslib.cvsjava.util;

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

import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.Client;
import org.netbeans.lib.cvsclient.admin.StandardAdminHandler;
import org.netbeans.lib.cvsclient.command.Command;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.command.CommandException;
import org.netbeans.lib.cvsclient.command.GlobalOptions;
import org.netbeans.lib.cvsclient.commandLine.CommandFactory;
import org.netbeans.lib.cvsclient.commandLine.GetOpt;
import org.netbeans.lib.cvsclient.connection.AbstractConnection;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.Connection;
import org.netbeans.lib.cvsclient.connection.ConnectionFactory;
import org.netbeans.lib.cvsclient.connection.PServerConnection;
import org.netbeans.lib.cvsclient.connection.StandardScrambler;
import org.netbeans.lib.cvsclient.event.CVSListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * A Cvs connection that simulates a command line interface.
 *
 * @author <a href="mailto:epugh@upstate.com">Eric Pugh</a>
 */
public class CvsConnection
{

    /**
     * The path to the repository on the server
     */
    private String repository;

    /**
     * The local path to use to perform operations (the top level)
     */
    private String localPath;

    /**
     * The connection to the server
     */
    private Connection connection;

    /**
     * The client that manages interactions with the server
     */
    private Client client;

    /**
     * The global options being used. GlobalOptions are only global for a
     * particular command.
     */
    private GlobalOptions globalOptions;

    private CvsConnection()
    {
    }

    /**
     * Execute a configured CVS command
     *
     * @param command the command to execute
     * @throws CommandException if there is an error running the command
     */
    public boolean executeCommand( Command command )
        throws CommandException, AuthenticationException
    {
        return client.executeCommand( command, globalOptions );
    }

    public void setRepository( String repository )
    {
        this.repository = repository;
    }

    public void setLocalPath( String localPath )
    {
        this.localPath = localPath;
    }

    public void setGlobalOptions( GlobalOptions globalOptions )
    {
        this.globalOptions = globalOptions;
    }

    /**
     * Creates the connection and the client and connects.
     */
    private void connect( CVSRoot root, String password )
        throws IllegalArgumentException, AuthenticationException, CommandAbortedException
    {
        if ( CVSRoot.METHOD_EXT.equals( root.getMethod() ) )
        {
            String cvs_rsh = System.getProperty( "maven.scm.cvs.java.cvs_rsh" );
            if ( cvs_rsh == null )
            {
                try
                {
                    cvs_rsh = CommandLineUtils.getSystemEnvVars().getProperty( "CVS_RSH" );
                }
                catch ( IOException e )
                {
                }
            }

            if ( cvs_rsh != null )
            {
                if ( cvs_rsh.indexOf( ' ' ) < 0 )
                {
                    //cvs_rsh should be 'rsh' or 'ssh'
                    //Complete the command to use
                    String username = root.getUserName();
                    if ( username == null )
                    {
                        username = System.getProperty( "user.name" );
                    }

                    cvs_rsh += " " + username + "@" + root.getHostName() + " cvs server";
                }

                AbstractConnection conn = new org.netbeans.lib.cvsclient.connection.ExtConnection( cvs_rsh );
                conn.setRepository( root.getRepository() );
                connection = conn;
            }
            else
            {
                connection = new ExtConnection( root );
            }
        }
        else
        {
            connection = ConnectionFactory.getConnection( root );
            if ( CVSRoot.METHOD_PSERVER.equals( root.getMethod() ) )
            {
                ( (PServerConnection) connection ).setEncodedPassword( password );
            }
        }
        connection.open();

        client = new Client( connection, new StandardAdminHandler() );
        client.setLocalPath( localPath );
    }

    private void addListener( CVSListener listener )
    {
        if ( client != null )
        {
            // add a listener to the client
            client.getEventManager().addCVSListener( listener );
        }
    }

    /**
     * Obtain the CVS root, either from the -D option cvs.root or from the CVS
     * directory
     *
     * @return the CVSRoot string
     */
    private static String getCVSRoot( String workingDir )
    {
        String root = null;
        BufferedReader r = null;
        if ( workingDir == null )
        {
            workingDir = System.getProperty( "user.dir" );
        }
        try
        {
            File f = new File( workingDir );
            File rootFile = new File( f, "CVS/Root" );
            if ( rootFile.exists() )
            {
                r = new BufferedReader( new FileReader( rootFile ) );
                root = r.readLine();
            }
        }
        catch ( IOException e )
        {
            // ignore
        }
        finally
        {
            try
            {
                if ( r != null )
                {
                    r.close();
                }
            }
            catch ( IOException e )
            {
                System.err.println( "Warning: could not close CVS/Root file!" );
            }
        }
        if ( root == null )
        {
            root = System.getProperty( "cvs.root" );
        }
        return root;
    }

    /**
     * Process global options passed into the application
     *
     * @param args          the argument list, complete
     * @param globalOptions the global options structure that will be passed to
     *                      the command
     */
    private static int processGlobalOptions( String[] args, GlobalOptions globalOptions )
    {
        final String getOptString = globalOptions.getOptString();
        GetOpt go = new GetOpt( args, getOptString );
        int ch;
        while ( ( ch = go.getopt() ) != GetOpt.optEOF )
        {
            //System.out.println("Global option '"+((char) ch)+"',
            // '"+go.optArgGet()+"'");
            String arg = go.optArgGet();
            boolean success = globalOptions.setCVSCommand( (char) ch, arg );
            if ( !success )
            {
                throw new IllegalArgumentException( "Failed to set CVS Command: -" + ch + " = " + arg );
            }
        }

        return go.optIndexGet();
    }

    /**
     * Lookup the password in the .cvspass file. This file is looked for in the
     * user.home directory if the option cvs.passfile is not set
     *
     * @param cvsRoot the CVS root for which the password is being searched
     * @return the password, scrambled
     */
    private static String lookupPassword( String cvsRoot, ScmLogger logger )
    {
        File passFile =
            new File( System.getProperty( "cvs.passfile", System.getProperty( "user.home" ) + "/.cvspass" ) );

        BufferedReader reader = null;
        String password = null;

        try
        {
            reader = new BufferedReader( new FileReader( passFile ) );
            password = processCvspass( cvsRoot, reader );
        }
        catch ( IOException e )
        {
            logger.warn( "Could not read password for '" + cvsRoot + "' from '" + passFile + "'", e );
            return null;
        }
        finally
        {
            if ( reader != null )
            {
                try
                {
                    reader.close();
                }
                catch ( IOException e )
                {
                    logger.error( "Warning: could not close password file." );
                }
            }
        }
        if ( password == null )
        {
            logger.error( "Didn't find password for CVSROOT '" + cvsRoot + "'." );
        }
        return password;
    }

    /**
     * Read in a list of return delimited lines from .cvspass and retreive
     * the password.  Return null if the cvsRoot can't be found.
     *
     * @param cvsRoot the CVS root for which the password is being searched
     * @param reader  A buffered reader of lines of cvspass information
     * @return The password, or null if it can't be found.
     * @throws IOException
     */
    static String processCvspass( String cvsRoot, BufferedReader reader )
        throws IOException
    {
        String line;
        String password = null;
        while ( ( line = reader.readLine() ) != null )
        {
            if ( line.startsWith( "/" ) )
            {
                String[] cvspass = StringUtils.split( line, " " );
                String cvspassRoot = cvspass[1];
                if ( compareCvsRoot( cvsRoot, cvspassRoot ) )
                {
                    int index = line.indexOf( cvspassRoot ) + cvspassRoot.length() + 1;
                    password = line.substring( index );
                    break;
                }
            }
            else if ( line.startsWith( cvsRoot ) )
            {
                password = line.substring( cvsRoot.length() + 1 );
                break;
            }
        }
        return password;
    }

    static boolean compareCvsRoot( String cvsRoot, String target )
    {
        String s1 = completeCvsRootPort( cvsRoot );
        String s2 = completeCvsRootPort( target );
        return s1 != null && s1.equals( s2 );

    }

    private static String completeCvsRootPort( String cvsRoot )
    {
        String result = cvsRoot;
        int idx = cvsRoot.indexOf( ':' );
        for ( int i = 0; i < 2 && idx != -1; i++ )
        {
            idx = cvsRoot.indexOf( ':', idx + 1 );
        }
        if ( idx != -1 && cvsRoot.charAt( idx + 1 ) == '/' )
        {
            StringBuffer sb = new StringBuffer();
            sb.append( cvsRoot.substring( 0, idx + 1 ) );
            sb.append( "2401" );
            sb.append( cvsRoot.substring( idx + 1 ) );
            result = sb.toString();
        }
        return result;

    }

    /**
     * Process the CVS command passed in args[] array with all necessary
     * options. The only difference from main() method is, that this method
     * does not exit the JVM and provides command output.
     *
     * @param args The command with options
     */
    public static boolean processCommand( String[] args, String localPath, CVSListener listener, ScmLogger logger )
        throws Exception
    {
        // Set up the CVSRoot. Note that it might still be null after this
        // call if the user has decided to set it with the -d command line
        // global option
        GlobalOptions globalOptions = new GlobalOptions();
        globalOptions.setCVSRoot( getCVSRoot( localPath ) );

        // Set up any global options specified. These occur before the
        // name of the command to run
        int commandIndex;

        try
        {
            commandIndex = processGlobalOptions( args, globalOptions );
        }
        catch ( IllegalArgumentException e )
        {
            logger.error( "Invalid argument: " + e );
            return false;
        }

        // if we don't have a CVS root by now, the user has messed up
        if ( globalOptions.getCVSRoot() == null )
        {
            logger.error( "No CVS root is set. Check your <repository> information in the POM." );
            return false;
        }

        // parse the CVS root into its constituent parts
        CVSRoot root;
        final String cvsRoot = globalOptions.getCVSRoot();
        try
        {
            root = CVSRoot.parse( cvsRoot );
        }
        catch ( IllegalArgumentException e )
        {
            logger.error( "Incorrect format for CVSRoot: " + cvsRoot + "\nThe correct format is: " +
                "[:method:][[user][:password]@][hostname:[port]]/path/to/repository" +
                "\nwhere \"method\" is pserver." );
            return false;
        }

        final String command = args[commandIndex];

        // this is not login, but a 'real' cvs command, so construct it,
        // set the options, and then connect to the server and execute it

        Command c;
        try
        {
            c = CommandFactory.getDefault().createCommand( command, args, ++commandIndex, globalOptions, localPath );
        }
        catch ( IllegalArgumentException e )
        {
            logger.error( "Illegal argument: " + e.getMessage() );
            return false;
        }

        String password = null;

        if ( CVSRoot.METHOD_PSERVER.equals( root.getMethod() ) )
        {
            password = root.getPassword();
            if ( password != null )
            {
                password = StandardScrambler.getInstance().scramble( password );
            }
            else
            {
                password = lookupPassword( cvsRoot, logger );
                if ( password == null )
                {
                    password = StandardScrambler.getInstance().scramble( "" );
                    // an empty password
                }
            }
        }
        CvsConnection cvsCommand = new CvsConnection();
        cvsCommand.setGlobalOptions( globalOptions );
        cvsCommand.setRepository( root.getRepository() );
        // the local path is just the path where we executed the
        // command. This is the case for command-line CVS but not
        // usually for GUI front-ends
        cvsCommand.setLocalPath( localPath );

        cvsCommand.connect( root, password );
        cvsCommand.addListener( listener );
        logger.debug( "Executing CVS command: " + c.getCVSCommand() );
        return cvsCommand.executeCommand( c );
    }
}
