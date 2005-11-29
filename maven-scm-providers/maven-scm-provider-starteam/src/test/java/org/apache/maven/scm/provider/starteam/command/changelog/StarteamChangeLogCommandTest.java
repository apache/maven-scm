package org.apache.maven.scm.provider.starteam.command.changelog;

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

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamChangeLogCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
		File workDir = new File("target");
		
		String workDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( workDir.getAbsolutePath() );

        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl",
                         workDir,
                         "stcmd hist -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl " +
                         "-fp " + workDirAbsolutePath + " -is", null  );
    }

    public void testGetCommandLineWithStartDate()
        throws Exception
    {
        SimpleDateFormat localFormat = new SimpleDateFormat();
        
        Date startDate = new Date( 0 );
        
		File workDir = new File("target");

		String workDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( workDir.getAbsolutePath() );

        String startDateStr = localFormat.format( startDate ).toString();
        
        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl",
                         workDir,
                         "stcmd hist -x -nologo -stop -p myusername:mypassword@myhost:1234/projecturl " +
                         "-fp " + workDirAbsolutePath + " -is -cfgd \"" + startDateStr + "\"", startDate );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, File workDir, String commandLine, Date startDate )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository svnRepository = (StarteamScmProviderRepository) repository.getProviderRepository();

        Commandline cl = StarteamChangeLogCommand.createCommandLine( svnRepository, workDir, startDate );

        assertEquals( commandLine, cl.toString() );
    }
 }
