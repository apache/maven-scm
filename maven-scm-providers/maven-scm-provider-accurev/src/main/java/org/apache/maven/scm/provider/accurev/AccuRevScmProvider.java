package org.apache.maven.scm.provider.accurev;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.commands.add.AccuRevAddCommand;
import org.apache.maven.scm.provider.accurev.commands.login.AccuRevLoginCommand;
import org.apache.maven.scm.provider.accurev.commands.checkout.BaseAccuRevCheckOutCommand;
import org.apache.maven.scm.provider.accurev.commands.checkout.AccuRevCheckOutWorkspaceCommand;
import org.apache.maven.scm.provider.accurev.commands.checkout.AccuRevCheckOutUsingPopCommand;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * Implementation of Accurev integration with Maven SCM
 *
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="accurev"
 */
public class AccuRevScmProvider
    extends AbstractScmProvider
{
    private final String accurevExecutable;

    private AccuRevAddCommand addCommand;

    private AccuRevLoginCommand loginCommand;

    private BaseAccuRevCheckOutCommand[] checkOutCommands;

    private static final String CHECKOUT_METHOD_PARAM_NAME = "checkoutMethod";

    private static final String CHECKOUT_METHOD_SYS_PROPERTY = "accurev.checkout.method";

    public AccuRevScmProvider()
    {
        boolean isWindows = System.getProperty( "os.name" ).toLowerCase().indexOf( "windows" ) != -1;
        this.accurevExecutable = resolveAccurevExecutable( isWindows );
    }

    /** {@inheritDoc} */
    public String getScmType()
    {
        return "accurev";
    }

    protected String getAccurevExecutable()
    {
        return this.accurevExecutable;
    }

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        AccuRevScmProviderRepository rep = new AccuRevScmProviderRepository();

        int atSignIdx = scmSpecificUrl.indexOf( '@' );
        if ( atSignIdx != -1 )
        {
            //User, password, host and port are specified
            String beforeAt = scmSpecificUrl.substring( 0, atSignIdx );
            String afterAt = scmSpecificUrl.substring( atSignIdx + 1 );

            LinkedList parts = splitString( beforeAt, delimiter, true );
            if ( parts.isEmpty() )
            {
                throw new ScmRepositoryException( "Username is required" );
            }
            rep.setUser( (String) parts.removeFirst() );
            if ( !parts.isEmpty() )
            {
                rep.setPassword( (String) parts.removeFirst() );
            }
            int hostEndIdx = afterAt.indexOf( delimiter );
            if ( hostEndIdx == -1 )
            {
                throw new ScmRepositoryException( "Invalid SCM URL" );
            }
            String host = afterAt.substring( 0, hostEndIdx );
            rep.setHost( host );

            int idx = afterAt.indexOf( delimiter, hostEndIdx + 1 );
            if ( idx == -1 )
            {
                throw new ScmRepositoryException( "Invalid SCM URL" );
            }
            String eitherPortOrNextToken = afterAt.substring( hostEndIdx + 1, idx );
            try
            {
                rep.setPort( Integer.parseInt( eitherPortOrNextToken ) );
            }
            catch ( NumberFormatException e )
            {
                idx = hostEndIdx;
            }
            scmSpecificUrl = afterAt.substring( idx + 1 );
        }
        int paramStartIdx = scmSpecificUrl.indexOf( '?' );
        if ( paramStartIdx != -1 )
        {
            String params = scmSpecificUrl.substring( paramStartIdx + 1 );
            scmSpecificUrl = scmSpecificUrl.substring( 0, paramStartIdx );
            processParams( params, rep );

        }
        processDepotStreamAndWorkspace( scmSpecificUrl, delimiter, rep );

        String checkoutMethodParam = (String) rep.getParams().get( CHECKOUT_METHOD_PARAM_NAME );
        if ( StringUtils.isNotEmpty( checkoutMethodParam ) )
        {
            //Set checkout method from the SCM URL parameter
            rep.setCheckoutMethod( checkoutMethodParam.trim() );
        }
        if ( rep.getCheckoutMethod() == null )
        {
            //Get from system property or use "pop" by default
            rep.setCheckoutMethod( System.getProperty( CHECKOUT_METHOD_SYS_PROPERTY, "pop" ) );
        }
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Repository created: " + rep.toString() );
        }
        return rep;
    }

    private void processParams( String params, AccuRevScmProviderRepository rep )
    {
        //Parse params
        String[] paramKeyValuePairs = StringUtils.split( params, "&" );
        for ( int i = 0; i < paramKeyValuePairs.length; i++ )
        {
            String keyValuePair = paramKeyValuePairs[i];
            int delimiterIdx = keyValuePair.indexOf( '=' );
            if ( delimiterIdx == -1 )
            {
                getLogger().warn( "Invalid parameter \"" + keyValuePair + "\" at position " + i );
                continue;
            }
            String key = keyValuePair.substring( 0, delimiterIdx );
            String value = keyValuePair.substring( delimiterIdx + 1 );
            //Store parameter
            rep.getParams().put( key, value );
        }
    }

    /** {@inheritDoc} */
    protected LoginScmResult login( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        if ( null == this.loginCommand )
        { //Lazy command instantiation
            this.loginCommand = new AccuRevLoginCommand( this.accurevExecutable );
            this.loginCommand.setLogger( getLogger() );
        }
        return (LoginScmResult) this.loginCommand.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                          CommandParameters parameters )
        throws ScmException
    {

        AccuRevScmProviderRepository rep = (AccuRevScmProviderRepository) repository;
        getLogger().debug( "accurev.checkout.method = " + rep.getCheckoutMethod() );
        BaseAccuRevCheckOutCommand[] checkOutCmds = getCheckoutCommands();
        //Find check-out command that supports specified method
        for ( int i = 0; i < checkOutCmds.length; i++ )
        {
            BaseAccuRevCheckOutCommand checkOutCommand = checkOutCmds[i];
            if ( checkOutCommand.getMethodName().equalsIgnoreCase( rep.getCheckoutMethod() ) )
            {
                return (CheckOutScmResult) checkOutCommand.execute( repository, fileSet, parameters );
            }
        }
        throw new ScmRepositoryException( "accurev.checkout.method=" + rep.getCheckoutMethod() + " is not supported" );
    }

    /** {@inheritDoc} */
    protected AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        //Lazy command creation
        if ( null == addCommand )
        {
            addCommand = new AccuRevAddCommand( this.accurevExecutable );
            addCommand.setLogger( getLogger() );
        }
        return (AddScmResult) addCommand.execute( repository, fileSet, parameters );
    }

    private BaseAccuRevCheckOutCommand[] getCheckoutCommands()
    {
        if ( null == this.checkOutCommands )
        { //Lazy command instantiation
            this.checkOutCommands = buildCheckOutCommands();
        }
        return this.checkOutCommands;
    }

    protected BaseAccuRevCheckOutCommand[] buildCheckOutCommands()
    {
        BaseAccuRevCheckOutCommand[] commands = {
            new AccuRevCheckOutUsingPopCommand( this.accurevExecutable ),
            new AccuRevCheckOutWorkspaceCommand( this.accurevExecutable ) };
        for ( int i = 0; i < commands.length; i++ )
        {
            BaseAccuRevCheckOutCommand command = commands[i];
            command.setLogger( getLogger() );
        }
        return commands;
    }

    /**
     * Append host (-H) parameter to the params list if the repository has this information
     *
     * @param repository AccuRevScmProviderRepository
     * @param params     Parameters
     */
    public static void appendHostToParamsIfNeeded( AccuRevScmProviderRepository repository, List params )
    {
        if ( repository.getHost() != null )
        {
            params.add( "-H" );
            params.add( repository.getHost() + ":" + String.valueOf( repository.getPort() ) );
        }
    }

    /**
     * Resolves accurev executable
     *
     * @param windows Is OS is windows
     * @return AccuRev executable
     * @throws IllegalStateException If the executable cannot be resolved
     */
    private static String resolveAccurevExecutable( boolean windows )
    {
        String executable = "accurev";
        //Append ".exe" suffix if the OS is Windows
        if ( windows )
        {
            executable += ".exe";
        }
        //Grab exeucutable from system variable if specified
        String accurevExecutable = System.getProperty( "accurevExecutable" );
        if ( accurevExecutable != null )
        {
            executable = accurevExecutable;
        }
        return executable;
    }

    /**
     * Split the given string with separator char
     *
     * @param string        String to be splitted
     * @param separatorChar separator char
     * @param blankIsNull   If the parameter is true then if the substring is blank then it will be considered as null
     * @return Linked list which contains strings
     */
    private static LinkedList splitString( String string, char separatorChar, boolean blankIsNull )
    {
        LinkedList parts = new LinkedList();
        int fromIdx = 0;
        for ( int idx; ( idx = string.indexOf( separatorChar, fromIdx ) ) != -1; fromIdx = ++idx )
        {
            parts.add( getSubstring( string, fromIdx, idx, blankIsNull ) );
        }
        //Add last token
        parts.add( getSubstring( string, fromIdx, string.length(), blankIsNull ) );
        return parts;
    }

    /**
     * Get substring from string
     *
     * @param string      source string
     * @param fromIdx     from index
     * @param toIdx       to index
     * @param blankIsNull If the parameter is true then if the substring is blank then null will be returned
     * @return Substring or the null if the substring is empty and the blankIsNull parameter is true
     * @see String#substring(int,int)
     * @see StringUtils#isBlank(String)
     */
    private static String getSubstring( String string, int fromIdx, int toIdx, boolean blankIsNull )
    {
        String substring = string.substring( fromIdx, toIdx );
        return ( blankIsNull && StringUtils.isEmpty( substring ) ) ? null : substring;
    }

    /**
     * Process depot, stream and workspace from the given string
     *
     * @param depotStreamWorkspace String that contains depot, stream and workspace name information
     * @param delimeter            Delimeter char
     * @param repRef               AccuRevScmProviderRepository reference where the information will be stored
     * @throws ScmRepositoryException If there was a validation error while processing the URL
     */
    private void processDepotStreamAndWorkspace( final String depotStreamWorkspace, final char delimeter,
                                                 final AccuRevScmProviderRepository repRef )
        throws ScmRepositoryException
    {
        LinkedList parts = splitString( depotStreamWorkspace, delimeter, true );
        if ( parts.size() < 2 )
        {
            throw new ScmRepositoryException( "Invalid scmUrl. Depot and stream names are required" );
        }
        //Set depot
        String depot = (String) parts.removeFirst();
        if ( null == depot )
        {
            throw new ScmRepositoryException( "Depot name is missing" );
        }
        repRef.setDepot( depot );

        //Set stream
        String streamName = (String) parts.removeFirst();
        if ( null == streamName )
        {
            throw new ScmRepositoryException( "Stream name is missing" );
        }
        repRef.setStreamName( streamName );

        //Set workspace if specified
        if ( !parts.isEmpty() )
        {
            repRef.setWorkspaceName( (String) parts.removeFirst() );
        }

        if ( !parts.isEmpty() )
        {
            throw new ScmRepositoryException( "Invalid workspace assosiation path: " + depotStreamWorkspace
                + ". Should be in format \"<depot>" + delimeter + "<stream>" + delimeter + "<workspace>\"" );
        }
        if ( repRef.getWorkspaceName() == null )
        {
            //TODO Use 'accurev show wspaces' to resolve the workspace name by the workspace path
        }
    }

}
