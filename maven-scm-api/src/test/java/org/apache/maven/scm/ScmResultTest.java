package org.apache.maven.scm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import junit.framework.TestCase;

public class ScmResultTest
    extends TestCase
{

    private static final String PASSWORD = "secr$t";

    private static final String SCM_URL_GIT_COLON = "scm:git:https://username:" + PASSWORD + "@github.com/username/repo.git";

    private static final String MOCK_ERROR_OUTPUT = "fatal: repository '" + SCM_URL_GIT_COLON + "' not found";

    private static final String MOCK_ERROR_MULTILINE_OUTPUT = "remote: Invalid username or password." + System.lineSeparator() + "fatal: Authentication failed for '" + SCM_URL_GIT_COLON + "'";

    public void testPasswordsAreMaskedInOutput()
        throws Exception
    {
        ScmResult result = new ScmResult( "git push", "git-push failed", MOCK_ERROR_OUTPUT, false );
        assertNotSame( "Command output contains password", MOCK_ERROR_OUTPUT, result.getCommandOutput() );
        assertTrue( "Command output not masked", result.getCommandOutput().contains( ScmResult.PASSWORD_PLACE_HOLDER ) );

        result = new ScmResult( "git push", "git-push failed", MOCK_ERROR_MULTILINE_OUTPUT, false );
        assertNotSame( "Command output contains password", MOCK_ERROR_MULTILINE_OUTPUT, result.getCommandOutput() );
        assertTrue( "Command output not masked", result.getCommandOutput().contains( ScmResult.PASSWORD_PLACE_HOLDER ) );
    }

}
