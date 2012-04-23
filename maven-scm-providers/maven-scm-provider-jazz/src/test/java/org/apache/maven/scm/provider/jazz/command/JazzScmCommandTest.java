package org.apache.maven.scm.provider.jazz.command;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.provider.jazz.JazzScmTestCase;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.Commandline;

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

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzScmCommandTest
    extends JazzScmTestCase
{
    public void testFileList()
    {
        assertTrue( getScmFileSet().getFileList().size() > 0 );
    }

    public void testJazzScmCommand()
        throws Exception
    {
        ScmFileSet scmFileSet = new ScmFileSet( getWorkingCopy() );
        JazzScmCommand listCommand = new JazzScmCommand( "list", getScmProviderRepository(), scmFileSet, null );
        String expected =
            "scm list --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword";

        assertCommandLine( expected, getWorkingDirectory(), listCommand.getCommandline() );

    }

    public void testCryptPassword()
        throws Exception
    {
        JazzScmCommand listCommand = new JazzScmCommand( "list", getScmProviderRepository(), null, null );
        String actual = JazzScmCommand.cryptPassword( listCommand.getCommandline() );
        String expected = Os.isFamily( Os.FAMILY_WINDOWS )
            ? "cmd.exe /X /C \"scm list --repository-uri https://localhost:9443/jazz --username myUserName --password *****\""
            : "/bin/sh -c scm list --repository-uri https://localhost:9443/jazz --username myUserName --password '*****'";

        System.out.println( "actual:" + actual );
        assertEquals( "cryptPassword failed!", expected, actual );
    }
}
