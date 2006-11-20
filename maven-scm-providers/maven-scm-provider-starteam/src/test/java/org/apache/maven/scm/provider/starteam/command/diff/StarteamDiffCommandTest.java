package org.apache.maven.scm.provider.starteam.command.diff;

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
public class StarteamDiffCommandTest
    extends ScmTestCase
{

    public void testGetCommandLine()
        throws Exception
    {

    	ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );
    	
        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;
        
        String expectedCmd = "stcmd diff -x -nologo -stop"
        	                 + " -p " + starteamUrl   
                             + " -fp " + workingCopy 
                             + " -is -filter M" ; 
        
        testCommandLine( mavenUrl, fileSet,null, null, expectedCmd );

    }

    
    public void testGetCommandLineWithLabels()
        throws Exception
    {

    	
    	ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        String workingCopy = StarteamCommandLineUtils.toJavaPath( getWorkingCopy().getPath() );
    	
        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;
        
        String expectedCmd = "stcmd diff -x -nologo -stop"
        	                 + " -p " + starteamUrl   
                             + " -fp " + workingCopy 
                             + " -is -filter M" 
                             + " -vl label1 -vl label2";
        
        testCommandLine( mavenUrl, fileSet, "label1", "label2", expectedCmd );  
        
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, ScmFileSet basedir, String startLabel, String endLabel, String commandLine )
        throws Exception
    {

        ScmRepository repo = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

        Commandline cl = StarteamDiffCommand.createCommandLine( repository, basedir, startLabel, endLabel );

        assertEquals( commandLine, cl.toString() );
    }

}
