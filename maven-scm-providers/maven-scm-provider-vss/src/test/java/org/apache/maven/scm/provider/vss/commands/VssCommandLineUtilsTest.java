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

import org.apache.maven.scm.ScmTestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Allan Lang
 *
 */
public class VssCommandLineUtilsTest
    extends ScmTestCase
{

    private static final String VSS_DIRECTORY_PROPERTY = "vssDirectory";

    //private ScmManager scmManager;

    public void setUp()
        throws Exception
    {
        super.setUp();
        System.setProperty( VSS_DIRECTORY_PROPERTY, "" );

        //scmManager = getScmManager();
    }

    public void testGetSettings()
        throws IOException
    {
        /*
         * If we have a genuine settings file, take a copy of this first Create
         * the test settings file Check getSettings from file Delete the test
         * settings file Check getSettings without file Check getSettings with
         * system property Re-instate original settings file, if existing
         *
         */

        final String vssInstallPath = "c:\\wherever";
        final String vssInstallPathAlt = "c:\\somewhere";
        final String settingsXml =
            "<vss-settings><vssDirectory>" + vssInstallPath + "</vssDirectory></vss-settings>";
        final String settingsFilename = "vss-settings.xml";
        final String backupFilename = settingsFilename + ".backup";
        boolean preExistingScmFolder = false;
        boolean preExistingSettings = false;

        /*
         * Create a backup of the current settings file, if one exists
         */
        File scmUserHome = new File( getTestFile( "target/vssdir" ), ".scm" );
        if ( scmUserHome.exists() )
        {
            preExistingScmFolder = true;
            File settingsFile = new File( scmUserHome, settingsFilename );
            if ( settingsFile.exists() )
            {
                preExistingSettings = true;
                settingsFile.renameTo( new File( scmUserHome, backupFilename ) );
            }
        }
        else
        {
            scmUserHome.mkdirs();
        }

        /*
         * Create the test settings file
         */
        File testSettings = new File( scmUserHome, settingsFilename );
        FileOutputStream fos = new FileOutputStream( testSettings );
        fos.write( settingsXml.getBytes() );
        fos.flush();
        fos.close();
        fos = null;

        /*
         * Validate that setting from settings file is returned correctly
         */
        VssCommandLineUtils.setScmConfDir( scmUserHome );
        assertEquals( vssInstallPath, VssCommandLineUtils.getSettings().getVssDirectory() );

        /*
         * Validate that setting is overridden by system property
         */
        System.setProperty( VSS_DIRECTORY_PROPERTY, vssInstallPathAlt );
        VssCommandLineUtils.setScmConfDir( new File( vssInstallPathAlt ) );
        assertEquals( vssInstallPathAlt, VssCommandLineUtils.getSettings().getVssDirectory() );

        /*
         * Delete the test settings file
         */
        testSettings.delete();

        /*
         * Validate that setting is still equal to system property
         */
        assertEquals( vssInstallPathAlt, VssCommandLineUtils.getSettings().getVssDirectory() );

        /*
         * Re-instate the original settings file, if one existed
         */
        if ( preExistingSettings )
        {
            File backup = new File( scmUserHome, backupFilename );
            backup.renameTo( new File( scmUserHome, settingsFilename ) );
        }

        if ( !preExistingScmFolder )
        {
            scmUserHome.delete();
        }

    }

    public void testGetSsDir()
    {
        final String vssInstallPathWindowsStyle = "c:\\vss\\bin";
        final String vssInstallPathUnixStyle = "c:/vss/bin";
        final String targetValue = "c:/vss/bin/";

        // Windows style test
        System.setProperty( VSS_DIRECTORY_PROPERTY, vssInstallPathWindowsStyle );
        VssCommandLineUtils.setScmConfDir( new File( vssInstallPathWindowsStyle ) );
        assertEquals( targetValue, VssCommandLineUtils.getSsDir() );

        // Unix style test
        System.setProperty( VSS_DIRECTORY_PROPERTY, vssInstallPathUnixStyle );
        VssCommandLineUtils.setScmConfDir( new File( vssInstallPathUnixStyle ) );
        assertEquals( targetValue, VssCommandLineUtils.getSsDir() );

        // Windows style with folder indicator
        System.setProperty( VSS_DIRECTORY_PROPERTY, vssInstallPathWindowsStyle + "\\" );
        VssCommandLineUtils.setScmConfDir( new File( vssInstallPathWindowsStyle ) );
        assertEquals( targetValue, VssCommandLineUtils.getSsDir() );

        // Unix style with folder indicator
        System.setProperty( VSS_DIRECTORY_PROPERTY, vssInstallPathUnixStyle + "/" );
        VssCommandLineUtils.setScmConfDir( new File( vssInstallPathUnixStyle ) );
        assertEquals( targetValue, VssCommandLineUtils.getSsDir() );

        // Unix style with Windows style folder indicator
        System.setProperty( VSS_DIRECTORY_PROPERTY, vssInstallPathUnixStyle + "\\" );
        VssCommandLineUtils.setScmConfDir( new File( vssInstallPathUnixStyle ) );
        assertEquals( targetValue, VssCommandLineUtils.getSsDir() );

    }
}
