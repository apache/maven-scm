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

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.codehaus.plexus.util.FileUtils;

/**
 * Unit Test for BootstrapMojo
 *
 * @author <a href="mailto:arne@degenring.com">Arne Degenring</a>
 *
 */
public class BootstrapMojoTest extends AbstractMojoTestCase {
    File checkoutDir;

    File projectDir;

    File goalDir;

    BootstrapMojo bootstrapMojo;

    protected void setUp() throws Exception {
        super.setUp();

        checkoutDir = getTestFile("target/checkout");
        FileUtils.forceDelete(checkoutDir);
        checkoutDir.mkdirs();

        projectDir = getTestFile("target/checkout/my/project");
        projectDir.mkdirs();

        goalDir = getTestFile("target/checkout/my/project/modules/1");
        goalDir.mkdirs();

        bootstrapMojo = new BootstrapMojo();
    }

    public void testDetermineWorkingDirectoryPath() throws Exception {
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
