package org.apache.maven.scm.provider.starteam.command.update;

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
public class StarteamUpdateCommandTest
    extends ScmTestCase
{
    public void testGetCommandLineWithWorkingDirectory()
        throws Exception
    {
        File workDir = new File( getBasedir() + "/target" );

        String absolutePath = StarteamCommandLineUtils.toJavaPath( workDir.getAbsolutePath() );

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", workDir, "myTag",
                         "stcmd co -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl " + "-fp " +
                             absolutePath + " -merge -neverprompt -vl myTag -is" );

    }


    public void testGetCommandLineWithFileOnRoot()
        throws Exception
    {
        System.out.println( "testGetCommandLineWithFileOnRoot" );

        File testFile = new File( "testfile" );

        String testFileAbsolutePath = StarteamCommandLineUtils.toJavaPath( testFile.getAbsoluteFile().getParent() );

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", testFile, "myTag",
                         "stcmd co -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl " + "-fp " +
                             testFileAbsolutePath + " -merge -neverprompt -vl myTag " + "testfile" );
    }

    public void testGetCommandLineWithFileInSubDir()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( new File( getBasedir() ), "**/*.java", null );

        File [] files = fileSet.getFiles();

        File testFile = files[0];

        String absolutePath = StarteamCommandLineUtils.toJavaPath( testFile.getAbsoluteFile().getParent() );

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", testFile, "myTag",
                         "stcmd co -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl/" +
                             StarteamCommandLineUtils.toJavaPath( testFile.getParent() ) + " -fp " + absolutePath +
                             " -merge -neverprompt -vl myTag " + testFile.getName() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, File testFileOrDir, String tag, String commandLine )
        throws Exception
    {
        ScmRepository repo = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

        Commandline cl = StarteamUpdateCommand.createCommandLine( repository, testFileOrDir, tag );

        System.out.println( commandLine );

        System.out.println( cl );

        assertEquals( commandLine, cl.toString() );
    }
}
