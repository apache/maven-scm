package org.apache.maven.scm.provider.perforce;

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

import java.io.File;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.login.LoginScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.command.add.PerforceAddCommand;
import org.apache.maven.scm.provider.perforce.command.changelog.PerforceChangeLogCommand;
import org.apache.maven.scm.provider.perforce.command.checkin.PerforceCheckInCommand;
import org.apache.maven.scm.provider.perforce.command.checkout.PerforceCheckOutCommand;
import org.apache.maven.scm.provider.perforce.command.diff.PerforceDiffCommand;
import org.apache.maven.scm.provider.perforce.command.edit.PerforceEditCommand;
import org.apache.maven.scm.provider.perforce.command.login.PerforceLoginCommand;
import org.apache.maven.scm.provider.perforce.command.remove.PerforceRemoveCommand;
import org.apache.maven.scm.provider.perforce.command.status.PerforceStatusCommand;
import org.apache.maven.scm.provider.perforce.command.tag.PerforceTagCommand;
import org.apache.maven.scm.provider.perforce.command.unedit.PerforceUnEditCommand;
import org.apache.maven.scm.provider.perforce.command.update.PerforceUpdateCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public class PerforceScmProvider
    extends AbstractScmProvider
{
    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public boolean requiresEditMode()
    {
        return true;
    }

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        String path;
        int port = 0;
        String host = null;

        int i1 = scmSpecificUrl.indexOf( delimiter );
        int i2 = scmSpecificUrl.indexOf( delimiter, i1 + 1 );

        if ( i1 > 0 )
        {
            int lastDelimiter = scmSpecificUrl.lastIndexOf( delimiter );
            path = scmSpecificUrl.substring( lastDelimiter + 1 );
            host = scmSpecificUrl.substring( 0, i1 );

            // If there is tree parts in the scm url, the second is the port
            if ( i2 >= 0 )
            {
                try
                {
                    String tmp = scmSpecificUrl.substring( i1 + 1, lastDelimiter );
                    port = Integer.parseInt( tmp );
                }
                catch ( NumberFormatException ex )
                {
                    throw new ScmRepositoryException( "The port has to be a number." );
                }
            }
        }
        else
        {
            path = scmSpecificUrl;
        }

        String user = null;
        String password = null;
        if ( host != null && host.indexOf( "@" ) > 1 )
        {
            user = host.substring( 0, host.indexOf( "@" ) );
            host = host.substring( host.indexOf( "@" ) + 1 );
        }

        if ( path.indexOf( "@" ) > 1 )
        {
            if ( host != null )
            {
                getLogger().warn(
                                  "Username as part of path is deprecated, the new format is "
                                      + "scm:perforce:[username@]host:port:path_to_repository" );
            }

            user = path.substring( 0, path.indexOf( "@" ) );
            path = path.substring( path.indexOf( "@" ) + 1 );
        }

        return new PerforceScmProviderRepository( host, port, path, user, password );
    }

    public String getScmType()
    {
        return "perforce";
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#changelog(org.apache.maven.scm.repository.ScmRepository,
     *      org.apache.maven.scm.ScmFileSet,
     *      org.apache.maven.scm.CommandParameters)
     */
    protected ChangeLogScmResult changelog( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        PerforceChangeLogCommand command = new PerforceChangeLogCommand();
        command.setLogger( getLogger() );
        return (ChangeLogScmResult) command.execute( repository.getProviderRepository(), fileSet, parameters );
    }

    protected AddScmResult add( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceAddCommand command = new PerforceAddCommand();
        command.setLogger( getLogger() );
        return (AddScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    protected RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceRemoveCommand command = new PerforceRemoveCommand();
        command.setLogger( getLogger() );
        return (RemoveScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    protected CheckInScmResult checkin( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceCheckInCommand command = new PerforceCheckInCommand();
        command.setLogger( getLogger() );
        return (CheckInScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    protected CheckOutScmResult checkout( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceCheckOutCommand command = new PerforceCheckOutCommand();
        command.setLogger( getLogger() );
        return (CheckOutScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    protected DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceDiffCommand command = new PerforceDiffCommand();
        command.setLogger( getLogger() );
        return (DiffScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    protected EditScmResult edit( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceEditCommand command = new PerforceEditCommand();
        command.setLogger( getLogger() );
        return (EditScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    protected LoginScmResult login( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceLoginCommand command = new PerforceLoginCommand();
        command.setLogger( getLogger() );
        return (LoginScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    protected StatusScmResult status( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceStatusCommand command = new PerforceStatusCommand();
        command.setLogger( getLogger() );
        return (StatusScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    protected TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceTagCommand command = new PerforceTagCommand();
        command.setLogger( getLogger() );
        return (TagScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    protected UnEditScmResult unedit( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceUnEditCommand command = new PerforceUnEditCommand();
        command.setLogger( getLogger() );
        return (UnEditScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    protected UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, CommandParameters params )
        throws ScmException
    {
        PerforceUpdateCommand command = new PerforceUpdateCommand();
        command.setLogger( getLogger() );
        return (UpdateScmResult) command.execute( repository.getProviderRepository(), fileSet, params );
    }

    public static Commandline createP4Command( PerforceScmProviderRepository repo, File workingDir )
    {
        Commandline command = new Commandline();
        command.setExecutable( "p4" );
        command.setWorkingDirectory( workingDir.getAbsolutePath() );

        if ( repo.getHost() != null )
        {
            command.createArgument().setValue( "-H" );
            String value = repo.getHost();
            if ( repo.getPort() != 0 )
            {
                value += ":" + Integer.toString( repo.getPort() );
            }
            command.createArgument().setValue( value );
        }

        if ( StringUtils.isNotEmpty( repo.getUser() ) )
        {
            command.createArgument().setValue( "-u" );
            command.createArgument().setValue( repo.getUser() );
        }

        if ( StringUtils.isNotEmpty( repo.getPassword() ) )
        {
            command.createArgument().setValue( "-P" );
            command.createArgument().setValue( repo.getPassword() );
        }
        return command;
    }

    public static String clean( String string )
    {
        if ( string.indexOf( " -P " ) == -1 ) 
        {
            return string;
        }
        int idx = string.indexOf( " -P " ) + 4;
        int end = string.indexOf(' ', idx);
        return string.substring( 0, idx ) + StringUtils.repeat( "*", end - idx ) + string.substring( end );
    }
    
    /**
     * Given a path like "//depot/foo/bar", returns the
     * proper path to include everything beneath it.
     * 
     * //depot/foo/bar -> //depot/foo/bar/...
     * //depot/foo/bar/ -> //depot/foo/bar/...
     * //depot/foo/bar/... -> //depot/foo/bar/...
     */
    public static String getCanonicalRepoPath( String repoPath ) 
    {
        if ( repoPath.endsWith( "/..." ) )
        {
            return repoPath;
        }
        else if ( repoPath.endsWith( "/" ) ) 
        {
            return repoPath + "...";
        }
        else 
        {
            return repoPath + "/...";
        }
    }
}
