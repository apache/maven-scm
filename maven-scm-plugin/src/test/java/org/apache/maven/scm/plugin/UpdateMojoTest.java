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

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
@MojoTest
@Basedir("/mojos/update")
class UpdateMojoTest {

    private File repository;

    @BeforeEach
    void setUp() throws Exception {

        File checkoutDir = getTestFile("target/checkout");

        repository = getTestFile("target/repository");

        FileUtils.forceDelete(checkoutDir);
    }

    @Test
    void testSkipCheckoutWithConnectionUrl(
            @InjectMojo(goal = "update", pom = "updateWithConnectionUrl.xml") UpdateMojo updateMojo,
            @InjectMojo(goal = "checkout", pom = "../checkout/checkoutWithConnectionUrl.xml") CheckoutMojo checkoutMojo)
            throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVNADMIN_COMMAND_LINE);

        SvnScmTestUtils.initializeRepository(repository);

        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        checkoutMojo.execute();

        updateMojo.execute();
    }
}
