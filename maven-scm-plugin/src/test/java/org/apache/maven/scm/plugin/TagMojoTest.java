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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
@MojoTest
@Basedir("/mojos/tag")
public class TagMojoTest {
    private File checkoutDir;

    @BeforeEach
    public void setUp() throws Exception {

        checkoutDir = getTestFile("target/checkout");

        FileUtils.forceDelete(checkoutDir);

        File repository = getTestFile("target/repository");

        FileUtils.forceDelete(repository);

        checkSystemCmdPresence(SvnScmTestUtils.SVNADMIN_COMMAND_LINE);

        SvnScmTestUtils.initializeRepository(repository);
    }

    @Test
    void testTag(
            @InjectMojo(goal = "tag", pom = "tag.xml") TagMojo mojo,
            @InjectMojo(goal = "checkout", pom = "../checkout/checkoutWithConnectionUrl.xml")
                    CheckoutMojo checkoutMojoInit,
            @InjectMojo(goal = "checkout", pom = "checkout.xml") CheckoutMojo checkoutMojo)
            throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        checkoutMojoInit.execute();

        mojo.setWorkingDirectory(checkoutDir);

        mojo.execute();

        File tagCheckoutDir = getTestFile("target/tags/mytag");
        if (tagCheckoutDir.exists()) {
            FileUtils.deleteDirectory(tagCheckoutDir);
        }

        assertFalse(new File(tagCheckoutDir, "pom.xml").exists());
        checkoutMojo.execute();
        assertTrue(new File(tagCheckoutDir, "pom.xml").exists());
    }

    @Test
    void testTagWithTimestamp(
            @InjectMojo(goal = "tag", pom = "tag.xml") TagMojo mojo,
            @InjectMojo(goal = "checkout", pom = "../checkout/checkoutWithConnectionUrl.xml")
                    CheckoutMojo checkoutMojoInit)
            throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        checkoutMojoInit.execute();

        mojo.setWorkingDirectory(checkoutDir);

        mojo.execute();
    }
}
