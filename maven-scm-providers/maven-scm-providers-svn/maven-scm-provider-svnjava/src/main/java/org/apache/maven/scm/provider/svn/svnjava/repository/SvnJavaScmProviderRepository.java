package org.apache.maven.scm.provider.svn.svnjava.repository;

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

import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnJavaScmProviderRepository
    extends SvnScmProviderRepository
{
    private SVNURL svnUrl;

    private SVNClientManager clientManager;

    public SvnJavaScmProviderRepository( SVNURL svnUrl, String strUrl )
    {
        super( strUrl, null, null );

        setUser( svnUrl.getUserInfo() );

        setPassword( null );

        this.svnUrl = svnUrl;

        if ( getUser() != null )
        {
            System.getProperties().setProperty( "javasvn.ssh2.username", getUser() );
        }
        if ( getPassword() != null )
        {
            System.getProperties().setProperty( "javasvn.ssh2.password", getPassword() );
        }

        initializeClientManager();
    }

    public SVNURL getSvnUrl()
    {
        return svnUrl;
    }

    public SVNClientManager getClientManager()
    {
        return clientManager;
    }

    public void setPrivateKey( String privateKey )
    {
        super.setPrivateKey( privateKey );

        if ( getPrivateKey() != null )
        {
            System.getProperties().setProperty( "javasvn.ssh2.key", getPrivateKey() );
        }

        initializeClientManager();
    }

    public void setPassphrase( String passphrase )
    {
        super.setPassphrase( passphrase );

        if ( getPassphrase() != null )
        {
            System.getProperties().setProperty( "javasvn.ssh2.passphrase", getPassphrase() );
        }

        initializeClientManager();
    }

    public void setUser( String user )
    {
        super.setUser( user );

        if ( getUser() != null )
        {
            System.getProperties().setProperty( "javasvn.ssh2.username", getUser() );
        }

        initializeClientManager();
    }

    public void setPassword( String password )
    {
        super.setPassword( password );

        if ( getPassword() != null )
        {
            System.getProperties().setProperty( "javasvn.ssh2.password", getPassword() );
        }

        initializeClientManager();
    }

    private void initializeClientManager()
    {
        /*
        * Creates a default run-time configuration options driver. Default options
        * created in this way use the Subversion run-time configuration area (for
        * instance, on a Windows platform it can be found in the '%APPDATA%\Subversion'
        * directory).
        *
        * readonly = true - not to save  any configuration changes that can be done
        * during the program run to a config file (config settings will only
        * be read to initialize; to enable changes the readonly flag should be set
        * to false).
        *
        */
        ISVNOptions options = SVNWCUtil.createDefaultOptions( true );

        clientManager = SVNClientManager.newInstance( options, SVNWCUtil.createDefaultAuthenticationManager() );
    }
}
