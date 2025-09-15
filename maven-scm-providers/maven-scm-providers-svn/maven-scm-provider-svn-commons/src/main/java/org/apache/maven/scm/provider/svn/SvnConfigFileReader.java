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
package org.apache.maven.scm.provider.svn;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.codehaus.plexus.util.Os;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class SvnConfigFileReader {
    private File configDirectory;

    public File getConfigDirectory() {
        if (configDirectory == null) {
            if (Os.isFamily(Os.FAMILY_WINDOWS)) {
                configDirectory = new File(System.getenv("APPDATA"), "Subversion");
            } else {
                configDirectory = new File(System.getProperty("user.home"), ".subversion");
            }
        }

        return configDirectory;
    }

    public void setConfigDirectory(File configDirectory) {
        this.configDirectory = configDirectory;
    }

    public String getProperty(String group, String propertyName) {
        List<String> lines = getConfigLines();

        boolean inGroup = false;
        for (Iterator<String> i = lines.iterator(); i.hasNext(); ) {
            String line = i.next().trim();

            if (!inGroup) {
                if (("[" + group + "]").equals(line)) {
                    inGroup = true;
                }
            } else {
                if (line.startsWith("[") && line.endsWith("]")) {
                    // a new group start
                    return null;
                }

                if (line.startsWith("#")) {
                    continue;
                }
                if (line.indexOf('=') < 0) {
                    continue;
                }

                String property = line.substring(0, line.indexOf('=')).trim();

                if (!property.equals(propertyName)) {
                    continue;
                }

                String value = line.substring(line.indexOf('=') + 1);
                return value.trim();
            }
        }

        return null;
    }

    /**
     * Load the svn config file.
     *
     * @return the list of all non-comment, non-empty lines in the config file
     */
    private List<String> getConfigLines() {
        Path configPath = getConfigDirectory().toPath().resolve("config");
        if (Files.exists(configPath)) {
            try {
                return Files.lines(configPath, StandardCharsets.UTF_8)
                        .filter(line -> !line.isEmpty())
                        .filter(line -> !line.startsWith("#"))
                        .collect(Collectors.toCollection(ArrayList::new));
            } catch (UncheckedIOException | IOException e) {
                return new ArrayList<>();
            }
        }
        return new ArrayList<>();
    }
}
