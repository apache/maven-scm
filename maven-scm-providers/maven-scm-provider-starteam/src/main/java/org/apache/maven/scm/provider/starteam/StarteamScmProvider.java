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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StarteamScmProvider
    extends AbstractScmProvider
{
    /**
     * @requirement org.apache.maven.scm.CvsCommand
     */
    private Map commands;

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, String delimiter )
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

        if ( rest.indexOf( '/' ) == -1 )
        {
            throw new ScmRepositoryException( "Invalid SCM URL: The url has to be on the form: [username[:password]@]hostname:port/projectName/[viewName/][folderHiearchy/]" );
        }

        String url = rest;

        return new StarteamScmProviderRepository( user, password, url );
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
