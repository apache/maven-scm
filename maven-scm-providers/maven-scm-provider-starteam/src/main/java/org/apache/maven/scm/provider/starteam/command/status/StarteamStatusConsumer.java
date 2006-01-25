package org.apache.maven.scm.provider.starteam.command.status;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamStatusConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private String workingDirectory;

    private List changedFiles = new ArrayList();

    /**
     * Marks current directory data
     */
    private static final String DIR_MARKER = "(working dir: ";

    /**
     * Marks current file data
     */
    private static final String FILE_MARKER = "History for: ";

    /**
     * Marks current file status
     */
    private static final String STATUS_MARKER = "Status: ";

    /**
     * Marks current file status
     */
    private static final String OUTDATE_MARKER = "Out of Date";

    private static final String MISSING_MARKER = "Missing";

    private static final String CURRENT_MARKER = "Current";

    private static final String MERGE_MARKER = "Merge";

    private static final String MODIFIED_MARKER = "Modified";

    private String currentDir = "";

    private String currentFile = "";

    public StarteamStatusConsumer( ScmLogger logger, File basedir )
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
            processGetDir( line, pos );
        }
        else if ( ( pos = line.indexOf( FILE_MARKER ) ) != -1 )
        {
            processGetFile( line, pos );
        }
        else if ( ( pos = line.indexOf( STATUS_MARKER ) ) != -1 )
        {
            processStatus( line, pos );
        }
        else
        {
            //do nothing
        }
    }

    private void processGetDir( String line, int pos )
    {
        String dirPath = line.substring( pos + DIR_MARKER.length(), line.length() - 1 ).replace( '\\', '/' );

        this.currentDir = "." + dirPath.substring( workingDirectory.length() );
    }

    private void processGetFile( String line, int pos )
    {
        String fileName = line.substring( pos + FILE_MARKER.length(), line.length() );

        String checkedOutFilePath = this.currentDir + "/" + fileName;

        this.currentFile = checkedOutFilePath;
    }

    private void processStatus( String line, int pos )
    {
        String status = line.substring( pos + STATUS_MARKER.length(), line.length() );

        if ( status.equals( OUTDATE_MARKER ) )
        {
            changedFiles.add( new ScmFile( this.currentFile, ScmFileStatus.MODIFIED ) );

            logger.info( "Out of Date file: " + this.currentFile );
        }
        else if ( status.equals( MODIFIED_MARKER ) )
        {
            changedFiles.add( new ScmFile( this.currentFile, ScmFileStatus.MODIFIED ) );

            logger.info( "Modified file: " + this.currentFile );
        }
        else if ( status.equals( MISSING_MARKER ) )
        {
            changedFiles.add( new ScmFile( this.currentFile, ScmFileStatus.ADDED ) );

            logger.info( "Missing file: " + this.currentFile );
        }
        else if ( status.equals( MERGE_MARKER ) )
        {
            changedFiles.add( new ScmFile( this.currentFile, ScmFileStatus.CONFLICT ) );

            logger.info( "Conflict file: " + this.currentFile );
        }
        else if ( status.equals( CURRENT_MARKER ) )
        {
            //ignore   
        }
        else
        {
            logger.warn( "status unknown (" + status + "): " + this.currentFile );
        }
    }

    public List getChangedFiles()
    {
        return changedFiles;
    }

}
