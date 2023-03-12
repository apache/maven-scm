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

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class StatusMojoTest extends AbstractMojoTestCase {
    public void testStatusMojo() throws Exception {
        if (!ScmTestCase.isSystemCmd(SvnScmTestUtils.SVN_COMMAND_LINE)) {
            ScmTestCase.printSystemCmdUnavail(SvnScmTestUtils.SVN_COMMAND_LINE, getName());
            return;
        }

        StatusMojo mojo = (StatusMojo) lookupMojo("status", getTestFile("src/test/resources/mojos/status/status.xml"));

        mojo.setWorkingDirectory(new File(getBasedir()));
        mojo.execute();
    }
}
