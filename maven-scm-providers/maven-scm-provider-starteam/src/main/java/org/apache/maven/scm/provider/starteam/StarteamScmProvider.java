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

import java.util.Map;

import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
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

    /**
     * @requirement org.apache.maven.scm.StarteamCommand
     */
    private Map commands;

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

    // ----------------------------------------------------------------------
    // AbstractScmProvider Implementation
    // ----------------------------------------------------------------------

    protected Map getCommands()
    {
        return commands;
    }

    public String getScmType()
    {
        return "starteam";
    }
}
