package org.apache.maven.scm.provider.jazz;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.add.JazzAddCommand;
import org.apache.maven.scm.provider.jazz.command.blame.JazzBlameCommand;
import org.apache.maven.scm.provider.jazz.command.branch.JazzBranchCommand;
import org.apache.maven.scm.provider.jazz.command.changelog.JazzChangeLogCommand;
import org.apache.maven.scm.provider.jazz.command.checkin.JazzCheckInCommand;
import org.apache.maven.scm.provider.jazz.command.checkout.JazzCheckOutCommand;
import org.apache.maven.scm.provider.jazz.command.diff.JazzDiffCommand;
import org.apache.maven.scm.provider.jazz.command.edit.JazzEditCommand;
import org.apache.maven.scm.provider.jazz.command.list.JazzListCommand;
import org.apache.maven.scm.provider.jazz.command.status.JazzStatusCommand;
import org.apache.maven.scm.provider.jazz.command.tag.JazzTagCommand;
import org.apache.maven.scm.provider.jazz.command.unedit.JazzUnEditCommand;
import org.apache.maven.scm.provider.jazz.command.update.JazzUpdateCommand;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.net.URI;

/**
 * The maven scm provider for Jazz.
 * <p/>
 * This provider is a wrapper for the command line tool, "scm.sh" or "scm.exe" is that is
 * part of the Jazz SCM Server.
 * <p/>
 * This provider does not use a native API to communicate with the Jazz SCM server.
 * <p/>
 * The scm tool itself is documented at:
 * V2.0.0  - http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_scm.html
 * V3.0    - http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_scm.html
 * V3.0.1  -
 *  http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_scm.html
 *
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="jazz"
 */
