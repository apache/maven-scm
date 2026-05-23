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
package org.apache.maven.scm.provider.svn.svnexe.command;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class SvnCommandLineUtilsTest extends ScmTestCase {
    @Test
    void testCryptPassword() throws Exception {
        /* FIXME Plexus does not quote the crypted password on Windows which is actually incorrect at the moment
         * it would cause wildcard expansion with cmd: https://github.com/codehaus-plexus/plexus-utils/issues/37.
         */
        SvnScmProviderRepository repo = new SvnScmProviderRepository(
                "https://svn.apache.org/repos/asf/maven/scm/trunk", "username", "password");
        String clString = SvnCommandLineUtils.cryptPassword(
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));
        Commandline expectedCmd =
                new Commandline("svn --username username --password ***** --no-auth-cache --non-interactive");
        expectedCmd.setWorkingDirectory(new File(".").getAbsolutePath());
        assertEquals(expectedCmd.toString(), clString);

        repo = new SvnScmProviderRepository("https://svn.apache.org/repos/asf/maven/scm/trunk", "username", null);
        clString = SvnCommandLineUtils.cryptPassword(
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));
        assertCommandLine(
                "svn --username username --no-auth-cache --non-interactive",
                new File("."),
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));

        repo = new SvnScmProviderRepository(
                "https://svn.apache.org/repos/asf/maven/scm/trunk", "username", "password with spaces");
        clString = SvnCommandLineUtils.cryptPassword(
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));
        expectedCmd = new Commandline("svn --username username --password ***** --no-auth-cache --non-interactive");
        expectedCmd.setWorkingDirectory(new File(".").getAbsolutePath());
        assertEquals(expectedCmd.toString(), clString);

        repo = new SvnScmProviderRepository(
                "https://svn.apache.org/repos/asf/maven/scm/trunk", "username", "password'with'single'quotes");
        clString = SvnCommandLineUtils.cryptPassword(
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));
        expectedCmd = new Commandline("svn --username username --password ***** --no-auth-cache --non-interactive");
        expectedCmd.setWorkingDirectory(new File(".").getAbsolutePath());
        assertEquals(expectedCmd.toString(), clString);

        repo = new SvnScmProviderRepository(
                "https://svn.apache.org/repos/asf/maven/scm/trunk",
                "username",
                "password'with'single'quotes and spaces");
        clString = SvnCommandLineUtils.cryptPassword(
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));
        expectedCmd = new Commandline("svn --username username --password ***** --no-auth-cache --non-interactive");
        expectedCmd.setWorkingDirectory(new File(".").getAbsolutePath());
        assertEquals(expectedCmd.toString(), clString);

        repo = new SvnScmProviderRepository(
                "https://svn.apache.org/repos/asf/maven/scm/trunk", "username", "password\"with\"double\"quotes");
        clString = SvnCommandLineUtils.cryptPassword(
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));
        expectedCmd = new Commandline("svn --username username --password ***** --no-auth-cache --non-interactive");
        expectedCmd.setWorkingDirectory(new File(".").getAbsolutePath());
        assertEquals(expectedCmd.toString(), clString);

        repo = new SvnScmProviderRepository(
                "https://svn.apache.org/repos/asf/maven/scm/trunk",
                "username",
                "password\"with\"double\"quotes and spaces");
        clString = SvnCommandLineUtils.cryptPassword(
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));
        expectedCmd = new Commandline("svn --username username --password ***** --no-auth-cache --non-interactive");
        expectedCmd.setWorkingDirectory(new File(".").getAbsolutePath());
        // FIXME https://github.com/codehaus-plexus/plexus-utils/issues/36
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            assertNotEquals(expectedCmd.toString(), clString);
        } else {
            assertEquals(expectedCmd.toString(), clString);
        }

        repo = new SvnScmProviderRepository(
                "https://svn.apache.org/repos/asf/maven/scm/trunk",
                "username",
                "password\"with\"double\"quotes'and'single'quotes");
        clString = SvnCommandLineUtils.cryptPassword(
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));
        expectedCmd = new Commandline("svn --username username --password ***** --no-auth-cache --non-interactive");
        expectedCmd.setWorkingDirectory(new File(".").getAbsolutePath());
        assertEquals(expectedCmd.toString(), clString);

        repo = new SvnScmProviderRepository(
                "https://svn.apache.org/repos/asf/maven/scm/trunk",
                "username",
                "password\"with\"double\"quotes'and'single'quotes and spaces");
        clString = SvnCommandLineUtils.cryptPassword(
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));
        expectedCmd = new Commandline("svn --username username --password ***** --no-auth-cache --non-interactive");
        expectedCmd.setWorkingDirectory(new File(".").getAbsolutePath());
        // FIXME https://github.com/codehaus-plexus/plexus-utils/issues/36
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            assertNotEquals(expectedCmd.toString(), clString);
        } else {
            assertEquals(expectedCmd.toString(), clString);
        }

        repo = new SvnScmProviderRepository("https://svn.apache.org/repos/asf/maven/scm/trunk", "username", null);
        assertCommandLine(
                "svn --username username --no-auth-cache --non-interactive",
                new File("."),
                SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), repo, false));
    }

    @Test
    // ISSUE-1375: ensure that LC_ALL is unset and LC_MESSAGES is set to C to avoid locale issues with svn command
    // output parsing
    void testGetBaseSvnCommandLineLcAllIsSetToC() throws Exception {
        Map<String, String> capturedEnvVarsMap = new HashMap<String, String>();

        SvnCommandLineUtils.CommandlineFactory factory = () -> new Commandline() {
            @Override
            public void addSystemEnvironment() throws Exception {
                super.addSystemEnvironment();
                addEnvironment("LC_ALL", "es_AR.UTF-8");
            }

            @Override
            public void addEnvironment(String name, String value) {
                capturedEnvVarsMap.put(name, value);
                super.addEnvironment(name, value);
            }
        };

        // Inject factory that returns our spy
        SvnCommandLineUtils.setCommandlineFactory(factory);

        try {
            SvnCommandLineUtils.getBaseSvnCommandLine(new File("."), null, false);

            assertEquals("", capturedEnvVarsMap.get("LC_ALL"));
            assertEquals("C", capturedEnvVarsMap.get("LC_MESSAGES"));

        } finally {
            // restore default
            SvnCommandLineUtils.setCommandlineFactory(null);
        }
    }
}
