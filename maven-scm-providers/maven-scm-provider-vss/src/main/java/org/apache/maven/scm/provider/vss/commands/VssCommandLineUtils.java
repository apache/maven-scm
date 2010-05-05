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
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * @author <a href="mailto:triek@thrx.de">Thorsten Riek</a>
 * @version $Id$
 */
public class VssCommandLineUtils
    // FIXME extend CommandLineUtils
{
    private static File scmConfDir = new File( System.getProperty( "user.home" ), ".scm" );

    public static void addFiles( Commandline cl, ScmFileSet fileSet )
    {
        Iterator it = fileSet.getFileList().iterator();

        while ( it.hasNext() )
        {
            File file = (File) it.next();

            cl.createArg().setValue( file.getPath().replace( '\\', '/' ) );
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
            cl.createArg().setValue( "-Y" );

            StringBuffer sb = new StringBuffer( repository.getUser() );
            if ( !StringUtils.isEmpty( repository.getPassword() ) )
            {
                sb.append( "," ).append( repository.getPassword() );
            }
            cl.createArg().setValue( sb.toString() );
        }

        return cl;
    }

    public static int executeCommandline( Commandline cl, StreamConsumer consumer,
                                          CommandLineUtils.StringStreamConsumer stderr, ScmLogger logger )
        throws ScmException
    {
        try
        {
            if ( logger.isInfoEnabled() )
            {
                logger.info( "Executing: " + cl );
                logger.info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
            }

            int exitcode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );

            if ( logger.isDebugEnabled() )
            {
                logger.debug( "VSS Command Exit_Code: " + exitcode );
            }

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
        File settingsFile = getScmConfFile();
        if ( settingsFile.exists() )
        {
            VssXpp3Reader reader = new VssXpp3Reader();
            try
            {
                settings = reader.read( ReaderFactory.newXmlReader( settingsFile ) );
            }
            catch ( FileNotFoundException e )
            {
                // nop
            }
            catch ( IOException e )
            {
                // nop
            }
            catch ( XmlPullParserException e )
            {
                String message = settingsFile.getAbsolutePath() + " isn't well formed. SKIPPED." + e.getMessage();

                System.err.println( message );
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
            String ssDir2 = VssCommandLineUtils.getSettings().getVssDirectory();

            if ( ssDir2 != null )
            {
                ssDir = StringUtils.replace( ssDir2, "\\", "/" );

                if ( !ssDir.endsWith( "/" ) )
                {
                    ssDir += "/";
                }
            }
        }
        return ssDir;
    }
    
    public static File getScmConfFile() 
    {
    	return new File( scmConfDir, "vss-settings.xml" );
    }
}
