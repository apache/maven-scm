package org.apache.maven.scm.provider.cvslib;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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

import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

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

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static class ScmUrlParserResult
    {
        List messages = new ArrayList();

        ScmProviderRepository repository;
    }

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        ScmUrlParserResult result = parseScmUrl( scmSpecificUrl, delimiter );

        if ( result.messages.size() > 0 )
        {
            throw new ScmRepositoryException( "The scm url is invalid.", result.messages );
        }

        return result.repository;
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#makeProviderScmRepository(java.io.File)
     */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        if ( path == null || !path.isDirectory() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a valid directory." );
        }

        File cvsDirectory = new File( path, "CVS" );

        if ( !cvsDirectory.exists() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a cvs checkout directory." );
        }

        File cvsRootFile = new File( cvsDirectory, "Root" );
        File moduleFile = new File( cvsDirectory, "Repository" );
        String cvsRoot;
        String module;

        try
        {
            cvsRoot = FileUtils.fileRead( cvsRootFile ).trim().substring( 1 );
        }
        catch ( IOException e )
        {
            throw new ScmRepositoryException( "Can't read " + cvsRootFile.getAbsolutePath() );
        }
        try
        {
            module = FileUtils.fileRead( moduleFile ).trim();
        }
        catch ( IOException e )
        {
            throw new ScmRepositoryException( "Can't read " + moduleFile.getAbsolutePath() );
        }

        return makeProviderScmRepository( cvsRoot + ":" + module, ':' );
    }

    public List validateScmUrl( String scmSpecificUrl, char delimiter )
    {
        ScmUrlParserResult result = parseScmUrl( scmSpecificUrl, delimiter );

        return result.messages;
    }

    // ----------------------------------------------------------------------
    // AbstractScmProvider Implementation
    // ----------------------------------------------------------------------

    protected Map getCommands()
    {
        return commands;
    }

    public String getScmType()
    {
        return "cvs";
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private ScmUrlParserResult parseScmUrl( String scmSpecificUrl, char delimiter )
    {
        ScmUrlParserResult result = new ScmUrlParserResult();

        String[] tokens = StringUtils.split( scmSpecificUrl, Character.toString( delimiter ) );

        if ( tokens.length < 3 )
        {
            result.messages.add( "The connection string contains to few tokens." );

            return result;
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
            if ( tokens.length != 4 && transport.equalsIgnoreCase( TRANSPORT_EXT ) )
            {
                result.messages.add( "The connection string contains to few tokens." );

                return result;
            }
            else if ( ( tokens.length < 4 || tokens.length > 6 ) && transport.equalsIgnoreCase( TRANSPORT_PSERVER ) )
            {
                result.messages.add( "The connection string contains to few tokens." );

                return result;
            }
            else if ( tokens.length < 4 || tokens.length > 5 && !transport.equalsIgnoreCase( TRANSPORT_PSERVER ) )
            {
                result.messages.add( "The connection string contains to few tokens." );

                return result;
            }

            if ( transport.equalsIgnoreCase( TRANSPORT_LSERVER ) )
            {
                //create the cvsroot as the local socket cvsroot
                cvsroot = tokens[1] + ":" + tokens[2];
            }
            else
            {
                //create the cvsroot as the remote cvsroot
                if ( tokens.length == 4 )
                {
                    cvsroot = ":" + transport + ":" + tokens[1] + ":" + tokens[2];
                }
                else
                {
                    cvsroot = ":" + transport + ":" + tokens[1] + ":" + tokens[2] + ":" + tokens[3];
                }
            }
        }
        else
        {
            result.messages.add( "Unknown transport: " + transport );

            return result;
        }

        String user = null;

        String password = null;

        String host = null;

        String path = null;

        String module = null;

        int port = -1;

        if ( transport.equalsIgnoreCase( TRANSPORT_PSERVER ) )
        {
            if ( tokens.length == 4 )
            {
                String userhost = tokens[1];

                int index = userhost.indexOf( "@" );

                if ( index == -1 )
                {
                    result.messages.add( "The userhost part must be on the form: <username>@<hostname>." );

                    return result;
                }

                user = userhost.substring( 0, index );

                host = userhost.substring( index + 1 );

                path = tokens[2];

                module = tokens[3];
            }
            else if ( tokens.length == 6 )
            {
                user = tokens[1];

                String passhost = tokens[2];

                int index = passhost.indexOf( "@" );

                if ( index == -1 )
                {
                    result.messages.add( "The user_password_host part must be on the form: <username>:<password>@<hostname>." );

                    return result;
                }

                password = passhost.substring( 0, index );

                host = passhost.substring( index + 1 );

                port = new Integer( tokens[3] ).intValue();

                path = tokens[4];

                module = tokens[5];
            }
            else
            {
                //tokens.length == 5
                if ( tokens[1].indexOf( "@" ) > 0 )
                {
                    //<username>@<hostname>:<port>
                    String userhost = tokens[1];

                    int index = userhost.indexOf( "@" );

                    if ( index == -1 )
                    {
                        result.messages.add( "The userhost part must be on the form: <username>@<hostname>." );

                        return result;
                    }

                    user = userhost.substring( 0, index );

                    host = userhost.substring( index + 1 );

                    port = new Integer( tokens[2] ).intValue();
                }
                else if ( tokens[2].indexOf( "@" ) > 0 )
                {
                    //<username>:<password>@<hostname>
                    user = tokens[1];

                    String passhost = tokens[2];

                    int index = passhost.indexOf( "@" );

                    if ( index == -1 )
                    {
                        result.messages.add( "The user_password_host part must be on the form: <username>:<password>@<hostname>." );

                        return result;
                    }

                    password = passhost.substring( 0, index );

                    host = passhost.substring( index + 1 );
                }
                else
                {
                    //incorrect
                    result.messages.add( "You need to specify an user in the url." );

                    return result;
                }

                path = tokens[3];

                module = tokens[4];
            }
            
            String userHostPort = host;
            if ( user != null )
            {
                userHostPort = user + "@" + host;
            }
            if ( port != -1 )
            {
                userHostPort += ":" + port;
            }
            cvsroot = ":" + transport + ":" + userHostPort + ":" + path;
        }
        else
        {
            if ( !transport.equalsIgnoreCase( TRANSPORT_LOCAL ) )
            {
                String userhost = tokens[1];

                int index = userhost.indexOf( "@" );

                if ( index == -1 )
                {
                    result.messages.add( "The userhost part must be on the form: <username>@<hostname>." );

                    return result;
                }

                user = userhost.substring( 0, index );

                host = userhost.substring( index + 1 );
            }

            if ( transport.equals( TRANSPORT_LOCAL ) )
            {
                path = tokens[1];

                module = tokens[2];
            }
            else
            {
                if ( tokens.length == 4 )
                {
                    path = tokens[2];

                    module = tokens[3];
                }
                else
                {
                    port = new Integer( tokens[2] ).intValue();

                    path = tokens[3];

                    module = tokens[4];
                }
            }
        }

        if ( port == -1 )
        {
            result.repository = new CvsScmProviderRepository( cvsroot, transport, user, password, host, path, module );
        }
        else
        {
            result.repository = new CvsScmProviderRepository( cvsroot, transport, user, password, host, port, path,
                                                              module );
        }

        return result;
    }
}
