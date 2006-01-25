package org.apache.maven.scm.provider.starteam.command.checkout;

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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamCheckOutCommandTest
    extends ScmTestCase
{
    public void testGetCommandLineWithWorkingDirectory()
        throws Exception
    {
        //note that workDir must exists, make should have already created it
        File workDir = new File( getBasedir() + "/target" );

        String workDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( workDir.getAbsolutePath() );

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", workDir, "myTag",
                         "stcmd co -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl " + "-fp " +
                             workDirAbsolutePath + " -vl myTag -is" );
    }

    public void testGetCommandLineWithWorkingDirectoryAsAbsolutePath()
        throws Exception
    {
        File workDir = new File( getBasedir() + "/target" );

        String workDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( workDir.getAbsolutePath() );

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", workDir.getAbsoluteFile(),
                         "myTag", "stcmd co -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl " +
            "-fp " + workDirAbsolutePath + " -vl myTag -is" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, File workDir, String tag, String commandLine )
        throws Exception
    {
        ScmRepository repo = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

        Commandline cl = StarteamCheckOutCommand.createCommandLine( repository, workDir, tag );

        assertEquals( commandLine, cl.toString() );
    }

}
