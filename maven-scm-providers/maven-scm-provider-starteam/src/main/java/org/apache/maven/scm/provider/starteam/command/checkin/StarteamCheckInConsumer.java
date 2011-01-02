package org.apache.maven.scm.provider.starteam.command.checkin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
 * @author Olivier Lamy
 * @version $Id$
 */
public class StarteamCheckInConsumer
    implements StreamConsumer
{
    private String workingDirectory;

    private ScmLogger logger;

    private List<ScmFile> files = new ArrayList<ScmFile>();

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
    private static final String CHECKIN_MARKER = ": checked in";

    /**
     * Marks skipped file during update
     */
    private static final String SKIPPED_MARKER = ": skipped";

    /**
     * Marks current file data
     */
    private static final String LINKTO_MARKER = ": linked to";

    public StarteamCheckInConsumer( ScmLogger logger, File basedir )
    {
        this.logger = logger;
        this.workingDirectory = basedir.getPath().replace( '\\', '/' );
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( line );
        }

        int pos = 0;

        if ( ( pos = line.indexOf( DIR_MARKER ) ) != -1 )
        {
            processDirectory( line, pos );
        }
        else if ( ( pos = line.indexOf( CHECKIN_MARKER ) ) != -1 )
        {
            processCheckedInFile( line, pos );
        }
        else if ( ( pos = line.indexOf( SKIPPED_MARKER ) ) != -1 )
        {
            processSkippedFile( line, pos );
        }
        else if ( ( pos = line.indexOf( LINKTO_MARKER ) ) != -1 )
        {
            //ignore
        }
        else
        {
            if ( logger.isWarnEnabled() )
            {
                logger.warn( "Unknown checkin ouput: " + line );
            }
        }

    }

    public List<ScmFile> getCheckedInFiles()
    {
        return files;
    }

    private void processDirectory( String line, int pos )
    {
        String dirPath = line.substring( pos + DIR_MARKER.length(), line.length() - 1 ).replace( '\\', '/' );

        if ( !dirPath.startsWith( workingDirectory ) )
        {
            if ( logger.isInfoEnabled() )
            {
                logger.info( "Working directory: " + workingDirectory );
                logger.info( "Checkin directory: " + dirPath );
            }

            throw new IllegalStateException( "Working and checkin directories are not on the same tree" );
        }

        this.currentDir = "." + dirPath.substring( workingDirectory.length() );
    }

    private void processCheckedInFile( String line, int pos )
    {
        String checkedInFilePath = this.currentDir + "/" + line.substring( 0, pos );

        this.files.add( new ScmFile( checkedInFilePath, ScmFileStatus.CHECKED_OUT ) );

        if ( logger.isInfoEnabled() )
        {
            logger.info( "Checked in: " + checkedInFilePath );
        }
    }

    private void processSkippedFile( String line, int pos )
    {
        String skippedFilePath = this.currentDir + "/" + line.substring( 0, pos );

        if ( logger.isInfoEnabled() )
        {
            logger.info( "Skipped: " + skippedFilePath );
        }
    }
}
