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

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import org.netbeans.lib.cvsclient.CVSRoot;
import org.netbeans.lib.cvsclient.command.CommandAbortedException;
import org.netbeans.lib.cvsclient.connection.AbstractConnection;
import org.netbeans.lib.cvsclient.connection.AuthenticationException;
import org.netbeans.lib.cvsclient.connection.ConnectionModifier;
import org.netbeans.lib.cvsclient.util.LoggedDataInputStream;
import org.netbeans.lib.cvsclient.util.LoggedDataOutputStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ExtConnection
    extends AbstractConnection
{
    private String host;

    private int port;

    private String userName;

    private String password;

    private Connection connection;

    private Session session;

    private BufferedReader stderrReader;

    public ExtConnection( CVSRoot cvsRoot )
    {
        this( cvsRoot.getHostName(), cvsRoot.getPort(), cvsRoot.getUserName(), cvsRoot.getPassword(),
              cvsRoot.getRepository() );
    }

    public ExtConnection( String host, int port, String username, String password, String repository )
    {
        this.userName = username;

        if ( this.userName == null )
        {
            this.userName = System.getProperty( "user.name" );
        }

        this.password = password;

        this.host = host;

        setRepository( repository );

        this.port = port;

        if ( this.port == 0 )
        {
            this.port = 22;
        }
    }

    public void open()
        throws AuthenticationException, CommandAbortedException
    {
        connection = new Connection( host, port );

        /* TODO: add proxy support
        ProxyData proxy = new HTTPProxyData( proxyHost, proxyPort, proxyUserName, proxyPassword );

        connection.setProxyData( proxy );
        */

        try
        {
            // TODO: connection timeout?
            connection.connect();
        }
        catch ( IOException e )
        {
            String message = "Cannot connect. Reason: " + e.getMessage();
            throw new AuthenticationException( message, e, message );
        }

        File privateKey = getPrivateKey();

        try
        {
            boolean authenticated;
            if ( privateKey != null && privateKey.exists() )
            {
                authenticated = connection.authenticateWithPublicKey( userName, privateKey, getPassphrase() );
            }
            else
            {
                authenticated = connection.authenticateWithPassword( userName, password );
            }

            if ( !authenticated )
            {
                String message = "Authentication failed.";
                throw new AuthenticationException( message, message );
            }
        }
        catch ( IOException e )
        {
            closeConnection();
            String message = "Cannot authenticate. Reason: " + e.getMessage();
            throw new AuthenticationException( message, e, message );
        }

        try
        {
            session = connection.openSession();
        }
        catch ( IOException e )
        {
            String message = "Cannot open session. Reason: " + e.getMessage();
            throw new CommandAbortedException( message, message );
        }

        String command = "cvs server";
        try
        {
            session.execCommand( command );
        }
        catch ( IOException e )
        {
            String message = "Cannot execute remote command: " + command;
            throw new CommandAbortedException( message, message );
        }

        InputStream stdout = new StreamGobbler( session.getStdout() );
        InputStream stderr = new StreamGobbler( session.getStderr() );
        stderrReader = new BufferedReader( new InputStreamReader( stderr ) );
        setInputStream( new LoggedDataInputStream( stdout ) );
        setOutputStream( new LoggedDataOutputStream( session.getStdin() ) );
    }

    public void verify()
        throws AuthenticationException
    {
        try
        {
            open();
            verifyProtocol();
            close();
        }
        catch ( Exception e )
        {
            String message = "Failed to verify the connection: " + e.getMessage();
            throw new AuthenticationException( message, e, message );
        }
    }

    private void closeConnection()
    {
        try
        {
            if ( stderrReader != null )
            {
                while ( true )
                {
                    String line = stderrReader.readLine();
                    if ( line == null )
                    {
                        break;
                    }

                    System.err.println( line );
                }
            }
        }
        catch ( IOException e )
        {
            //nothing to do
        }

        if ( session != null )
        {
            System.out.println( "Exit code:" + session.getExitStatus().intValue() );
            session.close();
        }

        if ( connection != null )
        {
            connection.close();
        }

        reset();
    }

    private void reset()
    {
        connection = null;
        session = null;
        stderrReader = null;
        setInputStream( null );
        setOutputStream( null );
    }

    public void close()
        throws IOException
    {
        closeConnection();
    }

    public boolean isOpen()
    {
        return connection != null;
    }

    public int getPort()
    {
        return port;
    }

    public void modifyInputStream( ConnectionModifier modifier )
        throws IOException
    {
        modifier.modifyInputStream( getInputStream() );
    }

    public void modifyOutputStream( ConnectionModifier modifier )
        throws IOException
    {
        modifier.modifyOutputStream( getOutputStream() );
    }

    private File getPrivateKey()
    {
        // If user don't define a password, he want to use a private key
        File privateKey = null;
        if ( password == null )
        {
            String pk = System.getProperty( "maven.scm.cvs.java.ssh.privateKey" );
            if ( pk != null )
            {
                privateKey = new File( pk );
            }
            else
            {
                privateKey = findPrivateKey();
            }
        }
        return privateKey;
    }

    private String getPassphrase()
    {
        String passphrase = System.getProperty( "maven.scm.cvs.java.ssh.passphrase" );

        if ( passphrase == null )
        {
            passphrase = "";
        }

        return passphrase;
    }

    private File findPrivateKey()
    {
        String privateKeyDirectory = System.getProperty( "maven.scm.ssh.privateKeyDirectory" );

        if ( privateKeyDirectory == null )
        {
            privateKeyDirectory = System.getProperty( "user.home" );
        }

        File privateKey = new File( privateKeyDirectory, ".ssh/id_dsa" );

        if ( !privateKey.exists() )
        {
            privateKey = new File( privateKeyDirectory, ".ssh/id_rsa" );
        }

        return privateKey;
    }
}
