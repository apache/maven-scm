package org.apache.maven.scm.provider.starteam.command.add;

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
public class StarteamAddCommandTest
    extends ScmTestCase
{

    public void testGetCommandLineWithFileOnRoot()
        throws Exception
    {

        File testFile = new File( "testfile" );

        File testFileDir = testFile.getAbsoluteFile().getParentFile();

        String testFileDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( testFileDir.getAbsolutePath() );

        String expectedCmd = "stcmd add -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl" + " -fp " +
            testFileDirAbsolutePath + " -eol on testfile";

        ScmFileSet fileSet = new ScmFileSet( testFileDir, testFile );
        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", fileSet, "", expectedCmd );
    }

    public void testGetCommandLineWithCR()
        throws Exception
    {
        File testFile = new File( "testfile" );

        File testFileDir = testFile.getAbsoluteFile().getParentFile();

        String testFileDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( testFileDir.getAbsolutePath() );

        String expectedCmd = "stcmd add -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl" + " -fp " +
            testFileDirAbsolutePath + " -cr view_root/dummycr" + " -eol on testfile";

        ScmFileSet fileSet = new ScmFileSet( testFileDir, testFile );
        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", fileSet, "view_root/dummycr",
                         expectedCmd );

    }

    public void testGetCommandLineWithFileInSubDir()
        throws Exception
    {

        File testFile = new File( "target/testfile" );

        File testFileDir = testFile.getAbsoluteFile().getParentFile();

        String testFileDirAbsolutePath =
            StarteamCommandLineUtils.toJavaPath( testFileDir.getAbsolutePath() ) + "/target";

        String expectedCmd = "stcmd add -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl/target" +
            " -fp " + testFileDirAbsolutePath + " -eol on testfile";

        ScmFileSet fileSet = new ScmFileSet( testFileDir, testFile );
        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", fileSet, null, expectedCmd );

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, ScmFileSet fileSet, String crPath, String commandLine )
        throws Exception
    {
        ScmRepository repo = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

        Commandline cl = StarteamAddCommand.createCommandLine( repository, fileSet, crPath );

        assertCommandLine( commandLine, null, cl );
    }
}
