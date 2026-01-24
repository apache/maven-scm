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
package org.apache.maven.scm.plugin;

import java.io.File;

import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getTestFile;
import static org.apache.maven.scm.ScmTestCase.checkSystemCmdPresence;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
@MojoTest
@Basedir("/mojos/export")
class ExportMojoTest {
    private File exportDir;

    private File repository;

    @BeforeEach
    void setUp() throws Exception {

        exportDir = getTestFile("target/export");

        repository = getTestFile("target/repository");

        FileUtils.forceDelete(exportDir);
    }

    @Test
    @InjectMojo(goal = "export", pom = "export.xml")
    void testExport(ExportMojo mojo) throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVNADMIN_COMMAND_LINE);

        SvnScmTestUtils.initializeRepository(repository);

        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        mojo.setExportDirectory(exportDir.getAbsoluteFile());

        mojo.execute();

        assertTrue(exportDir.listFiles().length > 0);
        assertFalse(new File(exportDir, ".svn").exists());
    }

    @Test
    @InjectMojo(goal = "export", pom = "exportWhenExportDirectoryExistsAndSkip.xml")
    void testSkipExportIfExists(ExportMojo mojo) throws Exception {
        exportDir.mkdirs();

        mojo.setExportDirectory(exportDir);

        mojo.execute();

        assertEquals(0, exportDir.listFiles().length);
    }

    @Test
    @InjectMojo(goal = "export", pom = "exportWithExcludesIncludes.xml")
    void testExcludeInclude(ExportMojo mojo) throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVNADMIN_COMMAND_LINE);

        SvnScmTestUtils.initializeRepository(repository);

        exportDir.mkdirs();

        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        mojo.setExportDirectory(exportDir);

        mojo.execute();

        assertTrue(exportDir.listFiles().length > 0);
        assertTrue(new File(exportDir, "pom.xml").exists());
        assertFalse(new File(exportDir, "readme.txt").exists());
        assertFalse(new File(exportDir, "src/test").exists());
    }
}
