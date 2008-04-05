package org.apache.maven.scm.provider.git.repository;

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

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class GitScmProviderRepository
    extends ScmProviderRepositoryWithHost
{
    /** */
    private String url;

    /** 
     * the protocol to use
     */
    private String protocol;

    /** use local file as transport*/
    public final static String PROTOCOL_FILE = "file";

    /** use gits internal protocol */
    public final static String PROTOCOL_GIT = "git";

    /** use secure shell protocol */
    public final static String PROTOCOL_SSH = "ssh";

    /** use the standard port 80 http protocol */
    public final static String PROTOCOL_HTTP = "http";

    /** use the standard port 443 https protocol */
    public final static String PROTOCOL_HTTPS = "https";

    /** use rsync for retrieving the data
     * TODO implement! */
    public final static String PROTOCOL_RSYNC = "rsync";


    public GitScmProviderRepository( String url )
    {
        parseUrl( url );
    }

    public GitScmProviderRepository( String url, String user, String password )
    {
        this( url );

        setUser( user );

        setPassword( password );
    }

    public String getUrl()
    {
        return url;
    }

    private void setProtocol( String protocol )
    {
        this.protocol = protocol;
    }

    /**
     * @return the protocol used in this repository (file, http, https, git, ...)
     */
    public String getProtocol()
    {
        return protocol;
    }

    private void parseUrl( String url )
    {
        if ( url.startsWith( PROTOCOL_FILE ) )
        {
            setProtocol( PROTOCOL_FILE );
        }
        else if ( url.startsWith( PROTOCOL_HTTPS ) )
        {
            setProtocol( PROTOCOL_HTTPS );
        }
        else if ( url.startsWith( PROTOCOL_HTTP ) )
        {
            setProtocol( PROTOCOL_HTTP );
        }
        else if ( url.startsWith( PROTOCOL_SSH ) )
        {
            setProtocol( PROTOCOL_SSH );
        }
        else if ( url.startsWith( PROTOCOL_GIT ) )
        {
            setProtocol( PROTOCOL_GIT );
        }
        else if ( url.startsWith( PROTOCOL_RSYNC ) )
        {
            setProtocol( PROTOCOL_RSYNC );
        }

        if ( getProtocol() == null )
        {
            return;
        }

        String urlPath = url.substring( getProtocol().length() );

        if ( urlPath.startsWith( "://" ) ) {
            urlPath = urlPath.substring( 3 );
        }
        int indexAt = urlPath.indexOf( "@" );

        if ( indexAt > 0 )
        {
            String userPassword = urlPath.substring( 0, indexAt );
            if ( userPassword.indexOf( ":" ) < 0 )
            {
                setUser( userPassword );
            }
            else
            {
                setUser( userPassword.substring( 0, userPassword.indexOf( ":" ) ) );
                setPassword( userPassword.substring( userPassword.indexOf( ":" ) + 1 ) );
            }

            urlPath = urlPath.substring( indexAt + 1 );

            if ( PROTOCOL_SSH.equals( getProtocol() ) ) 
            {
                StringBuffer urlSb = new StringBuffer( getProtocol() );
                
                urlSb.append( "://" );
                
                if ( getUser() != null ) 
                {
                     urlSb.append( getUser() );
                     
                     if ( getPassword() != null )
                     {
                         urlSb.append( ':' ).append( getPassword() );
                     }
                     
                     urlSb.append( '@' );
                }
                
                urlSb.append( urlPath );
                
                this.url = urlSb.toString();
            }
            else 
            {
                this.url = getProtocol() + "://" + urlPath;
            }
        }
        else
        {
            this.url = getProtocol() + "://"  + urlPath;
        }

        if ( !PROTOCOL_FILE.equals( getProtocol() ) )
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

        return new GitScmProviderRepository( getProtocol() + newUrl, getUser(), getPassword() );
    }

    /**
     * Get the relative path from the ancestor to this repository
     */
    public String getRelativePath( ScmProviderRepository ancestor )
    {
        if ( ancestor instanceof GitScmProviderRepository )
        {
            GitScmProviderRepository gitAncestor = (GitScmProviderRepository) ancestor;

            String path = getUrl().replaceFirst( gitAncestor.getUrl() + "/", "" );

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