public class JazzScmProvider
    extends AbstractScmProvider
{
    // Example: scm:jazz:daviddl;passw0rd123@https://localhost:9443/jazz:Dave's Repository Workspace
    // If the username or password is supplied, then the @ must be used to delimit them.
    public static final String JAZZ_URL_FORMAT =
        "scm:jazz:[username[;password]@]http[s]://server_name[:port]/contextRoot:repositoryWorkspace";

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public String getScmType()
    {
        return JazzConstants.SCM_TYPE;
    }

    /**
     * This method parses the scm URL and returns a SCM provider repository.
     * At this point, the scmUrl is the part after scm:provider_name: in your SCM URL.
     * <p/>
     * The basic url parsing approach is to be as loose as possible.
     * If you specify as per the docs you'll get what you expect.
     * If you do something else the result is undefined.
     * Don't use "/" "\" or "@" as the delimiter.
     * <p/>
     * Parse the scmUrl, which will be of the form:
     * [username[;password]@]http[s]://server_name[:port]/contextRoot:repositoryWorkspace
     * eg:
     * Deb;Deb@https://rtc:9444/jazz:BogusRepositoryWorkspace
     * {@inheritDoc}
     */
    public ScmProviderRepository makeProviderScmRepository( String scmUrl, char delimiter )
        throws ScmRepositoryException
    {
        // Called from:
        // AbstractScmProvider.makeScmRepository()
        // AbstractScmProvider.validateScmUrl()
        getLogger().debug( "JazzScmProvider:makeProviderScmRepository()" );
        getLogger().debug( "Provided scm url   - " + scmUrl );
        getLogger().debug( "Provided delimiter - '" + delimiter + "'" );

        String jazzUrlAndWorkspace = null;
        String usernameAndPassword = null;

        // Look for the Jazz URL after any '@' delimiter used to pass
        // username/password etc (which may not have been specified)
        int lastAtPosition = scmUrl.lastIndexOf( '@' );
        if ( lastAtPosition == -1 )
        {
            // The username;password@ was not supplied.
            jazzUrlAndWorkspace = scmUrl;
        }
        else
        {
            // The username@ or username;password@ was supplied.
            jazzUrlAndWorkspace = ( lastAtPosition < 0 ) ? scmUrl : scmUrl.substring( lastAtPosition + 1 );
            usernameAndPassword = ( lastAtPosition < 0 ) ? null : scmUrl.substring( 0, lastAtPosition );
        }

        // jazzUrlAndWorkspace should be: http[s]://server_name:port/contextRoot:repositoryWorkspace
        // usernameAndPassword should be: username;password or null

        // username and password may not be supplied, and so may remain null.
        String username = null;
        String password = null;

        if ( usernameAndPassword != null )
        {
            // Can be:
            // username
            // username;password
            int delimPosition = usernameAndPassword.indexOf( ';' );
            username = delimPosition >= 0 ? usernameAndPassword.substring( 0, delimPosition ) : usernameAndPassword;
            password = delimPosition >= 0 ? usernameAndPassword.substring( delimPosition + 1 ) : null;
        }

        // We will now validate the jazzUrlAndWorkspace for right number of colons.
        // This has been observed in the wild, where the contextRoot:repositoryWorkspace was not properly formed
        // and this resulted in very strange results in the way in which things were parsed.
        int colonsCounted = 0;
        int colonIndex = 0;
        while ( colonIndex != -1 )
        {
            colonIndex = jazzUrlAndWorkspace.indexOf( ":", colonIndex + 1 );
            if ( colonIndex != -1 )
            {
                colonsCounted++;
            }
        }
        // havePort may also be true when port is supplied, but otherwise have a malformed URL.
        boolean havePort = colonsCounted == 3;

        // Look for workspace after the end of the Jazz URL
        int repositoryWorkspacePosition = jazzUrlAndWorkspace.lastIndexOf( delimiter );
        String repositoryWorkspace = jazzUrlAndWorkspace.substring( repositoryWorkspacePosition + 1 );
        String jazzUrl = jazzUrlAndWorkspace.substring( 0, repositoryWorkspacePosition );

        // Validate the protocols.
        try
        {
            // Determine if it is a valid URI.
            URI jazzUri = URI.create( jazzUrl );
            String scheme = jazzUri.getScheme();
            getLogger().debug( "Scheme - " + scheme );
            if ( scheme == null || !( scheme.equalsIgnoreCase( "http" ) || scheme.equalsIgnoreCase( "https" ) ) )
            {
                throw new ScmRepositoryException(
                    "Jazz Url \"" + jazzUrl + "\" is not a valid URL. The Jazz Url syntax is " + JAZZ_URL_FORMAT );
            }
        }
        catch ( IllegalArgumentException e )
        {
            throw new ScmRepositoryException(
                "Jazz Url \"" + jazzUrl + "\" is not a valid URL. The Jazz Url syntax is " + JAZZ_URL_FORMAT );
        }

        // At this point, jazzUrl is guaranteed to start with either http:// or https://
        // Further process the jazzUrl to extract the server name and port.
        String hostname = null;
        int port = 0;

        if ( havePort )
        {
            // jazzUrlAndWorkspace should be: http[s]://server_name:port/contextRoot:repositoryWorkspace
            // jazzUrl should be            : http[s]://server_name:port/contextRoot
            int protocolIndex = jazzUrl.indexOf( ":" ) + 3;     // The +3 accounts for the "://"
            int portIndex = jazzUrl.indexOf( ":", protocolIndex + 1 );
            hostname = jazzUrl.substring( protocolIndex, portIndex );
            int pathIndex = jazzUrl.indexOf( "/", portIndex + 1 );
            String portNo = jazzUrl.substring( portIndex + 1, pathIndex );
            try
            {
                port = Integer.parseInt( portNo );
            }
            catch ( NumberFormatException nfe )
            {
                throw new ScmRepositoryException(
                    "Jazz Url \"" + jazzUrl + "\" is not a valid URL. The Jazz Url syntax is " + JAZZ_URL_FORMAT );
            }
        }
        else
        {
            // jazzUrlAndWorkspace should be: http[s]://server_name/contextRoot:repositoryWorkspace
            // jazzUrl should be            : http[s]://server_name/contextRoot
            // So we will set port to zero.
            int protocolIndex = jazzUrl.indexOf( ":" ) + 3;     // The +3 accounts for the "://"
            int pathIndex = jazzUrl.indexOf( "/", protocolIndex + 1 );
            if ( pathIndex != -1 )
            {
                hostname = jazzUrl.substring( protocolIndex, pathIndex );
            }
            else
            {
                throw new ScmRepositoryException(
                    "Jazz Url \"" + jazzUrl + "\" is not a valid URL. The Jazz Url syntax is " + JAZZ_URL_FORMAT );
            }
        }

        getLogger().debug( "Creating JazzScmProviderRepository with the following values:" );
        getLogger().debug( "jazzUrl             - " + jazzUrl );
        getLogger().debug( "username            - " + username );
        getLogger().debug( "password            - " + password );
        getLogger().debug( "hostname            - " + hostname );
        getLogger().debug( "port                - " + port );
        getLogger().debug( "repositoryWorkspace - " + repositoryWorkspace );

        return new JazzScmProviderRepository( jazzUrl, username, password, hostname, port, repositoryWorkspace );
    }

    /**
     * {@inheritDoc}
     */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:add()" );
        JazzAddCommand command = new JazzAddCommand();
        command.setLogger( getLogger() );
        return (AddScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected BranchScmResult branch( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:branch()" );
        JazzBranchCommand command = new JazzBranchCommand();
        command.setLogger( getLogger() );
        return (BranchScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected BlameScmResult blame( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:blame()" );
        JazzBlameCommand command = new JazzBlameCommand();
        command.setLogger( getLogger() );
        return (BlameScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                            CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:changelog()" );
        // We need to call the status command first, so that we can get the details of the workspace.
        // This is needed for the list changesets command.
        // We could also 'trust' the value in the pom.
        JazzStatusCommand statusCommand = new JazzStatusCommand();
        statusCommand.setLogger( getLogger() );
        statusCommand.execute( repository, fileSet, parameters );

        JazzChangeLogCommand command = new JazzChangeLogCommand();
        command.setLogger( getLogger() );
        return (ChangeLogScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:checkin()" );
        JazzCheckInCommand command = new JazzCheckInCommand();
        command.setLogger( getLogger() );
        return (CheckInScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                          CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:checkout()" );
        JazzCheckOutCommand command = new JazzCheckOutCommand();
        command.setLogger( getLogger() );
        return (CheckOutScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:diff()" );
        JazzDiffCommand command = new JazzDiffCommand();
        command.setLogger( getLogger() );
        return (DiffScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected EditScmResult edit( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:edit()" );
        JazzEditCommand command = new JazzEditCommand();
        command.setLogger( getLogger() );
        return (EditScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected ExportScmResult export( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:export()" );
        // Use checkout instead
        return super.export( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected ListScmResult list( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:list()" );
        // We need to call the status command first, so that we can get the details of the stream etc.
        // This is needed for workspace and component names.
        JazzStatusCommand statusCommand = new JazzStatusCommand();
        statusCommand.setLogger( getLogger() );
        statusCommand.execute( repository, fileSet, parameters );

        JazzListCommand command = new JazzListCommand();
        command.setLogger( getLogger() );
        return (ListScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:status()" );
        JazzStatusCommand command = new JazzStatusCommand();
        command.setLogger( getLogger() );
        return (StatusScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:tag()" );
        // We need to call the status command first, so that we can get the details of the stream etc.
        // This is needed for workspace deliveries and snapshot promotions.
        JazzStatusCommand statusCommand = new JazzStatusCommand();
        statusCommand.setLogger( getLogger() );
        statusCommand.execute( repository, fileSet, parameters );

        JazzTagCommand command = new JazzTagCommand();
        command.setLogger( getLogger() );
        return (TagScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:update()" );
        JazzUpdateCommand command = new JazzUpdateCommand();
        command.setLogger( getLogger() );
        return (UpdateScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    protected UnEditScmResult unedit( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        getLogger().debug( "JazzScmProvider:unedit()" );
        JazzUnEditCommand command = new JazzUnEditCommand();
        command.setLogger( getLogger() );
        return (UnEditScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * {@inheritDoc}
     */
    public String getScmSpecificFilename()
    {
        return JazzConstants.SCM_META_DATA_FOLDER;
    }

}