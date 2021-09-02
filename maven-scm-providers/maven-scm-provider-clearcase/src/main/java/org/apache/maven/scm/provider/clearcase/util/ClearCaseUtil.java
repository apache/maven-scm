package org.apache.maven.scm.provider.clearcase.util;

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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.maven.scm.providers.clearcase.settings.Settings;
import org.apache.maven.scm.providers.clearcase.settings.io.xpp3.ClearcaseXpp3Reader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public final class ClearCaseUtil
{

    protected static final String CLEARCASE_SETTINGS_FILENAME = "clearcase-settings.xml";

    public static final File DEFAULT_SETTINGS_DIRECTORY = new File( System.getProperty( "user.home" ), ".scm" );

    private static File settingsDirectory = DEFAULT_SETTINGS_DIRECTORY;

    private static final String RESOURCE_FILENAME = "org.apache.maven.scm.provider.clearcase.command.clearcase";

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( RESOURCE_FILENAME );

    private static Settings settings;

    private ClearCaseUtil()
    {
    }

    public static String getLocalizedResource( String key )
    {
        return RESOURCE_BUNDLE.getString( key );
    }

    public static Settings getSettings() 
    {
        if ( settings == null )
        {
            settings = readSettings();
        }
        return settings;
    }
    
    public static Settings readSettings() 
    {
        File settingsFile = new File( settingsDirectory, CLEARCASE_SETTINGS_FILENAME );

        if ( !settingsFile.exists() )
        {
            File scmGlobalDir = new File( System.getProperty( "maven.home" ), "conf" );
            settingsFile = new File( scmGlobalDir, CLEARCASE_SETTINGS_FILENAME );
        }

        if ( settingsFile.exists() )
        {
            ClearcaseXpp3Reader reader = new ClearcaseXpp3Reader();
            try
            {
                return reader.read( ReaderFactory.newXmlReader( settingsFile ) );
            }
            catch ( IOException e )
            {
                // nop
            }
            catch ( XmlPullParserException e )
            {
                String message = settingsFile.getAbsolutePath() + " isn't well formed. SKIPPED." + e.getMessage();

                System.out.println( message );
            }
        }

        return new Settings();
    }

    public static void setSettingsDirectory( File directory )
    {
        settingsDirectory = directory;
        settings = readSettings();
    }
}
