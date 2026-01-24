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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit Test for BootstrapMojo
 *
 * @author <a href="mailto:arne@degenring.com">Arne Degenring</a>
 *
 */
class BootstrapMojoTest {

    @TempDir
    private File tempDir;

    private File checkoutDir;

    private File projectDir;

    private File goalDir;

    private BootstrapMojo bootstrapMojo;

    @BeforeEach
    void setUp() throws Exception {

        checkoutDir = new File(tempDir, "target/checkout");
        checkoutDir.mkdirs();

        projectDir = new File(checkoutDir, "my/project");
        projectDir.mkdirs();

        goalDir = new File(checkoutDir, "my/project/modules/1");
        goalDir.mkdirs();

        bootstrapMojo = new BootstrapMojo(null, null);
    }

    @Test
    void testDetermineWorkingDirectoryPath() throws Exception {
        // only checkout dir
        assertEquals(checkoutDir.getPath(), bootstrapMojo.determineWorkingDirectoryPath(checkoutDir, "", ""));
        assertEquals(checkoutDir.getPath(), bootstrapMojo.determineWorkingDirectoryPath(checkoutDir, null, null));

        // checkout dir and goal dir
        assertEquals(projectDir.getPath(), bootstrapMojo.determineWorkingDirectoryPath(checkoutDir, "", "my/project"));

        // checkout dir and relative path project dir
        assertEquals(
                projectDir.getPath(), bootstrapMojo.determineWorkingDirectoryPath(checkoutDir, "my/project", null));
        assertEquals(
                projectDir.getPath(), bootstrapMojo.determineWorkingDirectoryPath(checkoutDir, "my/project/", null));
        assertEquals(
                projectDir.getPath(),
                bootstrapMojo.determineWorkingDirectoryPath(checkoutDir, "my" + File.separator + "project", null));

        // checkout dir, relative path project dir and goal dir have been set
        assertEquals(
                goalDir.getPath(), bootstrapMojo.determineWorkingDirectoryPath(checkoutDir, "my/project", "modules/1"));
        assertEquals(
                goalDir.getPath(),
                bootstrapMojo.determineWorkingDirectoryPath(checkoutDir, "my/project/", "modules/1/"));
        assertEquals(
                goalDir.getPath(),
                bootstrapMojo.determineWorkingDirectoryPath(
                        checkoutDir, "my" + File.separator + "project", "modules" + File.separator + "1"));
    }
}
