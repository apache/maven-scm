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
package org.apache.maven.scm.provider.local.repository;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class LocalRepositoryTest extends ScmTestCase {
    @BeforeEach
    void setUp() throws Exception {
        FileUtils.mkdir(getWorkingDirectory().getAbsolutePath());
    }

    @Test
    void testExistingRepository() throws Exception {
        ScmRepository repository = getScmManager().makeScmRepository("scm:local:src/test/repository:test-repo");

        assertNotNull(repository);

        assertEquals("local", repository.getProvider());

        ScmProviderRepository providerRepository = repository.getProviderRepository();

        assertNotNull(providerRepository);

        assertTrue(providerRepository instanceof LocalScmProviderRepository);

        LocalScmProviderRepository local = (LocalScmProviderRepository) providerRepository;

        assertEquals(getTestFile("src/test/repository").getAbsolutePath(), local.getRoot());

        assertEquals("test-repo", local.getModule());
    }

    @Test
    void testMissingRepositoryRoot() throws Exception {
        try {
            getScmManager().makeScmRepository("scm:local:");

            fail("Expected ScmRepositoryException.");
        } catch (ScmRepositoryException ex) {
            // expected
        }
    }

    @Test
    void testNonExistingMissingRepositoryRoot() throws Exception {
        try {
            getScmManager().makeScmRepository("scm:local:non-existing-directory:module");

            fail("Expected ScmRepositoryException.");
        } catch (ScmRepositoryException ex) {
            // expected
        }
    }

    @Test
    void testMissingModule() throws Exception {
        try {
            getScmManager().makeScmRepository("scm:local:src/test/repository");

            fail("Expected ScmRepositoryException.");
        } catch (ScmRepositoryException ex) {
            // expected
        }

        try {
            getScmManager().makeScmRepository("scm:local:src/test/repository:");

            fail("Expected ScmRepositoryException.");
        } catch (ScmRepositoryException ex) {
            // expected
        }
    }

    @Test
    void testNonExistingModule() throws Exception {
        try {
            getScmManager().makeScmRepository("scm:local:src/test/repository:non-existing-module");

            fail("Expected ScmRepositoryException.");
        } catch (ScmRepositoryException ex) {
            // expected
        }
    }
}
