package org.apache.maven.scm.provider.svn.svnexe.command;

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

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.util.SvnUtil;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;

/**
 * Command line construction utility.
 *
 * @author Brett Porter
 * @version $Id$
 */
public class SvnCommandLineUtils
{
    public static void addFiles( Commandline cl, File[] files )
    {
        for ( int i = 0; i < files.length; i++ )
        {
            cl.createArgument().setValue( files[i].getPath().replace( '\\', '/' ) );
        }
    }

    public static Commandline getBaseSvnCommandLine( File workingDirectory, SvnScmProviderRepository repository )
    {
        Commandline cl = new Commandline();

        cl.setExecutable( "svn" );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        if ( !StringUtils.isEmpty( System.getProperty( "maven.scm.svn.config_directory" ) ) )
        {
            cl.createArgument().setValue( "--config-dir" );
            cl.createArgument().setValue( System.getProperty( "maven.scm.svn.config_directory" ) );
        }
        else if ( !StringUtils.isEmpty( SvnUtil.getSettings().getConfigDirectory() ) )
        {
            cl.createArgument().setValue( "--config-dir" );
            cl.createArgument().setValue( SvnUtil.getSettings().getConfigDirectory() );
        }

        if ( !StringUtils.isEmpty( repository.getUser() ) )
        {
            cl.createArgument().setValue( "--username" );

            cl.createArgument().setValue( repository.getUser() );
        }

        if ( !StringUtils.isEmpty( repository.getPassword() ) )
        {
            cl.createArgument().setValue( "--password" );

            cl.createArgument().setValue( repository.getPassword() );
        }

        cl.createArgument().setValue( "--non-interactive" );

        return cl;
    }

    public static int execute( Commandline cl, StreamConsumer consumer, CommandLineUtils.StringStreamConsumer stderr,
                               ScmLogger logger )
        throws CommandLineException
    {
        int exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );

        exitCode = checkIfCleanUpIsNeeded( exitCode, cl, consumer, stderr, logger );

        return exitCode;
    }

    public static int execute( Commandline cl, CommandLineUtils.StringStreamConsumer stdout,
                               CommandLineUtils.StringStreamConsumer stderr, ScmLogger logger )
        throws CommandLineException
    {
        int exitCode = CommandLineUtils.executeCommandLine( cl, stdout, stderr );

        exitCode = checkIfCleanUpIsNeeded( exitCode, cl, stdout, stderr, logger );

        return exitCode;
    }

    private static int checkIfCleanUpIsNeeded( int exitCode, Commandline cl, StreamConsumer consumer,
                                               CommandLineUtils.StringStreamConsumer stderr, ScmLogger logger )
        throws CommandLineException
    {
        if ( exitCode != 0 && stderr.getOutput() != null && stderr.getOutput().indexOf( "'svn cleanup'" ) > 0 &&
            stderr.getOutput().indexOf( "'svn help cleanup'" ) > 0 )
        {
            logger.info( "Svn command failed due to some locks in working copy. We try to run a 'svn cleanup'." );

            if ( executeCleanUp( cl.getWorkingDirectory(), consumer, stderr ) == 0 )
            {
                exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
            }
        }
        return exitCode;
    }

    public static int executeCleanUp( File workinDirectory, StreamConsumer stdout, StreamConsumer stderr )
        throws CommandLineException
    {
        Commandline cl = new Commandline();
        cl.setExecutable( "svn" );
        cl.setWorkingDirectory( workinDirectory.getAbsolutePath() );
        return CommandLineUtils.executeCommandLine( cl, stdout, stderr );
    }
}
