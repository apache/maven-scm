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

import java.util.Map;

import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PerforceScmProvider
    extends AbstractScmProvider
{
    /**
     * @requirement org.apache.maven.scm.CvsCommand
     */
    private Map commands;

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

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
                catch( NumberFormatException ex )
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
                getLogger().warn( "Username as part of path is deprecated, the new format is " +
                                  "scm:perforce:[username@]host:port:path_to_repository" );
            }

            user = path.substring( 0, path.indexOf( "@" ) );

            path = path.substring( path.indexOf( "@" ) + 1 );
        }

        return new PerforceScmProviderRepository( host, port, path, user, password );
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
        return "perforce";
    }
}
