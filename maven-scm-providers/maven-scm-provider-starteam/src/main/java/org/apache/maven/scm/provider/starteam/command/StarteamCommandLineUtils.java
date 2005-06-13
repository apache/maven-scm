package org.apache.maven.scm.provider.starteam.command;

/*
 * Copyright 2003-2005 The Apache Software Foundation.
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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.logging.Logger;

import java.io.File;

/**
 * Command line construction utility.
 *
 * @author Dan T. Tran
 * @version $Id: $
 */
public class StarteamCommandLineUtils
{

    public static Commandline createStarteamBaseCommandLine( String action, StarteamScmProviderRepository repo )
    {
        Commandline cl = new Commandline();

        cl.createArgument().setValue( "stcmd" );

        cl.createArgument().setValue( action );

        cl.createArgument().setValue( "-x" );

        cl.createArgument().setValue( "-nologo" );

        cl.createArgument().setValue( "-stop" );

        return cl;
    }
    
    public static Commandline createStarteamBaseCommandLine( String action, File relativeFileOrDir, StarteamScmProviderRepository repo )
    {
        
        Commandline cl = createStarteamBaseCommandLine(action, repo);

		//set URL, makesure to alter the orginal URL 
        // to match with the working checkout directory of scm file
        
		String fullUrl = repo.getFullUrl();

        File relativeWorkingDir = relativeFileOrDir.getParentFile();
        
		if ( relativeFileOrDir.isDirectory() )
		{
		    relativeWorkingDir = relativeFileOrDir;
		}
		else
		{
	        if ( relativeWorkingDir != null ) 
		    {
		        fullUrl += "/"  + relativeWorkingDir.getPath().replace( '\\', '/' );                   
		    }
		}
	        
        cl.createArgument().setValue( "-p" );
        cl.createArgument().setValue( fullUrl );
        
        //set working directory
        
        File absoluteWorkingDir = relativeFileOrDir.getAbsoluteFile().getParentFile();
        
		if ( relativeFileOrDir.isDirectory() )
		{
		    absoluteWorkingDir = relativeFileOrDir.getAbsoluteFile();
		}
		
        cl.createArgument().setValue( "-fp" );
        
        cl.createArgument().setValue( absoluteWorkingDir.getAbsolutePath().replace( '\\', '/' ) );
		
        return cl;
    }    
    
    public static String toJavaPath(String path )
    {
        return path.replace('\\', '/' );
    }
    
    /**
     * Hellper method to display command line without password
     * @param cl
     * @return String
     * @throws ScmException
     */
    public static String displayCommandlineWithoutPassword (Commandline cl )
        throws ScmException
    {
        String retStr = "";
        
        String fullStr = cl.toString();
        
        //look for -p and take out the password arugment
        
        int usernamePos = fullStr.indexOf( "-p ") + 3;
        
        if ( usernamePos == 2 )
        {
            //should never get here since all starteam command lines
            // have -p argument
            
            throw new ScmException( "Invalid command line" );
        }

        retStr = fullStr.substring(0, usernamePos);
        
        int passwordStartPos = fullStr.indexOf( ":" );

        if ( passwordStartPos == -1 )
        {
            throw new ScmException( "Invalid command line" );
        }
        
        int passwordEndPos = fullStr.indexOf( "@" );

        if ( passwordEndPos == -1 )
        {
            throw new ScmException( "Invalid command line" );
        }
        
        retStr += fullStr.substring( usernamePos, passwordStartPos );
        
        retStr += fullStr.substring( passwordEndPos );
        
        return retStr;
        
    }
    
    
    public static int executeCommandline(Commandline cl, StreamConsumer consumer, CommandLineUtils.StringStreamConsumer stderr, Logger logger )
		throws ScmException
	{
        logger.info( "Command line: " + displayCommandlineWithoutPassword( cl ) );

        try
        {
            return CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }
	}    
}
