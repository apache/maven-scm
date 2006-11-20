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
    	ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );
    	
        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;
        
        String expectedCmd = "stcmd ci -x -nologo -stop"
        	                 + " -p " + starteamUrl   
                             + " -fp " + workingCopy 
                             + " -is -f NCI" ; 
        
        testCommandLine( mavenUrl, fileSet, "", "", "", "", expectedCmd );

    }

    public void testGetCommandLineWithFileOnRoot()
        throws Exception
    {
    	ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "test.txt" ) );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );
    	
        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;
        
        String expectedCmd = "stcmd ci -x -nologo -stop"
        	                 + " -p " + starteamUrl   
                             + " -fp " + workingCopy 
                             + " test.txt" ; 
        
        testCommandLine( mavenUrl, fileSet, "", "", "", "", expectedCmd );
    	
    }

    public void testGetCommandLineWithFileInSubDir()
        throws Exception
    {
    	ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "src/test.txt" ) );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );
    	
        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;
        
        String expectedCmd = "stcmd ci -x -nologo -stop"
        	                 + " -p " + starteamUrl + "/src"  
                             + " -fp " + workingCopy + "/src"
                             + " test.txt" ; 
        
        testCommandLine( mavenUrl, fileSet, "", "", "", "", expectedCmd );

    }

    public void testGetCommandLineWithDirInWorkingDirectory()
        throws Exception
    {
    	//physically create dir so that cmd can be generated correctly
    	new File( getWorkingCopy(), "src").mkdirs();
    	
	    ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "src" ) );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );
	
        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;
    
        String expectedCmd = "stcmd ci -x -nologo -stop"
    	                 + " -p " + starteamUrl + "/src"  
                         + " -fp " + workingCopy + "/src"
                         + " -is -f NCI" ; 
    
        testCommandLine( mavenUrl, fileSet, "", "", "", "", expectedCmd );

   }
    
    public void testGetCommandLineWithEmptyIssueValue()
        throws Exception
    {
    	ScmFileSet fileSet = new ScmFileSet( getWorkingCopy(), new File( "test.txt" ) );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );
    	
        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;
        
        String expectedCmd = "stcmd ci -x -nologo -stop"
        	                 + " -p " + starteamUrl   
                             + " -fp " + workingCopy 
                             + " -active test.txt" ; 
        
        testCommandLine( mavenUrl, fileSet, null, "", "active", " ", expectedCmd );
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
