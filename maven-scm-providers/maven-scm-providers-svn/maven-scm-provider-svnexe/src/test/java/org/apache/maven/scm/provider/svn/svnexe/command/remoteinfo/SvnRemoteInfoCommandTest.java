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
package org.apache.maven.scm.provider.svn.svnexe.command.remoteinfo;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.junit.Test;

import static org.apache.maven.scm.provider.svn.SvnScmTestUtils.SVN_COMMAND_LINE;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Olivier Lamy
 */
public class SvnRemoteInfoCommandTest extends ScmTestCase {
    @Test
    public void testExist() throws Exception {
        checkSystemCmdPresence(SVN_COMMAND_LINE);

        SvnRemoteInfoCommand svnRemoteInfoCommand = new SvnRemoteInfoCommand(false);

        SvnScmProviderRepository svnScmProviderRepository =
                new SvnScmProviderRepository("http://svn.apache.org/repos/asf/maven/scm/trunk/");
        assertTrue(svnRemoteInfoCommand.remoteUrlExist(svnScmProviderRepository, null));
    }

    @Test
    public void testNotExist() throws Exception {
        checkSystemCmdPresence(SVN_COMMAND_LINE);

        SvnRemoteInfoCommand svnRemoteInfoCommand = new SvnRemoteInfoCommand(false);

        SvnScmProviderRepository svnScmProviderRepository =
                new SvnScmProviderRepository("http://svn.apache.org/repos/asf/maven/scm/trunk/foo/bar/beer");
        assertFalse(svnRemoteInfoCommand.remoteUrlExist(svnScmProviderRepository, null));
    }
}
