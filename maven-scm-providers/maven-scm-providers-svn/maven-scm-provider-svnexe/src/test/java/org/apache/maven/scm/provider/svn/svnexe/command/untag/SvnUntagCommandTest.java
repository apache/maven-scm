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
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.jupiter.api.Test;

/**
 * test the subversion untag implementation
 *
 */
public class SvnUntagCommandTest extends ScmTestCase {

    /**
     * test with http repo and user
     *
     * @throws Exception in case of error
     */
    @Test
    public void testUntagHttp() throws Exception {

        File messageFile = File.createTempFile("maven-scm", "untag");
        messageFile.deleteOnExit();

        testCommandLine(
                "scm:svn:http://foo.com/svn/tags",
                new ScmFileSet(getUntagTestFile()),
                "svntag",
                "user",
                messageFile,
                "svn --username user --no-auth-cache --non-interactive " + "--file " + messageFile.getAbsolutePath()
                        + " remove http://foo.com/svn/tags/svntag@");
    }

    /**
     * test with ssh repo and user
     *
     * @throws Exception in case of error
     */
    @Test
    public void testUntagSsh() throws Exception {

        File messageFile = File.createTempFile("maven-scm", "untag");
        messageFile.deleteOnExit();

        testCommandLine(
                "scm:svn:svn+ssh://foo.com/svn/tags",
                new ScmFileSet(getUntagTestFile()),
                "svntag",
                "user",
                messageFile,
                "svn --username user --no-auth-cache --non-interactive " + "--file " + messageFile.getAbsolutePath()
                        + " remove svn+ssh://user@foo.com/svn/tags/svntag@");
    }

    /**
     * define path to local dir
     *
     * @return local dir
     */
    private File getUntagTestFile() {
        return getTestFile("target/svn-untag-command-test");
    }

    /**
     * get svn repo instance
     *
     * @param scmUrl     url to svn repo
     * @return           svn repo instance
     * @throws Exception in case of error
     */
    private SvnScmProviderRepository getSvnRepository(String scmUrl) throws Exception {
        ScmRepository repository = getScmManager().makeScmRepository(scmUrl);

        return (SvnScmProviderRepository) repository.getProviderRepository();
    }

    /**
     * test routine for command line
     *
     * @param scmUrl      url to build repo instnace from
     * @param scmFileSet  file set containing local base dir
     * @param tag         tag to delete
     * @param user        svn user for repo access
     * @param messageFile file containing commit message
     * @param commandline set command line to compare actual to
     * @throws Exception  in case of error
     */
    private void testCommandLine(
            String scmUrl, ScmFileSet scmFileSet, String tag, String user, File messageFile, String commandline)
            throws Exception {
        SvnScmProviderRepository repo = getSvnRepository(scmUrl);
        repo.setUser(user);
        Commandline cl = new SvnUntagCommand(false).createCommandline(repo, scmFileSet, tag, messageFile);

        assertCommandLine(commandline, scmFileSet.getBasedir(), cl);
    }
}
