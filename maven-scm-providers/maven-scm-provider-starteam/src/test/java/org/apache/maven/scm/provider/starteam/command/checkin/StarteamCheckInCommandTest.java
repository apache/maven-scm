package org.apache.maven.scm.provider.starteam.command.checkin;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
public class StarteamCheckInCommandTest
    extends ScmTestCase
{

    public void testGetCommandLineWithWorkingDirectory()
        throws Exception
    {
        File workDir = new File( getBasedir() + "/target" );

        String workDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( workDir.getAbsolutePath() );

        ScmFileSet fileSet = new ScmFileSet( workDir );

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", fileSet, "", "", "", "",
                         "stcmd ci -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl " + "-fp " +
                             workDirAbsolutePath + " -f NCI -is" );
    }

    public void testGetCommandLineWithFileOnRoot()
        throws Exception
    {
        File testFile = new File( "testfile" );

        String testFileAbsolutePath = StarteamCommandLineUtils.toJavaPath( testFile.getAbsoluteFile().getParent() );

        ScmFileSet fileSet = new ScmFileSet( testFile.getAbsoluteFile().getParentFile(), testFile );

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", fileSet, "myMessage", "myTag",
                         "", "", "stcmd ci -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl " + "-fp " +
            testFileAbsolutePath + " -r myMessage -vl myTag " + "testfile" );

    }

    public void testGetCommandLineWithFileInSubDir()
        throws Exception
    {
        File testFile = new File( "src/testfile.txt" );

        File workingDir = testFile.getAbsoluteFile().getParentFile().getParentFile();
        ScmFileSet fileSet = new ScmFileSet( workingDir, testFile );
        
        String testFileDirectory = StarteamCommandLineUtils.toJavaPath( testFile.getAbsoluteFile().getParent() );

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", fileSet, null, "", "cr" ,"myCr",
                         "stcmd ci -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl/src " + "-fp " +
                         testFileDirectory + " -cr myCr " + "testfile.txt" );
    }

    public void testGetCommandLineWithEmptyIssueValue()
        throws Exception
    {
        File testFile = new File( "src/testfile" );

        File workingDir = testFile.getAbsoluteFile().getParentFile().getParentFile();
        ScmFileSet fileSet = new ScmFileSet( workingDir, testFile );
        
        String testFileAbsolutePath = StarteamCommandLineUtils.toJavaPath( testFile.getAbsoluteFile().getParent() );

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", fileSet, null, "", "active", " ",
                         "stcmd ci -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl/src " + "-fp "
                             + testFileAbsolutePath + " -active " + "testfile" );
    }    
// ----------------------------------------------------------------------
//
// ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, ScmFileSet fileSet, String message, String tag, String issueType,
    		                      String issueValue, String commandLine )
        throws Exception
    {
        ScmRepository repo = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

        Commandline cl = StarteamCheckInCommand.createCommandLine( repository, fileSet, message, tag, issueType, issueValue );

        assertEquals( commandLine, cl.toString() );
    }


}
