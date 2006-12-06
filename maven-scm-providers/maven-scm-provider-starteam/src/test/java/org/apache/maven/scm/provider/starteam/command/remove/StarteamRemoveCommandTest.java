package org.apache.maven.scm.provider.starteam.command.remove;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 */
public class StarteamRemoveCommandTest
    extends ScmTestCase
{

    public void testGetCommandLineWithFileOnRoot()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "testfile.txt" ) );

        String testFileDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );

        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;

        String expectedCmd = "stcmd remove -x -nologo -stop" + " -p " + starteamUrl + " -fp " +
            testFileDirAbsolutePath + " testfile.txt";

        testCommandLine( mavenUrl, fileSet, "", expectedCmd );
    }

    public void testGetCommandLineWithFileInSubDir()
        throws Exception
    {

        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "dir/testfile.txt" ) );

        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;

        String localDirectory = StarteamCommandLineUtils.toJavaPath( getWorkingDirectory().getPath() + "/dir" );
        String expectedCmd = "stcmd remove -x -nologo -stop" + " -p " + starteamUrl + "/dir" + " -fp " +
            localDirectory + " testfile.txt";

        testCommandLine( mavenUrl, fileSet, null, expectedCmd );

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, ScmFileSet fileSet, String crPath, String commandLine )
        throws Exception
    {
        ScmRepository repo = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

        Commandline cl = StarteamRemoveCommand.createCommandLine( repository, fileSet );

        assertEquals( commandLine, cl.toString() );
    }
}
