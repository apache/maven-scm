package org.apache.maven.scm.provider.starteam;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.add.StarteamAddCommand;
import org.apache.maven.scm.provider.starteam.command.changelog.StarteamChangeLogCommand;
import org.apache.maven.scm.provider.starteam.command.checkin.StarteamCheckInCommand;
import org.apache.maven.scm.provider.starteam.command.checkout.StarteamCheckOutCommand;
import org.apache.maven.scm.provider.starteam.command.diff.StarteamDiffCommand;
import org.apache.maven.scm.provider.starteam.command.status.StarteamStatusCommand;
import org.apache.maven.scm.provider.starteam.command.tag.StarteamTagCommand;
import org.apache.maven.scm.provider.starteam.command.update.StarteamUpdateCommand;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StarteamScmProvider
    extends AbstractScmProvider
{
    public static final String STARTEAM_URL_FORMAT = "[username[:password]@]hostname:port:/projectName/[viewName/][folderHiearchy/]";

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        String user = null;

        String password = null;

        int index = scmSpecificUrl.indexOf( '@' );

        String rest = scmSpecificUrl;

        if ( index != -1 )
        {
            String userAndPassword = scmSpecificUrl.substring( 0, index );

            rest = scmSpecificUrl.substring( index + 1 );

            index = userAndPassword.indexOf( ":" );

            if ( index != -1 )
            {
                user = userAndPassword.substring( 0, index );

                password = userAndPassword.substring( index + 1 );
            }
            else
            {
                user = userAndPassword;
            }
        }

        String[] tokens = StringUtils.split( rest, Character.toString( delimiter ) );

        String host;

        int port;

        String path;

        if ( tokens.length == 3 )
        {
            host = tokens[0];

            port = new Integer( tokens[1] ).intValue();

            path = tokens[2];
        }
        else if ( tokens.length == 2 )
        {
            getLogger().warn( "Your scm URL use a deprecated format. The new format is :" + STARTEAM_URL_FORMAT );

            host = tokens[0];

            if ( tokens[1].indexOf( '/' ) == -1 )
            {
                throw new ScmRepositoryException( "Invalid SCM URL: The url has to be on the form: "
                                                  + STARTEAM_URL_FORMAT );
            }

            int at = tokens[1].indexOf( '/' );

            port = new Integer( tokens[1].substring( 0, at ) ).intValue();

            path = tokens[1].substring( at );
        }
        else
        {
            throw new ScmRepositoryException( "Invalid SCM URL: The url has to be on the form: " + STARTEAM_URL_FORMAT );
        }

        try
        {
            return new StarteamScmProviderRepository( user, password, host, port, path );
        }
        catch ( Exception e )
        {
            throw new ScmRepositoryException( "Invalid SCM URL: The url has to be on the form: " + STARTEAM_URL_FORMAT );
        }
    }

    public String getScmType()
    {
        return "starteam";
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#add(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (AddScmResult) new StarteamAddCommand().execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#changelog(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (ChangeLogScmResult) new StarteamChangeLogCommand().execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkin(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (CheckInScmResult) new StarteamCheckInCommand().execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkout(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (CheckOutScmResult) new StarteamCheckOutCommand().execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#diff(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (DiffScmResult) new StarteamDiffCommand().execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#status(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (StatusScmResult) new StarteamStatusCommand().execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#tag(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (TagScmResult) new StarteamTagCommand().execute( repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#update(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (UpdateScmResult) new StarteamUpdateCommand().execute( repository, fileSet, parameters );
    }

}
