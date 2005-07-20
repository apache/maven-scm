package org.apache.maven.scm.provider.svn.repository;

/*
 * Copyright 2003-2004 The Apache Software Foundation.
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

import org.apache.maven.scm.provider.ScmProviderRepository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnScmProviderRepository
    extends ScmProviderRepository
{
    /** */
    private String url;

    /**
     * The base directory for any tags, relative to the URL given. Default is <code>../tags</code>.
     */
    private String tagBase;

    public SvnScmProviderRepository( String url, String user, String password )
    {
        this.tagBase = url.substring( 0, url.lastIndexOf( '/' ) ) + "/tags";

        setUser( user );

        setPassword( password );

        parseUrl( url );
    }

    public String getUrl()
    {
        return url;
    }

    public String getTagBase()
    {
        return tagBase;
    }

    public void setTagBase( String tagBase )
    {
        this.tagBase = tagBase;
    }

    private void parseUrl( String url )
    {
        String protocol = null;

        if ( url.startsWith( "file" ) )
        {
            protocol = "file://";
        }
        else if ( url.startsWith( "https" ) )
        {
            protocol = "https://";
        }
        else if ( url.startsWith( "http" ) )
        {
            protocol = "http://";
        }
        else if ( url.startsWith( "svn+ssh" ) )
        {
            protocol = "svn+ssh://";
        }
        else if ( url.startsWith( "svn" ) )
        {
            protocol = "svn://";
        }

        String urlPath = url.substring( protocol.length() );

        int indexAt = urlPath.indexOf( "@" );

        if ( indexAt > 0 && !"svn+ssh://".equals( protocol ) )
        {
            setUser( urlPath.substring( 0, indexAt ) );

            this.url = protocol + urlPath.substring( indexAt + 1 );
        }
        else
        {
            this.url = protocol + urlPath;
        }
    }
}
