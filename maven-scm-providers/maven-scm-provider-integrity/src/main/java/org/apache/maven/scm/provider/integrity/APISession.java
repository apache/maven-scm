package org.apache.maven.scm.provider.integrity;

/**
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

import com.mks.api.CmdRunner;
import com.mks.api.Command;
import com.mks.api.IntegrationPoint;
import com.mks.api.IntegrationPointFactory;
import com.mks.api.Session;
import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.StringUtils;

import java.io.IOException;

/**
 * The APISession provides a wrapper for the MKS JAVA API
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: APISession.java 1.2 2011/08/22 13:06:44EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class APISession
{
    // Store the API Version
    public static final String VERSION =
        IntegrationPointFactory.getAPIVersion().substring( 0, IntegrationPointFactory.getAPIVersion().indexOf( ' ' ) );

    public static final int MAJOR_VERSION = Integer.parseInt( VERSION.substring( 0, VERSION.indexOf( '.' ) ) );

    public static final int MINOR_VERSION =
        Integer.parseInt( VERSION.substring( VERSION.indexOf( '.' ) + 1, VERSION.length() ) );

    // Logs all API work...
    private ScmLogger logger;

    // Class variables used to create an API Session
    private String hostName;

    private int port = 0;

    private String userName;

    private String password;

    // API Specific Objects
    private IntegrationPoint ip;

    private Session session;

    private boolean terminated;

    /**
     * Constructor for the API Session Object
     * Needs an ScmLogger to log all API operations
     *
     * @param logger
     */
    public APISession( ScmLogger logger )
    {
        logger.info( "MKS Integrity API Version: " + VERSION );
        this.logger = logger;
    }

    /**
     * Establishes a connection with the MKS Integrity Server
     *
     * @param host    Hostname or IP address for the MKS Integrity Server
     * @param portNum Port number for the MKS Integrity Server
     * @param user    Username to connect to the MKS Integrity Server
     * @param paswd   Password for the User connecting to the server
     * @throws APIException
     */
    public Response connect( String host, int portNum, String user, String paswd )
        throws APIException
    {
        // Initialize our termination flag...
        terminated = false;
        // Create a local integration point
        ip = IntegrationPointFactory.getInstance().createLocalIntegrationPoint( MAJOR_VERSION, MINOR_VERSION );
        // Set the flag to automatically start the MKS Integrity Client, if not running
        ip.setAutoStartIntegrityClient( true );
        // Use a common session, which means we don't have to manage the password
        if ( null != paswd && paswd.length() > 0 )
        {
            logger.info( "Creating session for " + user + "/" + StringUtils.repeat( "*", paswd.length() ) );
            session = ip.createSession( user, paswd );
            logger.info( "Attempting to establish connection using " + user + "@" + host + ":" + portNum );
        }
        else
        {
            logger.info( "Using a common session.  Connection information is obtained from client preferences" );
            session = ip.getCommonSession();
        }
        // Test the connection to the MKS Integrity Server
        Command ping = new Command( Command.SI, "connect" );
        CmdRunner cmdRunner = session.createCmdRunner();
        // Initialize the command runner with valid connection information
        if ( null != host && host.length() > 0 )
        {
            cmdRunner.setDefaultHostname( host );
        }
        if ( portNum > 0 )
        {
            cmdRunner.setDefaultPort( portNum );
        }
        if ( null != user && user.length() > 0 )
        {
            cmdRunner.setDefaultUsername( user );
        }
        if ( null != paswd && paswd.length() > 0 )
        {
            cmdRunner.setDefaultPassword( paswd );
        }
        // Execute the connection
        Response res = cmdRunner.execute( ping );
        logger.debug( res.getCommandString() + " returned exit code " + res.getExitCode() );
        // Initialize class variables based on the connection information
        hostName = res.getConnectionHostname();
        port = res.getConnectionPort();
        userName = res.getConnectionUsername();
        password = paswd;
        cmdRunner.release();
        logger.info( "Successfully established connection " + userName + "@" + hostName + ":" + port );
        return res;
    }

    /**
     * This function executes a generic API Command
     *
     * @param cmd MKS API Command Object representing an API command
     * @return MKS API Response Object
     * @throws APIException
     */
    public Response runCommand( Command cmd )
        throws APIException
    {
        CmdRunner cmdRunner = session.createCmdRunner();
        cmdRunner.setDefaultHostname( hostName );
        cmdRunner.setDefaultPort( port );
        cmdRunner.setDefaultUsername( userName );
        if ( null != password && password.length() > 0 )
        {
            cmdRunner.setDefaultPassword( password );
        }
        Response res = cmdRunner.execute( cmd );
        logger.debug( res.getCommandString() + " returned exit code " + res.getExitCode() );
        cmdRunner.release();
        return res;
    }

    /**
     * This function executes a generic API Command impersonating another user
     *
     * @param cmd             MKS API Command Object representing a API command
     * @param impersonateUser The user to impersonate
     * @return MKS API Response Object
     * @throws APIException
     */
    public Response runCommandAs( Command cmd, String impersonateUser )
        throws APIException
    {
        CmdRunner cmdRunner = session.createCmdRunner();
        cmdRunner.setDefaultHostname( hostName );
        cmdRunner.setDefaultPort( port );
        cmdRunner.setDefaultUsername( userName );
        if ( null != password && password.length() > 0 )
        {
            cmdRunner.setDefaultPassword( password );
        }
        cmdRunner.setDefaultImpersonationUser( impersonateUser );
        Response res = cmdRunner.execute( cmd );
        logger.debug( res.getCommandString() + " returned exit code " + res.getExitCode() );
        cmdRunner.release();
        return res;
    }

    /**
     * Terminate the API Session and Integration Point
     */
    public void Terminate()
    {
        // Terminate only if not already terminated!
        if ( !terminated )
        {
            try
            {
                if ( null != session )
                {
                    session.release();
                }

                if ( null != ip )
                {
                    ip.release();
                }
                terminated = true;
                logger.info( "Successfully disconnected connection " + userName + "@" + hostName + ":" + port );
            }
            catch ( APIException aex )
            {
                logger.debug( "Caught API Exception when releasing session!" );
                aex.printStackTrace();
            }
            catch ( IOException ioe )
            {
                logger.debug( "Caught IO Exception when releasing session!" );
                ioe.printStackTrace();
            }
        }
    }

    /**
     * Returns the MKS Integrity Hostname for this APISession
     *
     * @return
     */
    public String getHostName()
    {
        return hostName;
    }

    /**
     * Returns the MKS Integrity Port for this APISession
     *
     * @return
     */
    public int getPort()
    {
        return port;
    }

    /**
     * Returns the MKS Integrity User for this APISession
     *
     * @return
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * Returns the MKS Integrity Password for this APISession
     *
     * @return
     */
    public String getPassword()
    {
        if ( null != password && password.length() > 0 )
        {
            return password;
        }
        else
        {
            return "";
        }
    }

    /**
     * Returns the ScmLogger for this APISession
     */
    public ScmLogger getLogger()
    {
        return logger;
    }
}
