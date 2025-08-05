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
package org.apache.maven.scm.provider.local.command.update;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.local.metadata.LocalScmMetadata;
import org.apache.maven.scm.provider.local.metadata.io.xpp3.LocalScmMetadataXpp3Reader;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.update.UpdateCommandTckTest;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class LocalUpdateCommandTckTest extends UpdateCommandTckTest {
    private static final String MODULE_NAME = "update-tck";

    public String getScmUrl() throws Exception {
        return "scm:local|" + getRepositoryRoot() + "|" + MODULE_NAME;
    }

    public void initRepo() throws Exception {
        makeRepo(getRepositoryRoot());
    }

    /**
     * Tests that a file that has been deleted from repository after checkout will be removed by scm-local. Local
     * additions must not be deleted.
     */
    @Test
    public void testDeletion() throws Exception {
        FileUtils.deleteDirectory(getUpdatingCopy());

        ScmRepository repository = makeScmRepository(getScmUrl());

        checkOut(getUpdatingCopy(), repository);

        // Check preconditions
        File readmeFileLocal = new File(getUpdatingCopy(), "readme.txt");
        assertTrue(readmeFileLocal.exists());
        File newFileLocal = new File(getUpdatingCopy(), "newfile.xml");
        assertFalse(newFileLocal.exists());

        // Delete readme.txt from repository
        File readmeFileRepo = new File(getRepositoryRoot(), MODULE_NAME + "/readme.txt");
        assertTrue(readmeFileRepo.exists());
        assertTrue("Could not delete", readmeFileRepo.delete());
        assertFalse(readmeFileRepo.exists());

        // Make local addition to updating copy - this one must not be touched
        ScmTestCase.makeFile(getUpdatingCopy(), "newfile.xml", "added newfile.xml locally");
        assertTrue(newFileLocal.exists());

        // ----------------------------------------------------------------------
        // Update the project
        // ----------------------------------------------------------------------

        ScmManager scmManager = getScmManager();
        Date lastUpdate = new Date(System.currentTimeMillis());
        Thread.sleep(1000);
        UpdateScmResult result = scmManager.update(repository, new ScmFileSet(getUpdatingCopy()), lastUpdate);

        assertNotNull("The command returned a null result.", result);

        assertResultIsSuccess(result);

        List<ScmFile> updatedFiles = result.getUpdatedFiles();

        assertEquals("Expected 1 files in the updated files list " + updatedFiles, 1, updatedFiles.size());

        // ----------------------------------------------------------------------
        // Assert the files in the updated files list
        // ----------------------------------------------------------------------

        Iterator<ScmFile> files = new TreeSet<ScmFile>(updatedFiles).iterator();

        // readme.txt
        ScmFile file = (ScmFile) files.next();
        assertPath("/readme.txt", file.getPath());
        assertTrue(file.getStatus().isUpdate());

        // ----------------------------------------------------------------------
        // Assert working directory contents
        // ----------------------------------------------------------------------

        // readme.txt
        assertFalse("Expected local copy of readme.txt to be deleted", readmeFileLocal.exists());

        // newfile.xml
        assertTrue("Expected local copy of newfile.xml NOT to be deleted", newFileLocal.exists());

        // ----------------------------------------------------------------------
        // Assert metadata file
        // ----------------------------------------------------------------------
        File metadataFile = new File(getUpdatingCopy(), ".maven-scm-local");
        assertTrue("Expected metadata file .maven-scm-local does not exist", metadataFile.exists());
        try (Reader reader = Files.newBufferedReader(metadataFile.toPath())) {
            LocalScmMetadata metadata = new LocalScmMetadataXpp3Reader().read(reader);
            File root = new File(getRepositoryRoot() + "/" + MODULE_NAME);
            @SuppressWarnings("unchecked")
            List<String> fileNames = FileUtils.getFileNames(root, "**", null, false);
            assertEquals(fileNames, metadata.getRepositoryFileNames());
        }
    }

    private void makeRepo(File workingDirectory) throws Exception {
        makeFile(workingDirectory, MODULE_NAME + "/pom.xml", "/pom.xml");

        makeFile(workingDirectory, MODULE_NAME + "/readme.txt", "/readme.txt");

        makeFile(workingDirectory, MODULE_NAME + "/src/main/java/Application.java", "/src/main/java/Application.java");

        makeFile(workingDirectory, MODULE_NAME + "/src/test/java/Test.java", "/src/test/java/Test.java");

        makeDirectory(workingDirectory, MODULE_NAME + "/src/test/resources");
    }
}
