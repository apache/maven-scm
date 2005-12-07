package org.apache.maven.scm.provider.starteam.command.unedit;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;
import java.io.File;

/**
 * @author <a href="mailto:dantran@apache.org">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamUnEditConsumer
    implements StreamConsumer
{
    private String workingDirectory;

    private ScmLogger logger;

    private List files = new ArrayList();

    /**
     * the current directory entry being processed by the parser
     */
    private String currentDir = "";

    /**
     * Marks current directory data
     */
    private static final String DIR_MARKER = "(working dir: ";

    /**
     * Marks current file data
     */
    private static final String UNLOCKED_MARKER = ": unlocked";


    public StarteamUnEditConsumer( ScmLogger logger, File basedir )
    {
        this.logger = logger;
        this.workingDirectory = basedir.getPath().replace( '\\', '/' );
    }

    public void consumeLine( String line )
    {
        logger.debug( line );
        
        int pos = 0;

        if ( ( pos = line.indexOf( DIR_MARKER ) ) != -1 )
        {
            processDirectory( line, pos );
        }
        else if ( ( pos = line.indexOf( UNLOCKED_MARKER ) ) != -1 )
        {
            processUnLockedFile( line, pos );
        }
        else
        {
            this.logger.warn( "Unknown unedit ouput: " + line );
        }
    }

    public List getUnEditFiles()
    {
        return files;
    }

    private void processDirectory( String line, int pos )
    {
        String dirPath = line.substring( pos + DIR_MARKER.length(), line.length() - 1 ).replace( '\\', '/' );

        if ( !dirPath.startsWith( workingDirectory ) )
        {
            logger.info( "Working directory: " + workingDirectory );

            logger.info( "unedit directory: " + dirPath );

            throw new IllegalStateException( "Working and unedit directories are not on the same tree" );
        }

        this.currentDir = "." + dirPath.substring( workingDirectory.length() );
    }

    private void processUnLockedFile( String line, int pos )
    {
        String lockedFilePath = this.currentDir + "/" + line.substring( 0, pos );

        this.files.add( new ScmFile( lockedFilePath, ScmFileStatus.UNKNOWN) );

        this.logger.info( "Locked: " + lockedFilePath );
    }


}
