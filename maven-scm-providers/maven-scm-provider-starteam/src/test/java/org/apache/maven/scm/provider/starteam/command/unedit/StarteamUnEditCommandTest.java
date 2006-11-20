package org.apache.maven.scm.provider.starteam.command.unedit;

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
public class StarteamUnEditCommandTest
    extends ScmTestCase
{

    public void testGetCommandLineWithFileOnRoot()
        throws Exception
    {
    	ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "src/test.txt" ) );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );
    	
        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;
        
        String expectedCmd = "stcmd lck -x -nologo -stop"
        	                 + " -p " + starteamUrl    + "/src"
                             + " -fp " + workingCopy + "/src"
                             + " -u test.txt" ; 
        
        testCommandLine( mavenUrl, fileSet, expectedCmd );
    }


    public void testGetCommandLineWithFileInSubDir()
        throws Exception
    {
    	ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "src/test.txt" ) );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );
    	
        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;
        
        String expectedCmd = "stcmd lck -x -nologo -stop"
        	                 + " -p " + starteamUrl    + "/src"
                             + " -fp " + workingCopy + "/src"
                             + " -u test.txt" ; 
        
        testCommandLine( mavenUrl, fileSet, expectedCmd );

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, ScmFileSet fileName, String commandLine )
        throws Exception
    {
        ScmRepository repo = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

        Commandline cl = StarteamUnEditCommand.createCommandLine( repository, fileName );

        assertEquals( commandLine, cl.toString() );
    }
}
