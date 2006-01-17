package org.apache.maven.scm.provider.starteam.command.unedit;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 */
public class StarteamUnEditCommandTest
    extends ScmTestCase
{

    public void testGetCommandLineWithFileOnRoot()
        throws Exception
    {

        File testFile = new File( "testfile" );

        File testFileDir = testFile.getAbsoluteFile().getParentFile();

        String testFileDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( testFileDir.getAbsolutePath() );

        String expectedCmd = "stcmd lck -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl" + " -fp " +
            testFileDirAbsolutePath + " -u testfile";

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", testFile, expectedCmd );
    }


    public void testGetCommandLineWithFileInSubDir()
        throws Exception
    {

        File testFile = new File( "target/testfile" );

        File testFileDir = testFile.getAbsoluteFile().getParentFile();

        String testFileDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( testFileDir.getAbsolutePath() );

        String expectedCmd = "stcmd lck -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl/target" +
            " -fp " + testFileDirAbsolutePath + " -u" + " testfile";

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", testFile, expectedCmd );

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, File fileName, String commandLine )
        throws Exception
    {
        ScmRepository repo = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

        Commandline cl = StarteamUnEditCommand.createCommandLine( repository, fileName );

        assertEquals( commandLine, cl.toString() );
    }
}
