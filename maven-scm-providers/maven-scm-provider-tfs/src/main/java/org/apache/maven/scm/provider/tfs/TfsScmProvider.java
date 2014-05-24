package org.apache.maven.scm.provider.tfs;

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

import java.net.URI;

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
import org.apache.maven.scm.provider.tfs.command.TfsAddCommand;
import org.apache.maven.scm.provider.tfs.command.TfsBranchCommand;
import org.apache.maven.scm.provider.tfs.command.TfsChangeLogCommand;
import org.apache.maven.scm.provider.tfs.command.TfsCheckInCommand;
import org.apache.maven.scm.provider.tfs.command.TfsCheckOutCommand;
import org.apache.maven.scm.provider.tfs.command.TfsEditCommand;
import org.apache.maven.scm.provider.tfs.command.TfsListCommand;
import org.apache.maven.scm.provider.tfs.command.TfsStatusCommand;
import org.apache.maven.scm.provider.tfs.command.TfsTagCommand;
import org.apache.maven.scm.provider.tfs.command.TfsUnEditCommand;
import org.apache.maven.scm.provider.tfs.command.TfsUpdateCommand;
import org.apache.maven.scm.provider.tfs.command.blame.TfsBlameCommand;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="tfs"
 */
public class TfsScmProvider
    extends AbstractScmProvider
{

    public static final String TFS_URL_FORMAT =
        "[[domain\\]username[;password]@]http[s]://server_name[:port]:workspace:$/TeamProject/Path/To/Project";

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public String getScmType()
    {
        return "tfs";
    }

    public ScmProviderRepository makeProviderScmRepository( String scmUrl, char delimiter )
        throws ScmRepositoryException
    {
        // Look for the TFS URL after any '@' delmiter used to pass
        // usernames/password etc
        // We deliberately look for the last '@' character as username could
        // contain an '@' also.
        int lastAtPos = scmUrl.lastIndexOf( '@' );
        getLogger().info( "scmUrl - " + scmUrl );

        String tfsUrl = ( lastAtPos < 0 ) ? scmUrl : scmUrl.substring( lastAtPos + 1 );
        String usernamePassword = ( lastAtPos < 0 ) ? null : scmUrl.substring( 0, lastAtPos );

        // Look for TFS path after the end of the TFS URL
        int tfsPathPos = tfsUrl.lastIndexOf( delimiter + "$/" );
        String serverPath = "$/";
        if ( tfsPathPos > 0 )
        {
            serverPath = tfsUrl.substring( tfsPathPos + 1 );
            tfsUrl = tfsUrl.substring( 0, tfsPathPos );
        }

        // Look for workspace ater the end of the TFS URL
        int workspacePos = tfsUrl.lastIndexOf( delimiter );
        String workspace = tfsUrl.substring( workspacePos + 1 );
        tfsUrl = tfsUrl.substring( 0, workspacePos );
        getLogger().info( "workspace: " + workspace );

        // Look for workspace ater the end of the TFS URL
        int checkinPoliciesPos = tfsUrl.lastIndexOf( delimiter );
        String checkinPolicies = tfsUrl.substring( checkinPoliciesPos + 1 );
        tfsUrl = tfsUrl.substring( 0, checkinPoliciesPos );
        getLogger().info( "checkinPolicies: " + checkinPolicies );

        try
        {
            // Use URI's validation to determine if valid URI.
            URI tfsUri = URI.create( tfsUrl );
            String scheme = tfsUri.getScheme();
            getLogger().info( "Scheme - " + scheme );
            if ( scheme == null || !( scheme.equalsIgnoreCase( "http" ) || scheme.equalsIgnoreCase( "https" ) ) )
            {
                throw new ScmRepositoryException( "TFS Url \"" + tfsUrl + "\" is not a valid URL. "
                    + "The TFS Url syntax is " + TFS_URL_FORMAT );
            }
        }
        catch ( IllegalArgumentException e )
        {
            throw new ScmRepositoryException( "TFS Url \"" + tfsUrl + "\" is not a valid URL. The TFS Url syntax is "
                + TFS_URL_FORMAT );
        }

        String username = null;
        String password = null;

        if ( usernamePassword != null )
        {
            // Deliberately not using .split here in case password contains a
            // ';'
            int delimPos = usernamePassword.indexOf( ';' );
            username = ( delimPos < 0 ) ? usernamePassword : usernamePassword.substring( 0, delimPos );
            password = ( delimPos < 0 ) ? null : usernamePassword.substring( delimPos + 1 );
        }

        boolean useCheckinPolicies = Boolean.parseBoolean( checkinPolicies );

        return new TfsScmProviderRepository( tfsUrl, username, password, serverPath, workspace,
                useCheckinPolicies  );
    }

    protected ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                            CommandParameters parameters )
        throws ScmException
    {
        TfsChangeLogCommand command = new TfsChangeLogCommand();
        command.setLogger( getLogger() );
        return ( ChangeLogScmResult ) command.execute( repository, fileSet, parameters );
    }

    protected CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                          CommandParameters parameters )
        throws ScmException
    {
        TfsCheckOutCommand command = new TfsCheckOutCommand();
        command.setLogger( getLogger() );
        return ( CheckOutScmResult ) command.execute( repository, fileSet, parameters );
    }

    protected EditScmResult edit( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        TfsEditCommand command = new TfsEditCommand();
        command.setLogger( getLogger() );
        return (EditScmResult) command.execute( repository, fileSet, parameters );
    }

    protected UnEditScmResult unedit( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        TfsUnEditCommand command = new TfsUnEditCommand();
        command.setLogger( getLogger() );
        return (UnEditScmResult) command.execute( repository, fileSet, parameters );
    }

    protected StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        TfsStatusCommand command = new TfsStatusCommand();
        command.setLogger( getLogger() );
        return (StatusScmResult) command.execute( repository, fileSet, parameters );
    }

    protected UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        TfsUpdateCommand command = new TfsUpdateCommand();
        command.setLogger( getLogger() );
        return (UpdateScmResult) command.execute( repository, fileSet, parameters );
    }

    protected CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        TfsCheckInCommand command = new TfsCheckInCommand();
        command.setLogger( getLogger() );
        return (CheckInScmResult) command.execute( repository, fileSet, parameters );
    }

    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        TfsAddCommand command = new TfsAddCommand();
        command.setLogger( getLogger() );
        return (AddScmResult) command.execute( repository, fileSet, parameters );
    }

    protected TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        TfsTagCommand command = new TfsTagCommand();
        command.setLogger( getLogger() );
        return (TagScmResult) command.execute( repository, fileSet, parameters );
    }

    protected BranchScmResult branch( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        TfsBranchCommand command = new TfsBranchCommand();
        command.setLogger( getLogger() );
        return (BranchScmResult) command.execute( repository, fileSet, parameters );
    }

    protected ListScmResult list( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        TfsListCommand command = new TfsListCommand();
        command.setLogger( getLogger() );
        return (ListScmResult) command.execute( repository, fileSet, parameters );
    }

    protected BlameScmResult blame( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        TfsBlameCommand command = new TfsBlameCommand();
        command.setLogger( getLogger() );
        return (BlameScmResult) command.execute( repository, fileSet, parameters );
    }

    protected DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        // Because tf launches only external diffs
        return super.diff( repository, fileSet, parameters );
    }

    protected ExportScmResult export( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        // Use checkout instead
        return super.export( repository, fileSet, parameters );
    }

}