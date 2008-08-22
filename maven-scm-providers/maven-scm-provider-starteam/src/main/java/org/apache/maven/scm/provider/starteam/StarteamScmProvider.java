package org.apache.maven.scm.provider.starteam;

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
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.add.StarteamAddCommand;
import org.apache.maven.scm.provider.starteam.command.changelog.StarteamChangeLogCommand;
import org.apache.maven.scm.provider.starteam.command.checkin.StarteamCheckInCommand;
import org.apache.maven.scm.provider.starteam.command.checkout.StarteamCheckOutCommand;
import org.apache.maven.scm.provider.starteam.command.diff.StarteamDiffCommand;
import org.apache.maven.scm.provider.starteam.command.edit.StarteamEditCommand;
import org.apache.maven.scm.provider.starteam.command.remove.StarteamRemoveCommand;
import org.apache.maven.scm.provider.starteam.command.status.StarteamStatusCommand;
import org.apache.maven.scm.provider.starteam.command.tag.StarteamTagCommand;
import org.apache.maven.scm.provider.starteam.command.unedit.StarteamUnEditCommand;
import org.apache.maven.scm.provider.starteam.command.update.StarteamUpdateCommand;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="starteam"
 */
public class StarteamScmProvider
    extends AbstractScmProvider
{
    public static final String STARTEAM_URL_FORMAT =
        "[username[:password]@]hostname:port:/projectName/[viewName/][folderHiearchy/]";

    // ----------------------------------------------------------------------
    // ScmProvider Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        String user = null;

        String password = null;

        int index = scmSpecificUrl.indexOf( '@' );

        String rest = scmSpecificUrl;

        if ( index != -1 )
        {
            String userAndPassword = scmSpecificUrl.substring( 0, index );

            rest = scmSpecificUrl.substring( index + 1 );

            index = userAndPassword.indexOf( ":" );

            if ( index != -1 )
            {
                user = userAndPassword.substring( 0, index );

                password = userAndPassword.substring( index + 1 );
            }
            else
            {
                user = userAndPassword;
            }
        }

        String[] tokens = StringUtils.split( rest, Character.toString( delimiter ) );

        String host;

        int port;

        String path;

        if ( tokens.length == 3 )
        {
            host = tokens[0];

            port = new Integer( tokens[1] ).intValue();

            path = tokens[2];
        }
        else if ( tokens.length == 2 )
        {
            getLogger().warn( "Your scm URL use a deprecated format. The new format is :" + STARTEAM_URL_FORMAT );

            host = tokens[0];

            if ( tokens[1].indexOf( '/' ) == -1 )
            {
                throw new ScmRepositoryException(
                    "Invalid SCM URL: The url has to be on the form: " + STARTEAM_URL_FORMAT );
            }

            int at = tokens[1].indexOf( '/' );

            port = new Integer( tokens[1].substring( 0, at ) ).intValue();

            path = tokens[1].substring( at );
        }
        else
        {
            throw new ScmRepositoryException(
                "Invalid SCM URL: The url has to be on the form: " + STARTEAM_URL_FORMAT );
        }

        try
        {
            return new StarteamScmProviderRepository( user, password, host, port, path );
        }
        catch ( Exception e )
        {
            throw new ScmRepositoryException(
                "Invalid SCM URL: The url has to be on the form: " + STARTEAM_URL_FORMAT );
        }
    }

    public String getScmType()
    {
        return "starteam";
    }

    /** {@inheritDoc} */
    public AddScmResult add( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamAddCommand command = new StarteamAddCommand();

        command.setLogger( getLogger() );

        return (AddScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public ChangeLogScmResult changelog( ScmProviderRepository repository, ScmFileSet fileSet,
                                         CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamChangeLogCommand command = new StarteamChangeLogCommand();

        command.setLogger( getLogger() );

        return (ChangeLogScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckInScmResult checkin( ScmProviderRepository repository, ScmFileSet fileSet,
                                     CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamCheckInCommand command = new StarteamCheckInCommand();

        command.setLogger( getLogger() );

        return (CheckInScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamCheckOutCommand command = new StarteamCheckOutCommand();

        command.setLogger( getLogger() );

        return (CheckOutScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public DiffScmResult diff( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamDiffCommand command = new StarteamDiffCommand();

        command.setLogger( getLogger() );

        return (DiffScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamStatusCommand command = new StarteamStatusCommand();

        command.setLogger( getLogger() );

        return (StatusScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamTagCommand command = new StarteamTagCommand();

        command.setLogger( getLogger() );

        return (TagScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamUpdateCommand command = new StarteamUpdateCommand();

        command.setLogger( getLogger() );

        return (UpdateScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected EditScmResult edit( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamEditCommand command = new StarteamEditCommand();

        command.setLogger( getLogger() );

        return (EditScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    protected UnEditScmResult unedit( ScmProviderRepository repository, ScmFileSet fileSet,
                                      CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamUnEditCommand command = new StarteamUnEditCommand();

        command.setLogger( getLogger() );

        return (UnEditScmResult) command.execute( repository, fileSet, parameters );
    }

    /** {@inheritDoc} */
    public RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        fileSet = fixUpScmFileSetAbsoluteFilePath( fileSet );

        StarteamRemoveCommand command = new StarteamRemoveCommand();

        command.setLogger( getLogger() );

        return (RemoveScmResult) command.execute( repository, fileSet, parameters );
    }

    /**
     * Starteam provider requires that all files in ScmFileSet must be relative to basedir
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

            newFileSet = new ScmFileSet( basedir, Arrays.asList( files ) );
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

        if ( !fileOrDir.getCanonicalPath().startsWith( basedir.getCanonicalPath() ) )
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
