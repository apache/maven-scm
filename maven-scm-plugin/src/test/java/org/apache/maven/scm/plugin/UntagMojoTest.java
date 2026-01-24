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
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.codehaus.plexus.util.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getTestFile;
import static org.apache.maven.scm.ScmTestCase.checkSystemCmdPresence;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@MojoTest
@Basedir("/mojos/untag")
class UntagMojoTest {
    private File checkoutDir;

    @BeforeEach
    void setUp() throws Exception {
        //        super.setUp();

        checkoutDir = getTestFile("target/checkout");

        File repository = getTestFile("target/repository");

        checkSystemCmdPresence(GitScmTestUtils.GIT_COMMAND_LINE);

        GitScmTestUtils.initRepo("src/test/resources/git", repository, checkoutDir);
    }

    @Test
    void testUntag(
            @InjectMojo(goal = "tag", pom = "tag.xml") TagMojo tagMojo,
            @InjectMojo(goal = "untag", pom = "untag.xml") UntagMojo untagMojo,
            @InjectMojo(goal = "checkout", pom = "checkout.xml") CheckoutMojo checkoutMojoInit,
            @InjectMojo(goal = "checkout", pom = "checkout-tag.xml") CheckoutMojo checkoutMojo)
            throws Exception {
        checkSystemCmdPresence(GitScmTestUtils.GIT_COMMAND_LINE);

        checkoutMojoInit.execute();
        // Add a default user to the config
        GitScmTestUtils.setDefaultGitConfig(checkoutDir);

        tagMojo.execute();

        File tagCheckoutDir = getTestFile("target/tags/mytag");

        if (tagCheckoutDir.exists()) {
            FileUtils.deleteDirectory(tagCheckoutDir);
        }

        checkoutMojo.execute();
        untagMojo.execute();

        FileUtils.deleteDirectory(tagCheckoutDir);

        try {
            checkoutMojo.execute();

            fail("mojo execution must fail.");
        } catch (MojoExecutionException e) {
            assertNotNull(e.getMessage());
        }
    }
}
