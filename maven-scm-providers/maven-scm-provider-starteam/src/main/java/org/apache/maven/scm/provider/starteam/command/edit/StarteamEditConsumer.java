package org.apache.maven.scm.provider.starteam.command.edit;

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
 * @author <a href="mailto:dantran@apache.org">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamEditConsumer
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
    private static final String LOCKED_MARKER = ": locked";


    public StarteamEditConsumer( ScmLogger logger, File basedir )
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
        else if ( ( pos = line.indexOf( LOCKED_MARKER ) ) != -1 )
        {
            processLockedFile( line, pos );
        }
        else
        {
            if ( logger.isWarnEnabled() )
            {
                logger.warn( "Unknown edit ouput: " + line );
            }
        }
    }

    public List getEditedFiles()
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
                logger.info( "Edit directory: " + dirPath );
            }

            throw new IllegalStateException( "Working and edit directories are not on the same tree" );
        }

        this.currentDir = "." + dirPath.substring( workingDirectory.length() );
    }

    private void processLockedFile( String line, int pos )
    {
        String lockedFilePath = this.currentDir + "/" + line.substring( 0, pos );

        this.files.add( new ScmFile( lockedFilePath, ScmFileStatus.UNKNOWN ) );

        if ( logger.isInfoEnabled() )
        {
            logger.info( "Locked: " + lockedFilePath );
        }
    }


}
