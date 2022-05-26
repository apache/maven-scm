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

import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.apache.maven.scm.ScmTestCase.checkScmPresence;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
@RunWith(JUnit4.class)
public class TagMojoTest extends AbstractJUnit4MojoTestCase {
    File checkoutDir;

    File repository;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        checkoutDir = getTestFile("target/checkout");

        FileUtils.forceDelete(checkoutDir);

        repository = getTestFile("target/repository");

        FileUtils.forceDelete(repository);

        checkScmPresence(SvnScmTestUtils.SVNADMIN_COMMAND_LINE);

        SvnScmTestUtils.initializeRepository(repository);

        checkScmPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        CheckoutMojo checkoutMojo = (CheckoutMojo)
                lookupMojo("checkout", getTestFile("src/test/resources/mojos/checkout/checkoutWithConnectionUrl.xml"));
        checkoutMojo.setWorkingDirectory(new File(getBasedir()));

        setupConnectionUrl(checkoutMojo);

        checkoutMojo.setCheckoutDirectory(checkoutDir);

        checkoutMojo.execute();
    }

    private static void setupConnectionUrl(AbstractScmMojo mojo) {
        String connectionUrl = mojo.getConnectionUrl();
        connectionUrl = connectionUrl.replace("${basedir}", getBasedir());
        connectionUrl = connectionUrl.replace('\\', '/');
        mojo.setConnectionUrl(connectionUrl);
    }

    @Test
    public void testTag() throws Exception {
        checkScmPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        TagMojo mojo = (TagMojo) lookupMojo("tag", getTestFile("src/test/resources/mojos/tag/tag.xml"));
        mojo.setWorkingDirectory(checkoutDir);

        setupConnectionUrl(mojo);

        mojo.execute();

        CheckoutMojo checkoutMojo =
                (CheckoutMojo) lookupMojo("checkout", getTestFile("src/test/resources/mojos/tag/checkout.xml"));
        checkoutMojo.setWorkingDirectory(new File(getBasedir()));

        setupConnectionUrl(checkoutMojo);

        File tagCheckoutDir = getTestFile("target/tags/mytag");
        if (tagCheckoutDir.exists()) {
            FileUtils.deleteDirectory(tagCheckoutDir);
        }
        checkoutMojo.setCheckoutDirectory(tagCheckoutDir);

        assertFalse(new File(tagCheckoutDir, "pom.xml").exists());
        checkoutMojo.execute();
        assertTrue(new File(tagCheckoutDir, "pom.xml").exists());
    }

    @Test
    public void testTagWithTimestamp() throws Exception {
        checkScmPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        TagMojo mojo = (TagMojo) lookupMojo("tag", getTestFile("src/test/resources/mojos/tag/tagWithTimestamp.xml"));
        mojo.setWorkingDirectory(checkoutDir);

        setupConnectionUrl(mojo);

        mojo.execute();
    }
}
