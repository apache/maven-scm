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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamChangeLogCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", null,
                         "stcmd hist -x -nologo -is -p myusername:mypassword@myhost:1234/projecturl" );
    }

    public void testGetCommandLineWithTag()
        throws Exception
    {
        testCommandLine( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", "myTag",
                         "stcmd hist -x -nologo -is -p myusername:mypassword@myhost:1234/projecturl -vl myTag" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, String tag, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/starteam-changelog-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository svnRepository = (StarteamScmProviderRepository) repository.getProviderRepository();

        Commandline cl = StarteamChangeLogCommand.createCommandLine( svnRepository, workingDirectory, tag );

        assertEquals( commandLine, cl.toString() );
    }
 }
