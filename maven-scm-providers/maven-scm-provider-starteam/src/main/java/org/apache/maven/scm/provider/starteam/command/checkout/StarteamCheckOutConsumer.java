package org.apache.maven.scm.provider.starteam.command.checkout;

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
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamCheckOutConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private String workingDirectory;

    private String currentDir = "";

    private List files = new ArrayList();

    /**
     * Marks current directory data
     */
    private static final String DIR_MARKER = "(working dir: ";

    /**
     * Marks current file data
     */
    private static final String CHECKOUT_MARKER = ": checked out";

    /**
     * Marks skipped file during update
     */
    private static final String SKIPPED_MARKER = ": skipped";

    public StarteamCheckOutConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;

        this.workingDirectory = workingDirectory.getPath().replace( '\\', '/' );
    }

    public void consumeLine( String line )
    {
        logger.debug( line );

        int pos = 0;

        if ( ( pos = line.indexOf( CHECKOUT_MARKER ) ) != -1 )
        {
            processCheckedOutFile( line, pos );
        }
        else if ( ( pos = line.indexOf( DIR_MARKER ) ) != -1 )
        {
            processDirectory( line, pos );
        }
        else if ( ( pos = line.indexOf( CHECKOUT_MARKER ) ) != -1 )
        {
            processCheckedOutFile( line, pos );
        }
        else if ( ( pos = line.indexOf( SKIPPED_MARKER ) ) != -1 )
        {
            processSkippedFile( line, pos );
        }
        else
        {
            this.logger.warn( "Unknown checkout ouput: " + line );
        }
    }

    public List getCheckedOutFiles()
    {
        return files;
    }

    private void processDirectory( String line, int pos )
    {
        String dirPath = line.substring( pos + DIR_MARKER.length(), line.length() - 1 ).replace( '\\', '/' );

        if ( !dirPath.startsWith( workingDirectory ) )
        {
            logger.info( "Working directory: " + workingDirectory );

            logger.info( "Checked out directory: " + dirPath );

            throw new IllegalStateException( "Working and check out directories are not on the same tree" );
        }

        this.currentDir = "." + dirPath.substring( workingDirectory.length() );
    }

    private void processCheckedOutFile( String line, int pos )
    {
        String checkedOutFilePath = this.currentDir + "/" + line.substring( 0, pos );

        this.files.add( new ScmFile( checkedOutFilePath, ScmFileStatus.CHECKED_OUT ) );

        this.logger.info( "Checked out: " + checkedOutFilePath );
    }

    private void processSkippedFile( String line, int pos )
    {
        String skippedFilePath = this.currentDir + "/" + line.substring( 0, pos );

        this.logger.debug( "Skipped: " + skippedFilePath );
    }

}
