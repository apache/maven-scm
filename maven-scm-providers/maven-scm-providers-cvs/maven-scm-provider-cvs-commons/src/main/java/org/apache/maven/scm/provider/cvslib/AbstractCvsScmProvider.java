package org.apache.maven.scm.provider.cvslib;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractCvsScmProvider
    extends AbstractScmProvider
{
    /** */
    public final static String TRANSPORT_LOCAL = "local";

    /** */
    public final static String TRANSPORT_PSERVER = "pserver";

    /** */
    public final static String TRANSPORT_LSERVER = "lserver";

    /** */
    public final static String TRANSPORT_EXT = "ext";

    /** */
    public final static String TRANSPORT_SSPI = "sspi";

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

    public String getScmSpecificFilename()
    {
        return "CVS";
    }

    /* From the Cederqvist:
    *
    * "Tag names must start with an uppercase or lowercase letter and can
    * contain uppercase and lowercase letters, digits, `-', and `_'. The
    * two tag names BASE and HEAD are reserved for use by CVS. It is expected
    * that future names which are special to CVS will be specially named,
    * for example by starting with `.', rather than being named analogously
    * to BASE and HEAD, to avoid conflicts with actual tag names."
    */

    /* (non-Javadoc)
    * @see org.apache.maven.scm.provider.AbstractScmProvider#sanitizeTagName(java.lang.String)
    */

    public String sanitizeTagName( String arg0 )
    {
        if ( validateTagName( arg0 ) )
        {
            return arg0;
        }

        if ( arg0.equals( "HEAD" ) || arg0.equals( "BASE" ) || !arg0.matches( "[A-Za-z].*" ) )
            /* we don't even bother to sanitize these, they're just silly */
        {
            throw new RuntimeException(
                "Unable to sanitize tag " + arg0 + ": must begin with a letter" + "and not be HEAD or BASE" );
        }

        /* swap all illegal characters for a _ */
        return arg0.replaceAll( "[^A-Za-z0-9_-]", "_" );
    }

    /* (non-Javadoc)
    * @see org.apache.maven.scm.provider.AbstractScmProvider#validateTagName(java.lang.String)
    */
    public boolean validateTagName( String arg0 )
    {
        return ( arg0.matches( "[A-Za-z][A-Za-z0-9_-]*" ) && !arg0.equals( "HEAD" ) && !arg0.equals( "BASE" ) );
    }

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
            result.messages.add( "The connection string contains too few tokens." );

            return result;
        }

        String cvsroot;

        String transport = tokens[0];

        if ( transport.equalsIgnoreCase( TRANSPORT_LOCAL ) )
        {
            // use the local repository directory eg. '/home/cvspublic'
            cvsroot = tokens[1];
        }
        else if ( transport.equalsIgnoreCase( TRANSPORT_PSERVER ) || transport.equalsIgnoreCase( TRANSPORT_LSERVER ) ||
            transport.equalsIgnoreCase( TRANSPORT_EXT ) || transport.equalsIgnoreCase( TRANSPORT_SSPI ) )
        {
            if ( tokens.length != 4 && transport.equalsIgnoreCase( TRANSPORT_EXT ) )
            {
                result.messages.add( "The connection string contains too few tokens." );

                return result;
            }
            else if ( ( tokens.length < 4 || tokens.length > 6 ) && transport.equalsIgnoreCase( TRANSPORT_PSERVER ) )
            {
                result.messages.add( "The connection string contains too few tokens." );

                return result;
            }
            else if ( tokens.length < 4 || tokens.length > 5 && !transport.equalsIgnoreCase( TRANSPORT_PSERVER ) )
            {
                result.messages.add( "The connection string contains too few tokens." );

                return result;
            }
            else if ( tokens.length != 4 && transport.equalsIgnoreCase( TRANSPORT_SSPI ) )
            {
                result.messages.add( "The connection string contains an incorrect number of tokens (should be four)." );

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
            // set default port, it's necessary for checking entries in .cvspass
            port = 2401;

            if ( tokens.length == 4 )
            {
                //pserver:[username@]host:path:module
                String userhost = tokens[1];

                int index = userhost.indexOf( "@" );

                if ( index == -1 )
                {
                    host = userhost;
                }
                else
                {
                    user = userhost.substring( 0, index );

                    host = userhost.substring( index + 1 );
                }

                path = tokens[2];

                module = tokens[3];
            }
            else if ( tokens.length == 6 )
            {
                //pserver:username:password@host:port:path:module
                user = tokens[1];

                String passhost = tokens[2];

                int index = passhost.indexOf( "@" );

                if ( index == -1 )
                {
                    result.messages
                        .add( "The user_password_host part must be on the form: <username>:<password>@<hostname>." );

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
                    //pserver:username@host:port:path:module
                    String userhost = tokens[1];

                    int index = userhost.indexOf( "@" );

                    user = userhost.substring( 0, index );

                    host = userhost.substring( index + 1 );

                    port = new Integer( tokens[2] ).intValue();
                }
                else if ( tokens[2].indexOf( "@" ) >= 0 )
                {
                    //pserver:username:password@host:path:module
                    //<username>:<password>@<hostname>
                    user = tokens[1];

                    String passhost = tokens[2];

                    int index = passhost.indexOf( "@" );

                    password = passhost.substring( 0, index );

                    host = passhost.substring( index + 1 );
                }
                else
                {
                    //pserver:host:port:path:module
                    try
                    {
                        port = new Integer( tokens[2] ).intValue();
                    }
                    catch ( Exception e )
                    {
                        //incorrect
                        result.messages.add( "Your scm url is invalid." );

                        return result;
                    }

                    host = tokens[1];
                }

                path = tokens[3];

                module = tokens[4];
            }

            String userHost = host;

            if ( user != null )
            {
                userHost = user + "@" + host;
            }

            // cvsroot format is :pserver:[user@]host:[port]path
            cvsroot = ":" + transport + ":" + userHost + ":";

            if ( port != -1 )
            {
                cvsroot += port;
            }

            cvsroot += path;
        }
        else if ( transport.equalsIgnoreCase( TRANSPORT_SSPI ) )
        {
            //sspi:[username@]host:path:module
            String userhost = tokens[1];

            int index = userhost.indexOf( "@" );

            if ( index == -1 )
            {
                user = "";

                host = userhost;
            }
            else
            {
                user = userhost.substring( 0, index );

                host = userhost.substring( index + 1 );
            }

            path = tokens[2];

            module = tokens[3];

            // cvsroot format is :sspi:host:path
            cvsroot = ":" + transport + ":" + host + ":" + path;
        }
        else
        {
            if ( !transport.equalsIgnoreCase( TRANSPORT_LOCAL ) )
            {
                String userhost = tokens[1];

                int index = userhost.indexOf( "@" );

                if ( index == -1 )
                {
                    host = userhost;
                }
                else
                {
                    user = userhost.substring( 0, index );

                    host = userhost.substring( index + 1 );
                }
            }

            if ( transport.equals( TRANSPORT_LOCAL ) )
            {
                path = tokens[1];

                module = tokens[2];

                if ( module != null && module.startsWith( "/" ) )
                {
                    module = module.substring( 1 );
                }

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
            result.repository =
                new CvsScmProviderRepository( cvsroot, transport, user, password, host, port, path, module );
        }

        return result;
    }

    private ScmResult executeCommand( Command command, ScmRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        command.setLogger( getLogger() );

        return command.execute( repository.getProviderRepository(), fileSet, parameters );
    }

    protected abstract Command getAddCommand();

    protected abstract Command getChangeLogCommand();

    protected abstract Command getCheckInCommand();

    protected abstract Command getCheckOutCommand();

    protected abstract Command getDiffCommand();

    protected abstract Command getListCommand();

    protected abstract Command getLoginCommand();

    protected abstract Command getRemoveCommand();

    protected abstract Command getStatusCommand();

    protected abstract Command getTagCommand();

    protected abstract Command getUpdateCommand();

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#add(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (AddScmResult) executeCommand( getAddCommand(), repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#changelog(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public ChangeLogScmResult changelog( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (ChangeLogScmResult) executeCommand( getChangeLogCommand(), repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkin(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public CheckInScmResult checkin( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (CheckInScmResult) executeCommand( getCheckInCommand(), repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkout(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public CheckOutScmResult checkout( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (CheckOutScmResult) executeCommand( getCheckOutCommand(), repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#diff(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (DiffScmResult) executeCommand( getDiffCommand(), repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#login(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public LoginScmResult login( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (LoginScmResult) executeCommand( getLoginCommand(), repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#remove(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (RemoveScmResult) executeCommand( getRemoveCommand(), repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#status(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (StatusScmResult) executeCommand( getStatusCommand(), repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#tag(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public TagScmResult tag( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (TagScmResult) executeCommand( getTagCommand(), repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#update(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (UpdateScmResult) executeCommand( getUpdateCommand(), repository, fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#list(org.apache.maven.scm.repository.ScmRepository,org.apache.maven.scm.ScmFileSet,org.apache.maven.scm.CommandParameters)
     */
    protected ListScmResult list( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (ListScmResult) executeCommand( getListCommand(), repository, fileSet, parameters );
    }

    /**
     * CVS provider requires that all files in ScmFileSet must be relative to basedir
     * This function ensures and converts all absolute paths to relative paths
     *
     * @param currentFileSet
     * @return
     * @throws ScmException
     */
    private static ScmFileSet fixUpScmFileSetAbsoluteFilePath( ScmFileSet currentFileSet )
        throws ScmException
    {
        ScmFileSet newFileSet = null;
        try
        {
            File basedir = getAbsoluteFilePath( currentFileSet.getBasedir() );

            File[] files = currentFileSet.getFiles();

            for ( int i = 0; i < files.length; ++i )
            {
                if ( files[i].isAbsolute() )
                {
                    files[i] = new File( getRelativePath( basedir, files[i] ) );
                }
            }

            newFileSet = new ScmFileSet( basedir, files );
        }
        catch ( IOException e )
        {
            throw new ScmException( "Invalid file set.", e );
        }

        return newFileSet;
    }

    public static String getRelativePath( File basedir, File f )
        throws ScmException, IOException
    {
        File fileOrDir = getAbsoluteFilePath( f );

        if ( !fileOrDir.getPath().startsWith( basedir.getPath() ) )
        {
            throw new ScmException( fileOrDir.getPath() + " was not contained in " + basedir.getPath() );
        }

        return fileOrDir.getPath().substring( basedir.getPath().length() + 1, fileOrDir.getPath().length() );
    }

    private static File getAbsoluteFilePath( File fileOrDir )
        throws IOException
    {
        String javaPathString = fileOrDir.getCanonicalPath().replace( '\\', '/' );

        if ( javaPathString.endsWith( "/" ) )
        {
            javaPathString = javaPathString.substring( 0, javaPathString.length() - 1 );
        }

        return new File( javaPathString );
    }
}
