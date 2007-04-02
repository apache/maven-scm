package org.apache.maven.scm.provider.starteam.command.checkin;

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
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 */
public class StarteamCheckInCommandTest
    extends ScmTestCase
{

    public void testGetCommandLineWithWorkingDirectory()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );

        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;

        String expectedCmd =
            "stcmd ci -x -nologo -stop" + " -p " + starteamUrl + " -fp " + workingCopy + " -is -f NCI -eol on";

        testCommandLine( mavenUrl, fileSet, "", new ScmRevision( "" ), "", "", expectedCmd );

    }

    public void testGetCommandLineWithFileOnRoot()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "test.txt" ) );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );

        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;

        String expectedCmd =
            "stcmd ci -x -nologo -stop" + " -p " + starteamUrl + " -fp " + workingCopy + " -eol on test.txt";

        testCommandLine( mavenUrl, fileSet, "", new ScmRevision( "" ), "", "", expectedCmd );

    }

    public void testGetCommandLineWithFileInSubDir()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "src/test.txt" ) );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );

        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;

        String expectedCmd = "stcmd ci -x -nologo -stop" + " -p " + starteamUrl + "/src" + " -fp " + workingCopy +
            "/src" + " -eol on test.txt";

        testCommandLine( mavenUrl, fileSet, "", new ScmRevision( "" ), "", "", expectedCmd );

    }

    public void testGetCommandLineWithDirInWorkingDirectory()
        throws Exception
    {
        //physically create dir so that cmd can be generated correctly
        new File( getWorkingCopy(), "src" ).mkdirs();

        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "src" ) );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );

        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;

        String expectedCmd = "stcmd ci -x -nologo -stop" + " -p " + starteamUrl + "/src" + " -fp " + workingCopy +
            "/src" + " -is -f NCI -eol on";

        testCommandLine( mavenUrl, fileSet, "", new ScmRevision( "" ), "", "", expectedCmd );

    }

    public void testGetCommandLineWithEmptyIssueValue()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "test.txt" ) );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );

        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;

        String expectedCmd =
            "stcmd ci -x -nologo -stop" + " -p " + starteamUrl + " -fp " + workingCopy + " -active -eol on test.txt";

        testCommandLine( mavenUrl, fileSet, null, new ScmRevision( "" ), "active", " ", expectedCmd );
    }
// ----------------------------------------------------------------------
//
// ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, ScmFileSet fileSet, String message, ScmVersion version,
                                  String issueType, String issueValue, String commandLine )
        throws Exception
    {
        ScmRepository repo = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

        Commandline cl =
            StarteamCheckInCommand.createCommandLine( repository, fileSet, message, version, issueType, issueValue );

        assertEquals( commandLine, cl.toString() );
    }


}
