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
package org.apache.maven.scm.provider.local.command.checkout;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.local.metadata.LocalScmMetadata;
import org.apache.maven.scm.provider.local.metadata.io.xpp3.LocalScmMetadataXpp3Reader;
import org.apache.maven.scm.tck.command.checkout.CheckOutCommandTckTest;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class LocalCheckOutCommandTckTest extends CheckOutCommandTckTest {
    private String module = "check-out";

    public String getScmUrl() throws Exception {
        return "scm:local|" + getRepositoryRoot().getAbsolutePath() + "|" + module;
    }

    public void initRepo() throws Exception {
        File root = new File(getRepositoryRoot() + "/" + module);

        makeFile(root, "/pom.xml");

        makeFile(root, "/readme.txt");

        makeFile(root, "/src/main/java/Application.java");

        makeFile(root, "/src/test/java/Test.java");

        makeDirectory(root, "/src/test/resources");
    }

    /**
     * Tests that the metadata file .maven-scm-local is written correctly
     */
    @Test
    public void testMetadata() throws Exception {
        FileUtils.deleteDirectory(getWorkingCopy());

        CheckOutScmResult result = checkOut(getWorkingCopy(), getScmRepository());

        assertResultIsSuccess(result);

        List<ScmFile> checkedOutFiles = result.getCheckedOutFiles();

        assertEquals(4, checkedOutFiles.size());

        // ----------------------------------------------------------------------
        // Assert metadata file
        // ----------------------------------------------------------------------
        File metadataFile = new File(getWorkingCopy(), ".maven-scm-local");
        assertTrue("Expected metadata file .maven-scm-local does not exist", metadataFile.exists());

        try (Reader reader = Files.newBufferedReader(metadataFile.toPath()); ) {
            LocalScmMetadata metadata = new LocalScmMetadataXpp3Reader().read(reader);
            File root = new File(getRepositoryRoot() + "/" + module);
            @SuppressWarnings("unchecked")
            List<String> fileNames = FileUtils.getFileNames(root, "**", null, false);
            assertEquals(fileNames, metadata.getRepositoryFileNames());
        }
    }
}
