package org.apache.maven.scm.provider.cvslib;

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

import java.util.Map;

import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsScmProvider
    extends AbstractScmProvider
{
    /** */
    private final static String TRANSPORT_LOCAL = "local";

    /** */
    private final static String TRANSPORT_PSERVER = "pserver";

    /** */
    private final static String TRANSPORT_LSERVER = "lserver";

    /** */
    private final static String TRANSPORT_EXT = "ext";

    /** @requirement org.apache.maven.scm.CvsCommand */
    private Map commands;

    protected Map getCommands()
    {
        return commands;
    }

    public String getScmType()
    {
        return "cvs";
    }

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, String delimiter )
    	throws ScmRepositoryException
    {
        // TODO: make sure one can use different types of delimiters

        String[] tokens = StringUtils.split( scmSpecificUrl, delimiter );

        if ( tokens.length < 3 )
        {
            throw new ScmRepositoryException( "The connection string contains to few tokens." );
        }

        String cvsroot;

        String transport = tokens[0];

        if ( transport.equalsIgnoreCase( TRANSPORT_LOCAL ) )
        {
            // use the local repository directory eg. '/home/cvspublic'
            cvsroot = tokens[1];
        }
        else if ( transport.equalsIgnoreCase( TRANSPORT_PSERVER ) || transport.equalsIgnoreCase( TRANSPORT_LSERVER )
            || transport.equalsIgnoreCase( TRANSPORT_EXT ) )
        {
            if ( tokens.length != 4 )
            {
                throw new ScmRepositoryException( "The connection string contains to few tokens." );
            }

            if ( transport.equalsIgnoreCase( TRANSPORT_LSERVER ) )
            {
                //create the cvsroot as the local socket cvsroot
                cvsroot = tokens[1] + ":" + tokens[2];
            }
            else
            {
                //create the cvsroot as the remote cvsroot
                cvsroot = ":" + transport + ":" + tokens[1] + ":" + tokens[2];
            }
        }
        else
        {
            throw new ScmRepositoryException( "Unknown transport: " + transport );
        }

        String user = null;

        String host = null;

        if ( !transport.equalsIgnoreCase( TRANSPORT_LOCAL ) )
        {
            String userhost = tokens[1];

            int index = userhost.indexOf( "@" );

            if ( index == -1 )
            {
                throw new ScmRepositoryException( "The userhost part must be on the form: <username>@<hostname>." );
            }

            user = userhost.substring( 0, index );

            host = userhost.substring( index + 1 );
        }

        String path;

        String module;

        if ( transport.equals( TRANSPORT_LOCAL ) )
        {
            path = tokens[1];

            module = tokens[2];
        }
        else
        {
            path = tokens[2];

            module = tokens[3];
        }

        return new CvsScmProviderRepository( cvsroot, transport, user, host, path, module );
    }
}
