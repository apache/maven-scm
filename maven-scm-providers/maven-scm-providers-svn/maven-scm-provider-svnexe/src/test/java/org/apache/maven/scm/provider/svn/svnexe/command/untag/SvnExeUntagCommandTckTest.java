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
package org.apache.maven.scm.provider.svn.svnexe.command.untag;

import java.io.File;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.provider.svn.command.untag.SvnUntagCommandTckTest;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.jupiter.api.Test;

import static org.apache.maven.scm.provider.svn.SvnScmTestUtils.SVN_COMMAND_LINE;

/**
 * This test tests the untag command for Subversion.
 *
 */
public class SvnExeUntagCommandTckTest extends SvnUntagCommandTckTest {
    @Override
    public String getScmProviderCommand() {
        return SVN_COMMAND_LINE;
    }

    /**
     * test against ssh repository with user
     *
     * @throws Exception in case of error
     */
    @Test
    public void testUntagSsh() throws Exception {
        File messageFile = File.createTempFile("maven-scm", "commit");
        messageFile.deleteOnExit();

        ScmFileSet scmFileSet = new ScmFileSet(new File("target/svn-untag-command-test"));

        testCommandLine(
                "scm:svn:svn+ssh://foo.com/svn/trunk",
                scmFileSet,
                "svntag",
                messageFile,
                "user",
                "svn --username user --no-auth-cache --non-interactive --file " + messageFile.getAbsolutePath()
                        + " remove svn+ssh://user@foo.com/svn/tags/svntag@");
    }

    /**
     * test against https repository with user
     *
     * @throws Exception in case of error
     */
    @Test
    public void testUntagHttps() throws Exception {
        File messageFile = File.createTempFile("maven-scm", "commit");
        messageFile.deleteOnExit();

        ScmFileSet scmFileSet = new ScmFileSet(new File("target/svn-untag-command-test"));

        testCommandLine(
                "scm:svn:https://foo.com/svn/tags",
                scmFileSet,
                "svntag",
                messageFile,
                "user",
                "svn --username user --no-auth-cache --non-interactive --file " + messageFile.getAbsolutePath()
                        + " remove https://foo.com/svn/tags/svntag@");
    }

    /**
     * test routine, build command line and assert
     *
     * @param scmUrl      url to svn repo
     * @param scmFileSet  file set for local dir
     * @param tag         tag to delete
     * @param messageFile file containing commit message
     * @param user        user to acces the repo with
     * @param commandLine set command line for comparison
     * @throws Exception  in case of error
     */
    private void testCommandLine(
            String scmUrl, ScmFileSet scmFileSet, String tag, File messageFile, String user, String commandLine)
            throws Exception {
        File workingDirectory = getTestFile("target/svn-untag-command-test");

        ScmRepository repository = getScmManager().makeScmRepository(scmUrl);

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        svnRepository.setUser(user);

        Commandline cl = new SvnUntagCommand(false).createCommandline(svnRepository, scmFileSet, tag, messageFile);

        assertCommandLine(commandLine, workingDirectory, cl);
    }
}
