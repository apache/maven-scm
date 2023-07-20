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
package org.apache.maven.scm.provider.svn.svnexe.command.checkin;

import java.io.File;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.util.SvnUtil;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class SvnCheckInCommandTest extends ScmTestCase {
    private File messageFile;

    private String messageFileString;

    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();

        messageFile = new File("commit-message");

        String path = messageFile.getAbsolutePath();
        if (path.indexOf(' ') >= 0) {
            path = "\"" + path + "\"";
        }
        messageFileString = "--file " + path + " --encoding UTF-8";
    }

    @Test
    public void testCommandLineWithEmptyTag() throws Exception {
        testCommandLine("scm:svn:http://foo.com/svn/trunk", "svn --non-interactive commit " + messageFileString);
    }

    @Test
    public void testCommandLineWithoutTag() throws Exception {
        testCommandLine("scm:svn:http://foo.com/svn/trunk", "svn --non-interactive commit " + messageFileString);
    }

    @Test
    public void testCommandLineTag() throws Exception {
        testCommandLine(
                "scm:svn:http://anonymous@foo.com/svn/trunk",
                "svn --username anonymous --no-auth-cache --non-interactive commit " + messageFileString);
    }

    @Test
    public void testCommandLineWithUsernameAndTag() throws Exception {
        testCommandLine(
                "scm:svn:http://anonymous@foo.com/svn/trunk",
                "svn --username anonymous --no-auth-cache --non-interactive commit " + messageFileString);
    }

    @Test
    public void testCommandLineWithUsernameWithoutNonInteractive() throws Exception {
        try {
            SvnUtil.setSettingsDirectory(getTestFile("src/test/resources/svn/checkin/macos"));
            testCommandLine(
                    "scm:svn:http://anonymous@foo.com/svn/trunk",
                    "svn --username anonymous --no-auth-cache commit " + messageFileString);
        } finally {

            SvnUtil.setSettingsDirectory(SvnUtil.DEFAULT_SETTINGS_DIRECTORY);
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine(String scmUrl, String commandLine) throws Exception {
        File workingDirectory = getTestFile("target/svn-checkin-command-test");

        ScmRepository repository = getScmManager().makeScmRepository(scmUrl);

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl =
                SvnCheckInCommand.createCommandLine(svnRepository, new ScmFileSet(workingDirectory), messageFile);

        assertCommandLine(commandLine, workingDirectory, cl);
    }
}
