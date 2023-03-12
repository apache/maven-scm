/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.provider.svn.util;

import java.io.File;
import java.io.IOException;

import org.apache.maven.scm.providers.svn.settings.Settings;
import org.apache.maven.scm.providers.svn.settings.io.xpp3.SvnXpp3Reader;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class SvnUtil {
    protected static final String SVN_SETTINGS_FILENAME = "svn-settings.xml";

    public static final File DEFAULT_SETTINGS_DIRECTORY = new File(System.getProperty("user.home"), ".scm");

    private static File settingsDirectory = DEFAULT_SETTINGS_DIRECTORY;

    private static Settings settings;

    private SvnUtil() {}

    public static Settings getSettings() {
        if (settings == null) {
            settings = readSettings();
        }
        return settings;
    }

    public static Settings readSettings() {
        File settingsFile = getSettingsFile();

        if (settingsFile.exists()) {
            SvnXpp3Reader reader = new SvnXpp3Reader();
            try {
                return reader.read(ReaderFactory.newXmlReader(settingsFile));
            } catch (IOException e) {
                // Nothing to do
            } catch (XmlPullParserException e) {
                String message = settingsFile.getAbsolutePath() + " isn't well formed. SKIPPED." + e.getMessage();

                System.err.println(message);
            }
        }

        return new Settings();
    }

    public static void setSettingsDirectory(File directory) {
        settingsDirectory = directory;
        settings = readSettings();
    }

    public static File getSettingsFile() {
        return new File(settingsDirectory, SVN_SETTINGS_FILENAME);
    }
}
