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
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnScmProviderRepository
    extends ScmProviderRepositoryWithHost
{
    /** */
    private String url;

    private String protocol;

    /**
     * The base directory for any tags. Can be relative to the repository URL or an absolute URL.
     */
    private String tagBase;

    /**
     * The base directory for any branches. Can be relative to the repository URL or an absolute URL.
     */
    private String branchBase;

    public SvnScmProviderRepository( String url )
    {
        parseUrl( url );

        tagBase = SvnTagBranchUtils.resolveTagBase( url );

        branchBase = SvnTagBranchUtils.resolveBranchBase( url );
    }

    public SvnScmProviderRepository( String url, String user, String password )
    {
        this( url );

        setUser( user );

        setPassword( password );
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

    /**
     * Returns the url/directory to be used when tagging this repository.
     */
    public String getBranchBase()
    {
        return branchBase;
    }

    /**
     * Sets the url/directory to be used when branching this repository.
     * The BranchBase is a way to override the default branch location for the
     * repository.  The default branch location is automatically determined
     * for repositories in the standard subversion layout (with /tags /branches /trunk).
     * Specify this value only if the repository is using a directory other than "/branches" for branching.
     *
     * @param branchBase an absolute or relative url to the base directory to create branch in.
     *                   URL should be in a format that svn client understands, not the scm url format.
     */
    public void setBranchBase( String branchBase )
    {
        this.branchBase = branchBase;
    }

    private void setProtocol( String protocol )
    {
        this.protocol = protocol;
    }

    /**
     * Get the protocol used in this repository (file://, http://, https://,...)
     *
     * @return the protocol
     */
    public String getProtocol()
    {
        return protocol;
    }

    private void parseUrl( String url )
    {
        if ( url.startsWith( "file" ) )
        {
            setProtocol( "file://" );
        }
        else if ( url.startsWith( "https" ) )
        {
            setProtocol( "https://" );
        }
        else if ( url.startsWith( "http" ) )
        {
            setProtocol( "http://" );
        }
        else if ( url.startsWith( "svn+ssh" ) )
        {
            setProtocol( "svn+ssh://" );
        }
        else if ( url.startsWith( "svn" ) )
        {
            setProtocol( "svn://" );
        }

        if ( getProtocol() == null )
        {
            return;
        }

        String urlPath = url.substring( getProtocol().length() );

        int indexAt = urlPath.indexOf( "@" );

        if ( indexAt > 0 && !"svn+ssh://".equals( getProtocol() ) )
        {
            setUser( urlPath.substring( 0, indexAt ) );

            urlPath = urlPath.substring( indexAt + 1 );

            this.url = getProtocol() + urlPath;
        }
        else
        {
            this.url = getProtocol() + urlPath;
        }

        if ( !"file://".equals( getProtocol() ) )
        {
            int indexSlash = urlPath.indexOf( "/" );

            String hostPort = urlPath;

            if ( indexSlash > 0 )
            {
                hostPort = urlPath.substring( 0, indexSlash );
            }

            int indexColon = hostPort.indexOf( ":" );

            if ( indexColon > 0 )
            {
                setHost( hostPort.substring( 0, indexColon ) );
                setPort( Integer.parseInt( hostPort.substring( indexColon + 1 ) ) );
            }
            else
            {
                setHost( hostPort );
            }
        }
    }

    /**
     * A ScmProviderRepository like this but with the parent url (stripping the last directory)
     *
     * @return the parent repository or <code>null</null> if this is the top level repository
     */
    public ScmProviderRepository getParent()
    {
        String newUrl = getUrl().substring( getProtocol().length() );

        while ( newUrl.endsWith( "/." ) )
        {
            newUrl = newUrl.substring( 0, newUrl.length() - 1 );
        }

        while ( newUrl.endsWith( "/" ) )
        {
            newUrl = newUrl.substring( 0, newUrl.length() );
        }

        int i = newUrl.lastIndexOf( "/" );

        if ( i < 0 )
        {
            return null;
        }
        newUrl = newUrl.substring( 0, i );

        return new SvnScmProviderRepository( getProtocol() + newUrl, getUser(), getPassword() );
    }

    /**
     * Get the relative path from the ancestor to this repository
     */
    public String getRelativePath( ScmProviderRepository ancestor )
    {
        if ( ancestor instanceof SvnScmProviderRepository )
        {
            SvnScmProviderRepository svnAncestor = (SvnScmProviderRepository) ancestor;

            String path = getUrl().replaceFirst( svnAncestor.getUrl() + "/", "" );

            if ( !path.equals( getUrl() ) )
            {
                return path;
            }
        }
        return null;
    }

    public String toString()
    {
        return getUrl();
    }

}
