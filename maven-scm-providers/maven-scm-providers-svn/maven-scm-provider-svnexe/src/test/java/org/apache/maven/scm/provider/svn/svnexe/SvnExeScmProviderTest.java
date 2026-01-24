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
package org.apache.maven.scm.provider.svn.svnexe;

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.codehaus.plexus.util.Os;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SvnExeScmProviderTest {
    private SvnExeScmProvider scmProvider;

    @BeforeEach
    void onSetup() {
        scmProvider = new SvnExeScmProvider();
    }

    // SCM-435
    @Test
    @Disabled("This test is for now ignore: it mock Logger and then asserts against it")
    public void testGetRepositoryURLWindows() throws Exception {
        assumeTrue(Os.isFamily(Os.FAMILY_WINDOWS));

        // prepare
        Logger logger = mock(Logger.class);
        when(logger.isInfoEnabled()).thenReturn(Boolean.TRUE);
        // scmProvider.addListener( logger );
        File workingDirectory = new File(".");

        // test
        // Since SCM-project has moved from svn to GIT, we can't verify the URL of this project
        String url;
        try {
            url = scmProvider.getRepositoryURL(workingDirectory);

            // verify
            assertFalse(url.startsWith("file://"));
        } catch (ScmException e) {
        }

        // verify
        verify(logger).info("Executing: cmd.exe /X /C \"svn --non-interactive info .\"");
        verify(logger).info("Working directory: " + workingDirectory.getCanonicalPath());
    }
}
