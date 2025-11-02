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
import org.apache.maven.scm.PlexusJUnit4TestCase;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class AbstractJUnit4MojoTestCase extends AbstractMojoTestCase {
    private static final PlexusJUnit4TestCase PLEXUS_JUNIT4_TEST_CASE = new PlexusJUnit4TestCase();

    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        PLEXUS_JUNIT4_TEST_CASE.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        PLEXUS_JUNIT4_TEST_CASE.tearDown();
    }

    public static String getBasedir() {
        return PLEXUS_JUNIT4_TEST_CASE.getBasedir();
    }

    public static File getTestFile(final String path) {
        return PLEXUS_JUNIT4_TEST_CASE.getTestFile(getBasedir(), path);
    }
}
