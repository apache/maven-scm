package org.apache.maven.scm.provider.starteam.command.add;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id: $
 */
public class StarteamAddConsumer
    implements StreamConsumer
{
    private Logger logger;
    
    private String workingDirectory;

    /**
     * the current directory entry being processed by the parser
     */
    private String currentDir;

    private List files = new ArrayList();

    /**
     * Marks current directory data
     */
    private static final String DIR_MARKER = "(working dir: ";

    /**
     * Marks current file data
     */
    private static final String ADDED_MARKER = ": added";
   
    /**
     * Marks current file data
     */
    private static final String LINKTO_MARKER = ": linked to";
    

    public StarteamAddConsumer( Logger logger, File basedir )
    {
        this.logger = logger;
        
        this.workingDirectory = basedir.getPath().replace('\\', '/');
    }

    public void consumeLine( String line )
    {
        logger.debug(line);
        
        int pos =0;
        
        if ( ( pos = line.indexOf( DIR_MARKER ) ) != -1 )
        {
            processDirectory(line, pos);
        }
        else if ( ( pos = line.indexOf( ADDED_MARKER ) ) != -1 ) 
        {
            processAddedFile(line, pos);
        }
        else if ( ( pos = line.indexOf( LINKTO_MARKER ) ) != -1 ) 
        {
            //ignore
        }
        else
        {
          this.logger.warn("Unknown add ouput: " + line);
        }
    }

    public List getAddedFiles()
    {
        return files;
    }
    
    private void processDirectory( String line, int pos )
    {
        String dirPath = line
                          .substring( pos + DIR_MARKER.length(), line.length() - 1 )
                          .replace('\\', '/');
            
        if ( ! dirPath.startsWith( workingDirectory ) )
        {
            logger.info("Working directory: " + workingDirectory );
            logger.info("Checkin directory path: " + dirPath ) ;
            throw new IllegalStateException( "Working and checkin directories are not on the same tree" );
        }
        
        this.currentDir = "." + dirPath.substring( workingDirectory.length() );
    }
    
    private void processAddedFile( String line, int pos )
    {
        String addedFilePath = this.currentDir + "/" + line.substring( 0, pos );
            
        this.files.add( new ScmFile( addedFilePath, ScmFileStatus.ADDED ) );
            
        this.logger.info("Added: " + addedFilePath );
    }    
    
}
