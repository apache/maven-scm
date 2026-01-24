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

import javax.inject.Inject;

import java.io.File;

import org.apache.maven.api.plugin.testing.Basedir;
import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.apache.maven.api.plugin.testing.MojoExtension.getTestFile;
import static org.apache.maven.scm.ScmTestCase.checkSystemCmdPresence;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
@MojoTest
@Basedir("/mojos/changelog")
class ChangeLogMojoTest {

    @Inject
    private Log log;

    @BeforeEach
    void setUp() throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVNADMIN_COMMAND_LINE);

        File repository = getTestFile("target/repository");
        SvnScmTestUtils.initializeRepository(repository);
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "changelog.xml")
    void testChangeLog(ChangeLogMojo mojo) throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        mojo.execute();
        // verify log messages as result of mojo execution
        verify(log, times(8)).debug(anyString());
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "changelogWithParameters.xml")
    void testChangeLogWithParameters(ChangeLogMojo mojo) throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        mojo.execute();
        // verify log messages as result of mojo execution
        verify(log, atMost(7)).debug(anyString());
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "changelogWithBadUserDateFormat.xml")
    void testChangeLogWithBadUserDateFormat(ChangeLogMojo mojo) throws Exception {
        MojoExecutionException exception = assertThrows(MojoExecutionException.class, mojo::execute);
        assertTrue(exception.getMessage().contains("Please use this date pattern: yyyyMMdd"));
    }

    @Test
    @InjectMojo(goal = "changelog", pom = "changelogWithBadConnectionUrl.xml")
    void testChangeLogWithBadConnectionUrl(ChangeLogMojo mojo) throws Exception {
        checkSystemCmdPresence(SvnScmTestUtils.SVN_COMMAND_LINE);

        MojoExecutionException exception = assertThrows(MojoExecutionException.class, mojo::execute);
        assertTrue(exception.getMessage().contains("Command failed: The svn command failed."));
    }
}
