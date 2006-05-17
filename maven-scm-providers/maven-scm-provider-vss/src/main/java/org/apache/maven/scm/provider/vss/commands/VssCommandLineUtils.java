package org.apache.maven.scm.provider.vss.commands;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.apache.maven.scm.providers.vss.settings.Settings;
import org.apache.maven.scm.providers.vss.settings.io.xpp3.VssXpp3Reader;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class VssCommandLineUtils
    implements VssConstants
{
    public static void addFiles( Commandline cl, File[] files )
    {
        for ( int i = 0; i < files.length; i++ )
        {
            cl.createArgument().setValue( files[i].getPath().replace( '\\', '/' ) );
        }
    }

    public static Commandline getBaseSvnCommandLine( File workingDirectory, String cmd,
                                                     VssScmProviderRepository repository )
    {
        Commandline cl = new Commandline();

        cl.setExecutable( VssConstants.SS_EXE );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        if ( !StringUtils.isEmpty( repository.getUser() ) )
        {
            cl.createArgument().setValue( "-Y" );

            StringBuffer sb = new StringBuffer( repository.getUser() );
            if ( !StringUtils.isEmpty( repository.getPassword() ) )
            {
                sb.append( "," ).append( repository.getPassword() );
            }
            cl.createArgument().setValue( sb.toString() );
        }

        return cl;
    }

    public static int executeCommandline( Commandline cl, StreamConsumer consumer,
                                          CommandLineUtils.StringStreamConsumer stderr, ScmLogger logger )
        throws ScmException
    {
        try
        {
            logger.info( "Executing: " + cl );
            logger.info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

            return CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }
    }


    public static final Settings getSettings()
    {
        Settings settings = null;
        File scmUserHome = new File( System.getProperty( "user.home" ), ".scm" );
        File settingsFile = new File( scmUserHome, "vss-settings.xml" );
        if ( settingsFile.exists() )
        {
            VssXpp3Reader reader = new VssXpp3Reader();
            try
            {
                settings = reader.read( new FileReader( settingsFile ) );
            }
            catch ( FileNotFoundException e )
            {
            }
            catch ( IOException e )
            {
            }
            catch ( XmlPullParserException e )
            {
                String message = settingsFile.getAbsolutePath() + " isn't well formed. SKIPPED." + e.getMessage();

                System.out.println( message );
            }
        }
        return settings;
    }

    public static final String getSsDir()
    {
        String ssDir = "";
        if ( VssCommandLineUtils.getSettings() != null )
        {
            ssDir = VssCommandLineUtils.getSettings().getVssDirectory();

            ssDir = StringUtils.replace( ssDir, "\\", "/" );

            if ( !ssDir.endsWith( "/" ) )
            {
                ssDir += "/";
            }
        }
        return ssDir;
    }
}
