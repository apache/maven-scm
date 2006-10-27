package org.apache.maven.scm.provider.hg.repository;

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
import org.codehaus.plexus.util.StringUtils;

import java.io.File;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgScmProviderRepository
    extends ScmProviderRepositoryWithHost
{
    //Known and tested protocols
    private static final String FILE = "";

    private static final String SFTP = "sftp://";

    private static final String FTP = "ftp://";

    private static final String AFTP = "aftp://";

    private static final String HTTP = "http://";

    private static final String HTTPS = "https://";

    private final String path;

    private final String protocol;

    private final String orgUrl;

    public HgScmProviderRepository( String url )
    {
        orgUrl = url;
        protocol = getProtocol( url );
        path = parseUrl( url );
    }

    public String getURI()
    {
        return protocol + (needsAuthentication() ? addUser() + addPassword() + addAt() : "") +  addHost() + addPort() + addPath();
    }

    /**
     * @return A message if the repository as an invalid URI, null if the URI seems fine.
     */
    public String validateURI()
    {

        String msg = null;

        if ( needsAuthentication() )
        {
            if ( getUser() == null )
            {
                msg = "Username is missing for protocol " + protocol;
            }
            else if ( getPassword() == null )
            {
                msg = "Password is missing for protocol " + protocol;
            }
            else if ( getHost() == null )
            {
                msg = "Host (eg. www.myhost.com) is missing for protocol " + protocol;
            }
        }

        else if ( getPort() != 0 && getHost() == null )
        {
            msg = "Got port information without any host for protocol " + protocol;
        }

        if ( msg != null )
        {
            msg = "Something could be wrong about the repository URL: " + orgUrl + "\nReason: " + msg
                + "\nCheck http://maven.apache.org/scm for usage and hints.";
        }
        return msg;
    }

    private String getProtocol( String url )
    {
    	// Assume we have a file unless we find a URL based syntax
        String prot = FILE;
        if ( url.startsWith( SFTP ) )
        {
            prot = SFTP;
        }
        else if ( url.startsWith( HTTP ) )
        {
            prot = HTTP;
        }
        else if ( url.startsWith( HTTPS ) )
        {
            prot = HTTPS;
        }

        return prot;
    }

    private String parseUrl( String url )
    {
        if ( protocol == FILE )
        {
            return url;
        }

        //Strip protocol
        url = url.substring( protocol.length() );

        url = parseUsernameAndPassword( url );

        url = parseHostAndPort( url );

        url = parsePath( url );

        return url; //is now only the path
    }

    private String parseHostAndPort( String url )
    {
        if ( protocol != FILE )
        {
            String[] split = url.split( ":" );
            if ( split.length == 2 )
            {
                setHost( split[0] );
                url = url.substring( split[0].length() + 1 );
                split = split[1].split( "/" );
                if ( split.length == 2 )
                {
                    url = url.substring( split[0].length() );
                    try {
                        setPort( Integer.valueOf( split[0] ).intValue() );
                    } catch (NumberFormatException e) {
                        //Ignore - error will manifest itself later.
                    }
                }
            }
            else
            {
                split = url.split( "/" );
                if ( split.length > 1 )
                {
                    url = url.substring( split[0].length() );
                    setHost( split[0] );
                }
            }
        }
        return url;
    }

    private String parseUsernameAndPassword( String url )
    {
        if ( needsAuthentication() )
        {
            String[] split = url.split( "@" );
            if ( split.length == 2 )
            {
                url = split[1]; //Strip away 'username:password@' from url
                split = split[0].split( ":" );
                if ( split.length == 2 )
                { //both username and password
                    setUser( split[0] );
                    setPassword( split[1] );
                }
                else
                { //only username
                    setUser( split[0] );
                }
            }
        }
        return url;
    }

    private String parsePath( String url )
    {
        if ( protocol == FILE )
        {
            //Use OS dependent path separator
            url = StringUtils.replace( url, "/", File.separator );

            //Test first path separator (*nix systems use them to denote root)
            File tmpFile = new File( url ); //most likly a *nix system
            String url2 = url.substring( File.pathSeparator.length() );
            File tmpFile2 = new File( url2 ); //most likly a windows system
            if ( !tmpFile.exists() && !tmpFile2.exists() )
            {
                // This is trouble - Trouble is reported in validateURI()
            }

            url = tmpFile2.exists() ? url2 : url;
        }

        return url;
    }

    private String addUser()
    {
        return ( getUser() == null ) ? "" : getUser();
    }

    private String addPassword()
    {
        return ( getPassword() == null ) ? "" : ":" + getPassword();
    }

    private String addAt()
    {
        return needsAuthentication() ? "@" : "";
    }

    private String addHost()
    {
        return ( getHost() == null ) ? "" : getHost();
    }

    private String addPort()
    {
        return ( getPort() == 0 ) ? "" : ":" + getPort();
    }

    private String addPath()
    {
        return path;
    }

    private boolean needsAuthentication()
    {
        return protocol == SFTP || protocol == FTP || protocol == HTTPS || protocol == AFTP;
    }

    public String toString()
    {
        return "Hg Repository Interpreted from: " + orgUrl + ":\nProtocol: " + protocol + "\nHost: " + getHost()
            + "\nPort: " + getPort() + "\nUsername: " + getUser() + "\nPassword: " + getPassword() + "\nPath: " + path;
    }
}
