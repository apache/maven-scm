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
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getTestFile;
import static org.apache.maven.scm.ScmTestCase.checkSystemCmdPresence;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.AssertionsKt.assertNotNull;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
@MojoTest
@Basedir("/mojos/checkout")
class CheckoutMojoTest {
    private File checkoutDir;

    private File repository;

    @BeforeEach
    void setUp() throws Exception {
        checkoutDir = getTestFile("target/checkout");

        repository = getTestFile("target/repository");

        FileUtils.forceDelete(checkoutDir);
    }

    @Test
    @InjectMojo(goal = "checkout", pom = "checkoutWhenCheckoutDirectoryExistsAndSkip.xml")
    void testSkipCheckoutWhenCheckoutDirectoryExistsAndSkip(CheckoutMojo mojo) throws Exception {
        FileUtils.forceDelete(checkoutDir);
        checkoutDir.mkdirs();

        mojo.setCheckoutDirectory(checkoutDir);

        mojo.execute();

        assertEquals(0, checkoutDir.listFiles().length);
    }

    @Test
    @InjectMojo(goal = "checkout", pom = "checkoutWithConnectionUrl.xml")
    void testSkipCheckoutWithConnectionUrl(CheckoutMojo mojo) throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVNADMIN_COMMAND_LINE);

        SvnScmTestUtils.initializeRepository(repository);

        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        mojo.setCheckoutDirectory(checkoutDir);

        mojo.execute();
    }

    @Test
    @InjectMojo(goal = "checkout", pom = "checkoutWithoutConnectionUrl.xml")
    void testSkipCheckoutWithoutConnectionUrl(CheckoutMojo mojo) throws Exception {
        FileUtils.forceDelete(checkoutDir);

        checkoutDir.mkdirs();

        try {
            mojo.execute();

            fail("mojo execution must fail.");
        } catch (MojoExecutionException e) {
            assertNotNull(e.getMessage());
        }
    }

    @Test
    @InjectMojo(goal = "checkout", pom = "checkoutUsingExport.xml")
    void testUseExport(CheckoutMojo mojo) throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        FileUtils.forceDelete(checkoutDir);

        checkoutDir.mkdirs();

        mojo.setCheckoutDirectory(checkoutDir);

        mojo.execute();

        assertTrue(checkoutDir.listFiles().length > 0);
        assertFalse(new File(checkoutDir, ".svn").exists());
    }

    @Test
    @InjectMojo(goal = "checkout", pom = "checkoutWithExcludesIncludes.xml")
    void testExcludeInclude(CheckoutMojo mojo) throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVNADMIN_COMMAND_LINE);

        FileUtils.forceDelete(checkoutDir);

        checkoutDir.mkdirs();

        SvnScmTestUtils.initializeRepository(repository);

        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        mojo.setCheckoutDirectory(checkoutDir);

        mojo.execute();

        assertTrue(checkoutDir.listFiles().length > 0);
        assertTrue(new File(checkoutDir, ".svn").exists());
        assertTrue(new File(checkoutDir, "pom.xml").exists());
        assertFalse(new File(checkoutDir, "readme.txt").exists());
        assertFalse(new File(checkoutDir, "src/test").exists());
        assertTrue(new File(checkoutDir, "src/main/java").exists());
    }

    @Test
    @InjectMojo(goal = "checkout", pom = "checkoutEncryptedPasswordFromSettings.xml")
    void testEncryptedPasswordFromSettings(CheckoutMojo mojo) throws Exception {
        ScmProviderRepositoryWithHost repo =
                (ScmProviderRepositoryWithHost) mojo.getScmRepository().getProviderRepository();

        assertNotEquals(
                "{Ael0S2tnXv8H3X+gHKpZAvAA25D8+gmU2w2RrGaf5v8=}",
                repo.getPassword(),
                "Raw encrypted Password was returned instead of the decrypted plaintext version");

        assertNotEquals(
                "{7zK9P8hNVeUHbTsjiA/vnOs0zUXbND+9MBNPvdvl+x4=}",
                repo.getPassphrase(),
                "Raw encrypted Passphrase was returned instead of the decrypted plaintext version");

        assertEquals("testuser", repo.getUser());
        assertEquals("testpass", repo.getPassword());
        assertEquals("testphrase", repo.getPassphrase());
    }
}
