package org.apache.maven.scm.provider.starteam.repository;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamScmProviderRepository
    extends ScmProviderRepositoryWithHost
{
    private String path;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public StarteamScmProviderRepository( String user, String password, String host, int port, String path )
    {
        setUser( user );

        setPassword( password );

        setHost( host );

        setPort( port );

        if ( !path.startsWith( "/" ) )
        {
            throw new IllegalArgumentException( "The path must be start with a slash?" );
        }

        this.path = path;
    }

    public String getUrl()
    {
        return getHost() + ":" + getPort() + path;
    }

    public String getFullUrl()
    {
        String fullUrl = getUser() + ":";

        if ( getPassword() != null )
        {
            fullUrl += getPassword();
        }

        fullUrl += "@" + getUrl();

        return fullUrl;
    }

    /**
     * @return Returns the path.
     */
    public String getPath()
    {
        return path;
    }
}
