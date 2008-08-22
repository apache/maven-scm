package org.apache.maven.scm.provider.cvslib.repository;

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

import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmProvider;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsScmProviderRepository
    extends ScmProviderRepositoryWithHost
{
    /** */
    private String cvsroot;

    /** */
    private String transport;

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

        if ( user == null && AbstractCvsScmProvider.TRANSPORT_EXT.equals( transport ) )
        {
            user = System.getProperty( "user.name" );
        }

        setUser( user );

        setPassword( password );

        setHost( host );

        setPort( port );

        this.path = path;

        this.module = module;
    }

    /**
     * @return The cvs root
     */
    public String getCvsRoot()
    {
        String root = getCvsRootForCvsPass();

        return removeDefaultPortFromCvsRoot( root );
    }

    private String removeDefaultPortFromCvsRoot( String root )
    {
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
        String result;
        String transport = getTransport();
        if ( AbstractCvsScmProvider.TRANSPORT_LOCAL.equals( transport ) )
        {
            result = ":" + transport + ":" + cvsroot;
        }
        else if ( getUser() != null )
        {
            result = getCvsRootWithCorrectUser( getUser() );
        }
        else
        {
            throw new IllegalArgumentException( "Username isn't defined." );
        }
        return result;
    }

    /**
     * @return The subtype (like pserver).
     */
    public String getTransport()
    {
        return transport;
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
        return getCvsRootWithCorrectUser( null );
    }

    /**
     * @param user user name
     * @return
     */
    private String getCvsRootWithCorrectUser( String user )
    {
        //:transport:rest_of_cvsroot
        int indexOfUsername = getTransport().length() + 2;

        int indexOfAt = cvsroot.indexOf( "@" );

        String userString = user == null ? "" : ":" + user;

        if ( indexOfAt > 0 )
        {
            cvsroot = ":" + getTransport() + userString + cvsroot.substring( indexOfAt );
        }
        else
        {
            cvsroot = ":" + getTransport() + userString + "@" + cvsroot.substring( indexOfUsername );
        }

        return cvsroot;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        StringBuffer sb = new StringBuffer();

        if ( getUser() == null )
        {
            if ( AbstractCvsScmProvider.TRANSPORT_LOCAL.equals( getTransport() ) )
            {
                sb.append( getCvsRoot() );
            }
            else
            {
                sb.append( removeDefaultPortFromCvsRoot( getCvsRootWithCorrectUser() ) );
            }
        }
        else
        {
            sb.append( getCvsRoot() );
        }
        sb.append( ":" );
        sb.append( getModule() );

        /* remove first colon */
        if ( sb.charAt( 0 ) == ':' )
        {
            sb.deleteCharAt( 0 );
        }
        return sb.toString();
    }

}
