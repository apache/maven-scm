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
package org.apache.maven.scm.provider.svn.svnexe.command.tag;

import java.io.File;

import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.provider.svn.command.tag.SvnTagCommandTckTest;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.jupiter.api.Test;

import static org.apache.maven.scm.provider.svn.SvnScmTestUtils.SVN_COMMAND_LINE;

/**
 * This test tests the tag command.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class SvnExeTagCommandTckTest extends SvnTagCommandTckTest {
    @Override
    public String getScmProviderCommand() {
        return SVN_COMMAND_LINE;
    }

    @Test
    void testTagUserNameSvnSsh() throws Exception {
        File messageFile = File.createTempFile("maven-scm", "commit");
        messageFile.deleteOnExit();

        testCommandLine(
                "scm:svn:svn+ssh://foo.com/svn/trunk",
                "svntag",
                messageFile,
                "user",
                "svn --username user --no-auth-cache --non-interactive copy --file " + messageFile.getAbsolutePath()
                        + " --encoding UTF-8 --parents . svn+ssh://user@foo.com/svn/tags/svntag@",
                null);
    }

    @Test
    void testTagRemoteTagHttps() throws Exception {
        File messageFile = File.createTempFile("maven-scm", "commit");
        messageFile.deleteOnExit();

        ScmTagParameters scmTagParameters = new ScmTagParameters();
        scmTagParameters.setRemoteTagging(true);
        scmTagParameters.setPinExternals(false);
        testCommandLine(
                "scm:svn:https://foo.com/svn/trunk",
                "svntag",
                messageFile,
                "user",
                "svn --username user --no-auth-cache --non-interactive copy --file " + messageFile.getAbsolutePath()
                        + " --encoding UTF-8 --parents https://foo.com/svn/trunk@ https://foo.com/svn/tags/svntag@",
                scmTagParameters);
    }

    @Test
    void testTagRemoteTagHttpsWithPinExternals() throws Exception {
        File messageFile = File.createTempFile("maven-scm", "commit");
        messageFile.deleteOnExit();

        ScmTagParameters scmTagParameters = new ScmTagParameters();
        scmTagParameters.setRemoteTagging(true);
        scmTagParameters.setPinExternals(true);
        testCommandLine(
                "scm:svn:https://foo.com/svn/trunk",
                "svntag",
                messageFile,
                "user",
                "svn --username user --no-auth-cache --non-interactive copy --file " + messageFile.getAbsolutePath()
                        + " --encoding UTF-8 --parents --pin-externals https://foo.com/svn/trunk@ https://foo.com/svn/tags/svntag@",
                scmTagParameters);
    }

    @Test
    void testTagRemoteTagHttpsWithRevision() throws Exception {
        File messageFile = File.createTempFile("maven-scm", "commit");
        messageFile.deleteOnExit();

        ScmTagParameters scmTagParameters = new ScmTagParameters();
        scmTagParameters.setRemoteTagging(true);
        scmTagParameters.setPinExternals(false);
        scmTagParameters.setScmRevision("12");
        testCommandLine(
                "scm:svn:https://foo.com/svn/trunk",
                "svntag",
                messageFile,
                "user",
                "svn --username user --no-auth-cache --non-interactive copy --file " + messageFile.getAbsolutePath()
                        + " --encoding UTF-8 --parents --revision 12 https://foo.com/svn/trunk@ https://foo.com/svn/tags/svntag@",
                scmTagParameters);
    }

    @Test
    void testTagRemoteTagHttpsWithRevisionAndPinExternals() throws Exception {
        File messageFile = File.createTempFile("maven-scm", "commit");
        messageFile.deleteOnExit();

        ScmTagParameters scmTagParameters = new ScmTagParameters();
        scmTagParameters.setRemoteTagging(true);
        scmTagParameters.setPinExternals(true);
        scmTagParameters.setScmRevision("12");
        testCommandLine(
                "scm:svn:https://foo.com/svn/trunk",
                "svntag",
                messageFile,
                "user",
                "svn --username user --no-auth-cache --non-interactive copy --file " + messageFile.getAbsolutePath()
                        + " --encoding UTF-8 --parents --revision 12 --pin-externals https://foo.com/svn/trunk@ https://foo.com/svn/tags/svntag@",
                scmTagParameters);
    }

    private void testCommandLine(
            String scmUrl,
            String tag,
            File messageFile,
            String user,
            String commandLine,
            ScmTagParameters scmTagParameters)
            throws Exception {
        File workingDirectory = getTestFile("target/svn-update-command-test");

        ScmRepository repository = getScmManager().makeScmRepository(scmUrl);

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        svnRepository.setUser(user);

        Commandline cl = null;

        cl = new SvnTagCommand(false)
                .createCommandLine(svnRepository, workingDirectory, tag, messageFile, scmTagParameters);

        assertCommandLine(commandLine, workingDirectory, cl);
    }
}
