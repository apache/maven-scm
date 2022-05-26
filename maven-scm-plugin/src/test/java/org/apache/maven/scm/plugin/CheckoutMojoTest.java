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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.apache.maven.scm.ScmTestCase.checkScmPresence;
import static org.junit.Assert.assertNotEquals;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
@RunWith(JUnit4.class)
public class CheckoutMojoTest extends AbstractJUnit4MojoTestCase {
    File checkoutDir;

    File repository;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        checkoutDir = getTestFile("target/checkout");

        repository = getTestFile("target/repository");

        FileUtils.forceDelete(checkoutDir);
    }

    @Test
    public void testSkipCheckoutWhenCheckoutDirectoryExistsAndSkip() throws Exception {
        FileUtils.forceDelete(checkoutDir);
        checkoutDir.mkdirs();

        CheckoutMojo mojo = (CheckoutMojo) lookupMojo(
                "checkout",
                getTestFile("src/test/resources/mojos/checkout/checkoutWhenCheckoutDirectoryExistsAndSkip.xml"));

        mojo.setCheckoutDirectory(checkoutDir);

        mojo.execute();

        assertEquals(0, checkoutDir.listFiles().length);
    }

    @Test
    public void testSkipCheckoutWithConnectionUrl() throws Exception {
        checkScmPresence(SvnScmTestUtils.SVNADMIN_COMMAND_LINE);

        FileUtils.forceDelete(checkoutDir);

        SvnScmTestUtils.initializeRepository(repository);

        checkScmPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        CheckoutMojo mojo = (CheckoutMojo)
                lookupMojo("checkout", getTestFile("src/test/resources/mojos/checkout/checkoutWithConnectionUrl.xml"));
        mojo.setWorkingDirectory(new File(getBasedir()));

        String connectionUrl = mojo.getConnectionUrl();
        connectionUrl = StringUtils.replace(connectionUrl, "${basedir}", getBasedir());
        connectionUrl = StringUtils.replace(connectionUrl, "\\", "/");
        mojo.setConnectionUrl(connectionUrl);

        mojo.setCheckoutDirectory(checkoutDir);

        mojo.execute();
    }

    @Test
    public void testSkipCheckoutWithoutConnectionUrl() throws Exception {
        FileUtils.forceDelete(checkoutDir);

        checkoutDir.mkdirs();
        CheckoutMojo mojo = (CheckoutMojo) lookupMojo(
                "checkout", getTestFile("src/test/resources/mojos/checkout/checkoutWithoutConnectionUrl.xml"));

        try {
            mojo.execute();

            fail("mojo execution must fail.");
        } catch (MojoExecutionException e) {
            assertTrue(true);
        }
    }

    @Test
    public void testUseExport() throws Exception {
        checkScmPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        FileUtils.forceDelete(checkoutDir);

        checkoutDir.mkdirs();

        CheckoutMojo mojo = (CheckoutMojo)
                lookupMojo("checkout", getTestFile("src/test/resources/mojos/checkout/checkoutUsingExport.xml"));

        mojo.setCheckoutDirectory(checkoutDir);

        mojo.execute();

        assertTrue(checkoutDir.listFiles().length > 0);
        assertFalse(new File(checkoutDir, ".svn").exists());
    }

    @Test
    public void testExcludeInclude() throws Exception {
        checkScmPresence(SvnScmTestUtils.SVNADMIN_COMMAND_LINE);

        FileUtils.forceDelete(checkoutDir);

        checkoutDir.mkdirs();

        SvnScmTestUtils.initializeRepository(repository);

        checkScmPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        CheckoutMojo mojo = (CheckoutMojo) lookupMojo(
                "checkout", getTestFile("src/test/resources/mojos/checkout/checkoutWithExcludesIncludes.xml"));

        mojo.setCheckoutDirectory(checkoutDir);

        mojo.execute();

        assertTrue(checkoutDir.listFiles().length > 0);
        assertTrue(new File(checkoutDir, ".svn").exists());
        assertTrue(new File(checkoutDir, "pom.xml").exists());
        assertFalse(new File(checkoutDir, "readme.txt").exists());
        assertFalse(new File(checkoutDir, "src/test").exists());
        assertTrue(new File(checkoutDir, "src/main/java").exists());
        // olamy those files not exists anymore with svn 1.7
        // assertTrue( new File( checkoutDir, "src/main/java/.svn" ).exists() );
        // assertTrue( new File( checkoutDir, "src/main/.svn" ).exists() );
    }

    @Test
    public void testEncryptedPasswordFromSettings() throws Exception {
        File pom = getTestFile("src/test/resources/mojos/checkout/checkoutEncryptedPasswordFromSettings.xml");
        CheckoutMojo mojo = (CheckoutMojo) lookupMojo("checkout", pom);
        ScmProviderRepositoryWithHost repo =
                (ScmProviderRepositoryWithHost) mojo.getScmRepository().getProviderRepository();

        assertNotEquals(
                "Raw encrypted Password was returned instead of the decrypted plaintext version",
                "{Ael0S2tnXv8H3X+gHKpZAvAA25D8+gmU2w2RrGaf5v8=}",
                repo.getPassword());

        assertNotEquals(
                "Raw encrypted Passphrase was returned instead of the decrypted plaintext version",
                "{7zK9P8hNVeUHbTsjiA/vnOs0zUXbND+9MBNPvdvl+x4=}",
                repo.getPassphrase());

        assertEquals("testuser", repo.getUser());
        assertEquals("testpass", repo.getPassword());
        assertEquals("testphrase", repo.getPassphrase());
    }
}
