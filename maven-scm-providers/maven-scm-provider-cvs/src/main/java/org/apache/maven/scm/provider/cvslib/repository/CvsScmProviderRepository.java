package org.apache.maven.scm.provider.cvslib.repository;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
import org.apache.maven.scm.provider.cvslib.CvsScmProvider;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsScmProviderRepository
    extends ScmProviderRepository
{
    /** */
    private String cvsroot;

    /** */
    private String transport;

    /** */
    private String host;

    /** */
    private int port;

    /** */
    private String path;

    /** */
    private String module;

    public CvsScmProviderRepository( String cvsroot, String transport, String user, String password, String host,
                                     String path, String module )
    {
        this( cvsroot, transport, user, password, host, -1, path, module );
    }

    public CvsScmProviderRepository( String cvsroot, String transport, String user, String password, String host,
                                     int port, String path, String module )
    {
        this.cvsroot = cvsroot;

        this.transport = transport;

        setUser( user );

        setPassword( password );

        this.host = host;

        this.port = port;

        this.path = path;

        this.module = module;
    }

    /**
     * @return The cvs root
     */
    public String getCvsRoot()
    {
        String root = getCvsRootForCvsPass();

        if ( root != null && root.indexOf( ":2401" ) > 0 )
        {
            root = root.substring( 0, root.indexOf( ":2401" ) ) + ":" + root.substring( root.indexOf( ":2401" ) + 5 );
        }

        return root;
    }

    /**
     * @return The cvs root stored in .cvspass
     */
    public String getCvsRootForCvsPass()
    {
        if ( getUser() != null )
        {
            return getCvsRootWithCorrectUser();
        }
        else
        {
            if ( CvsScmProvider.TRANSPORT_LOCAL.equals( getTransport() ) )
            {
                return cvsroot;
            }
            else
            {
                throw new IllegalArgumentException( "Username isn't defined." );
            }
        }
    }

    /**
     * @return The subtype (like pserver).
     */
    public String getTransport()
    {
        return transport;
    }

    /**
     * @return The host.
     */
    public String getHost()
    {
        return host;
    }

    /**
     * Returns the port or -1 if it isn't set.
     *
     * @return The port.
     */
    public int getPort()
    {
        return port;
    }

    /**
     * @return The path.
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @return The module name.
     */
    public String getModule()
    {
        return module;
    }

    private String getCvsRootWithCorrectUser()
    {
        //:transport:rest_of_cvsroot
        int indexOfUsername = getTransport().length() + 2;

        int indexOfAt = cvsroot.indexOf( "@" );

        if ( indexOfAt > 0 )
        {
            cvsroot = ":" + getTransport() + ":" + getUser() + cvsroot.substring( indexOfAt );
        }
        else
        {
            cvsroot = ":" + getTransport() + ":" + getUser() + "@" + cvsroot.substring( indexOfUsername );
        }

        return cvsroot;
    }
}
