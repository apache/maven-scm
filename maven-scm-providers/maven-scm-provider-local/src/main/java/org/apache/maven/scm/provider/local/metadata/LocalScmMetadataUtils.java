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
package org.apache.maven.scm.provider.local.metadata;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.List;

import org.apache.maven.scm.provider.local.metadata.io.xpp3.LocalScmMetadataXpp3Reader;
import org.apache.maven.scm.provider.local.metadata.io.xpp3.LocalScmMetadataXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.ReaderFactory;
import org.codehaus.plexus.util.WriterFactory;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utils for dealing with LocalScmMetadata.
 *
 * @author <a href="mailto:arne@degenring.de">Arne Degenring</a>
 */
public class LocalScmMetadataUtils {
    /**
     * The name of the metadata file.
     */
    public static final String FILENAME = ".maven-scm-local";

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalScmMetadataUtils.class);

    /**
     * Builds LocalScmMetadata based on contents of repository.
     *
     * @param repository
     * @return TODO
     * @throws IOException if any
     */
    public LocalScmMetadata buildMetadata(File repository) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> repoFilenames = FileUtils.getFileNames(repository.getAbsoluteFile(), "**", null, false);
        LocalScmMetadata metadata = new LocalScmMetadata();
        metadata.setRepositoryFileNames(repoFilenames);
        return metadata;
    }

    /**
     * Writes metadata file.
     *
     * @param destinationDir
     * @param metadata
     * @throws IOException if any
     */
    public void writeMetadata(File destinationDir, LocalScmMetadata metadata) throws IOException {
        File metadataFile = new File(destinationDir, FILENAME);
        metadataFile.createNewFile();

        try (Writer writer = WriterFactory.newXmlWriter(metadataFile)) {
            new LocalScmMetadataXpp3Writer().write(writer, metadata);
        }
    }

    /**
     * Reads metadata file from given directory.
     *
     * @param dir the directory that should contain the metadata file
     * @return LocalScmMetadata or <code>null</code> in case of problems
     */
    public LocalScmMetadata readMetadata(File dir) {
        File metadataFile = new File(dir, FILENAME);
        if (!metadataFile.exists()) {
            return null;
        }

        try (Reader reader = ReaderFactory.newXmlReader(metadataFile)) {
            LocalScmMetadata result = new LocalScmMetadataXpp3Reader().read(reader);
            return result;
        } catch (XmlPullParserException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not interpret .maven-scm-local - ignoring", e);
            }
            return null;
        } catch (IOException e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Could not read .maven-scm-local - ignoring", e);
            }
            return null;
        }
    }
}
