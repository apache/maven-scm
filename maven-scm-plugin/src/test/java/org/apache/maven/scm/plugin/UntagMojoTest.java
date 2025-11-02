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

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.codehaus.plexus.util.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.apache.maven.scm.ScmTestCase.checkSystemCmdPresence;

@RunWith(JUnit4.class)
public class UntagMojoTest extends AbstractJUnit4MojoTestCase {
    File checkoutDir;

    File repository;

    @Before
    public void setUp() throws Exception {
        super.setUp();

        checkoutDir = getTestFile("target/checkout");

        repository = getTestFile("target/repository");

        checkSystemCmdPresence(GitScmTestUtils.GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/git", repository, checkoutDir);

        CheckoutMojo checkoutMojo =
                (CheckoutMojo) lookupMojo("checkout", getTestFile("src/test/resources/mojos/untag/checkout.xml"));
        checkoutMojo.setWorkingDirectory(checkoutDir);

        String connectionUrl = checkoutMojo.getConnectionUrl();
        connectionUrl = StringUtils.replace(connectionUrl, "${basedir}", getBasedir());
        connectionUrl = StringUtils.replace(connectionUrl, "\\", "/");
        checkoutMojo.setConnectionUrl(connectionUrl);

        checkoutMojo.setCheckoutDirectory(checkoutDir);

        checkoutMojo.execute();

        // Add a default user to the config
        GitScmTestUtils.setDefaultGitConfig(checkoutDir);
    }

    @Test
    public void testUntag() throws Exception {
        checkSystemCmdPresence(GitScmTestUtils.GIT_COMMAND_LINE);

        TagMojo tagMojo = (TagMojo) lookupMojo("tag", getTestFile("src/test/resources/mojos/untag/tag.xml"));
        tagMojo.setWorkingDirectory(checkoutDir);
        tagMojo.setConnectionUrl(getConnectionLocalAddress(tagMojo));
        tagMojo.execute();

        CheckoutMojo checkoutMojo =
                (CheckoutMojo) lookupMojo("checkout", getTestFile("src/test/resources/mojos/untag/checkout-tag.xml"));
        checkoutMojo.setWorkingDirectory(new File(getBasedir()));
        checkoutMojo.setConnectionUrl(getConnectionLocalAddress(checkoutMojo));

        File tagCheckoutDir = getTestFile("target/tags/mytag");

        if (tagCheckoutDir.exists()) {
            FileUtils.deleteDirectory(tagCheckoutDir);
        }

        checkoutMojo.setCheckoutDirectory(tagCheckoutDir);
        checkoutMojo.execute();

        UntagMojo mojo = (UntagMojo) lookupMojo("untag", getTestFile("src/test/resources/mojos/untag/untag.xml"));
        mojo.setWorkingDirectory(checkoutDir);
        mojo.setConnectionUrl(getConnectionLocalAddress(mojo));

        mojo.execute();

        FileUtils.deleteDirectory(tagCheckoutDir);

        try {
            checkoutMojo.execute();

            fail("mojo execution must fail.");
        } catch (MojoExecutionException e) {
            assertNotNull(e.getMessage());
        }
    }

    private String getConnectionLocalAddress(AbstractScmMojo mojo) {
        String connectionUrl = mojo.getConnectionUrl();
        connectionUrl = StringUtils.replace(connectionUrl, "${basedir}", getBasedir());
        connectionUrl = StringUtils.replace(connectionUrl, "\\", "/");
        return connectionUrl;
    }
}
