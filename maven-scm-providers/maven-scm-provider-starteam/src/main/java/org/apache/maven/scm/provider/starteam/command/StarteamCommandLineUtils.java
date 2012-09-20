package org.apache.maven.scm.provider.starteam.command;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.provider.starteam.util.StarteamUtil;
import org.apache.maven.scm.providers.starteam.settings.Settings;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Command line construction utility.
 *
 * @author Dan T. Tran
 *
 */
public final class StarteamCommandLineUtils
{

    private StarteamCommandLineUtils()
    {
    }

    private static Settings settings = StarteamUtil.getSettings();

    public static Commandline createStarteamBaseCommandLine( String action, StarteamScmProviderRepository repo )
    {
        Commandline cl = new Commandline();

        cl.createArg().setValue( "stcmd" );

        cl.createArg().setValue( action );

        cl.createArg().setValue( "-x" );

        cl.createArg().setValue( "-nologo" );

        cl.createArg().setValue( "-stop" );

        return cl;
    }

    private static Commandline addCommandlineArguments( Commandline cl, List<String> args )
    {
        if ( args == null )
        {
          return cl;
        }
        for ( String arg : args )
        {
            cl.createArg().setValue( arg );
        }
        return cl;
    }

    public static Commandline createStarteamCommandLine( String action, List<String> args, ScmFileSet scmFileSet,
                                                         StarteamScmProviderRepository repo )
    {
        Commandline cl = StarteamCommandLineUtils.createStarteamBaseCommandLine( action, repo );

        // case 1: scmFileSet has only basedir
        if ( scmFileSet.getFileList().size() == 0 )
        {
            //perform an action on directory
            cl.createArg().setValue( "-p" );
            cl.createArg().setValue( repo.getFullUrl() );
            cl.createArg().setValue( "-fp" );
            cl.createArg().setValue( scmFileSet.getBasedir().getAbsolutePath().replace( '\\', '/' ) );

            cl.createArg().setValue( "-is" );

            addCompressionOption( cl );

            addCommandlineArguments( cl, args );

            return cl;
        }

        //case 2 scmFileSet has a sub file, but we dont know if the sub file is a directory or a file
        File fileInFileSet = (File) scmFileSet.getFileList().get( 0 );
        File subFile = new File( scmFileSet.getBasedir(), fileInFileSet.getPath() );

        //Perform an scm action on a single file where the orignal
        // url and local directory ( -p and -fp options ) are altered
        // to deal with single file/subdirectory

        File workingDirectory = subFile;
        String scmUrl = repo.getFullUrl() + "/" + fileInFileSet.getPath().replace( '\\', '/' );
        if ( !subFile.isDirectory() )
        {
            workingDirectory = subFile.getParentFile();
            if ( fileInFileSet.getParent() != null )
            {
                scmUrl = repo.getFullUrl() + "/" + fileInFileSet.getParent().replace( '\\', '/' );
            }
            else
            {
                //subFile is right under root
                scmUrl = repo.getFullUrl();
            }
        }

        cl.createArg().setValue( "-p" );
        cl.createArg().setValue( scmUrl );

        cl.createArg().setValue( "-fp" );
        cl.createArg().setValue( workingDirectory.getPath().replace( '\\', '/' ) );

        cl.setWorkingDirectory( workingDirectory.getPath() );

        if ( subFile.isDirectory() )
        {
            cl.createArg().setValue( "-is" );
        }

        StarteamCommandLineUtils.addCompressionOption( cl );

        addCommandlineArguments( cl, args );

        if ( !subFile.isDirectory() )
        {
            cl.createArg().setValue( subFile.getName() );
        }

        return cl;
    }

    public static void addCompressionOption( Commandline cl )
    {
        if ( settings.isCompressionEnable() )
        {
            cl.createArg().setValue( "-cmp" );
        }
    }

    public static void addEOLOption( List<String> args )
    {
        if ( settings.getEol() != null  )
        {
            args.add( "-eol" );
            args.add( settings.getEol() );
        }
    }

    public static String toJavaPath( String path )
    {
        return path.replace( '\\', '/' );
    }

    /**
     * Hellper method to display command line without password
     *
     * @param cl
     * @return String
     * @throws ScmException
     */
    public static String displayCommandlineWithoutPassword( Commandline cl )
        throws ScmException
    {
        String retStr = "";

        String fullStr = cl.toString();

        //look for -p and take out the password arugment

        int usernamePos = fullStr.indexOf( "-p " ) + 3;

        if ( usernamePos == 2 )
        {
            //should never get here since all starteam command lines
            // have -p argument

            throw new ScmException( "Invalid command line" );
        }

        retStr = fullStr.substring( 0, usernamePos );

        int passwordStartPos = fullStr.indexOf( ':' );

        if ( passwordStartPos == -1 )
        {
            throw new ScmException( "Invalid command line" );
        }

        int passwordEndPos = fullStr.indexOf( '@' );

        if ( passwordEndPos == -1 )
        {
            throw new ScmException( "Invalid command line" );
        }

        retStr += fullStr.substring( usernamePos, passwordStartPos );

        retStr += fullStr.substring( passwordEndPos );

        return retStr;

    }

    public static int executeCommandline( Commandline cl, StreamConsumer consumer,
                                          CommandLineUtils.StringStreamConsumer stderr, ScmLogger logger )
        throws ScmException
    {
        if ( logger.isInfoEnabled() )
        {
            logger.info( "Command line: " + displayCommandlineWithoutPassword( cl ) );
        }

        try
        {
            return CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }
    }

    /**
     * Given 2 paths, make sure parent and child are on the same tree
     * return the port of child that not in parent
     *
     * @param parent
     * @param child
     * @return
     */
    public static String getRelativeChildDirectory( String parent, String child )
    {
        //expect parentDir contains childDir
        try
        {
            String childPath = new File( child ).getCanonicalFile().getPath().replace( '\\', '/' );

            String parentPath = new File( parent ).getCanonicalFile().getPath().replace( '\\', '/' );

            if ( !childPath.startsWith( parentPath ) )
            {
                throw new IllegalStateException();
            }

            String retDir = "." + childPath.substring( parentPath.length() );

            return retDir;

        }
        catch ( IOException e )
        {
            throw new IllegalStateException(
                "Unable to convert to canonical path of either " + parent + " or " + child );
        }
    }

}
