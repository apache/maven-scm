package org.apache.maven.scm.provider.svn.repository;

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
     * The base directory for any tags. Can be relative to the repository URL or an absolute URL.
     */
    private String tagBase;

    public SvnScmProviderRepository( String url, String user, String password )
    {
        setUser( user );

        setPassword( password );

        parseUrl( url );
    }

    public String getUrl()
    {
        return url;
    }

    /**
     * Returns the url/directory to be used when tagging this repository.
     */
    public String getTagBase()
    {
        return tagBase;
    }

    /**
     * Sets the url/directory to be used when tagging this repository.
     * The TagBase is a way to override the default tag location for the
     * repository.  The default tag location is automatically determined
     * for repositories in the standard subversion layout (with /tags /branches /trunk).
     * Specify this value only if the repository is using a directory other than "/tags" for tagging.
     *
     * @param tagBase an absolute or relative url to the base directory to create tags in.
     *                URL should be in a format that svn client understands, not the scm url format.
     */
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
