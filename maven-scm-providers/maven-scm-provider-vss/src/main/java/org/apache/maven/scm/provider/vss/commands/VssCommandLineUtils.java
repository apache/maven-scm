package org.apache.maven.scm.provider.vss.commands;

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
import java.util.Iterator;

/**
 * @author <a href="mailto:triek@thrx.de">Thorsten Riek</a>
 * @version $Id$
 */
public class VssCommandLineUtils
    implements VssConstants  // FIXME extend CommandLineUtils
{
    private static File scmConfDir = new File( System.getProperty( "user.home" ), ".scm" );

    public static void addFiles( Commandline cl, ScmFileSet fileSet )
    {
        Iterator it = fileSet.getFileList().iterator();

        while ( it.hasNext() )
        {
            File file = (File) it.next();

            cl.createArgument().setValue( file.getPath().replace( '\\', '/' ) );
        }

    }

    public static Commandline getBaseVssCommandLine( File workingDirectory, String cmd,
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

            int exitcode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );

            logger.debug( "VSS Command Exit_Code: " + exitcode );

            return exitcode;
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }
    }


    public static final Settings getSettings()
    {
        Settings settings = null;
        File settingsFile = new File( scmConfDir, "vss-settings.xml" );
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

        // override settings with command line options
        String vssDirectory = System.getProperty( "vssDirectory" );
        if ( StringUtils.isNotEmpty( vssDirectory ) )
        {
            if ( settings == null )
            {
                settings = new Settings();
            }
            settings.setVssDirectory( vssDirectory );
        }
        return settings;
    }

    protected static final File getScmConfDir()
    {
        return scmConfDir;
    }

    protected static final void setScmConfDir( File directory )
    {
        scmConfDir = directory;
    }

    public static final String getSsDir()
    {
        String ssDir = "";
        if ( VssCommandLineUtils.getSettings() != null )
        {
            String _ssDir = VssCommandLineUtils.getSettings().getVssDirectory();

            if ( _ssDir != null )
            {
                ssDir = StringUtils.replace( _ssDir, "\\", "/" );

                if ( !ssDir.endsWith( "/" ) )
                {
                    ssDir += "/";
                }
            }
        }
        return ssDir;
    }
}
