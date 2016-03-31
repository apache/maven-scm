package org.apache.maven.scm.provider.git.gitexe;

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

import java.io.File;
import java.io.IOException;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.git.gitexe.command.FakeShell;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

public abstract class GitExeTestCase extends ScmTestCase {
	@Override
	public void assertCommandLine(String expectedCommand, File expectedWorkingDirectory, Commandline actualCommand)
			throws IOException {
        Commandline cl = new Commandline( expectedCommand );
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) ) {
            cl.setShell( new FakeShell() );
        }
        if ( expectedWorkingDirectory != null )
        {
            cl.setWorkingDirectory( expectedWorkingDirectory.getAbsolutePath() );
        }
        String expectedCommandLineAsExecuted = StringUtils.join( cl.getShellCommandline(), " " );
        String actualCommandLineAsExecuted = StringUtils.join( actualCommand.getShellCommandline(), " " );
        assertEquals( expectedCommandLineAsExecuted, actualCommandLineAsExecuted );
	}
}
