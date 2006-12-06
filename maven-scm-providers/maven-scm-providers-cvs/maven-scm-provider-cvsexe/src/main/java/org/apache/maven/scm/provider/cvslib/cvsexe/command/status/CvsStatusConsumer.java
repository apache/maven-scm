package org.apache.maven.scm.provider.cvslib.cvsexe.command.status;

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
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class CvsStatusConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private File workingDirectory;

    private List changedFiles = new ArrayList();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public CvsStatusConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;

        this.workingDirectory = workingDirectory;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    public void consumeLine( String line )
    {
        if ( line.length() <= 3 )
        {
            logger.warn( "Unexpected input, the line must be at least three characters long. Line: '" + line + "'." );

            return;
        }

        String statusString = line.substring( 0, 1 );

        String file = line.substring( 2 );

        ScmFileStatus status;

        if ( statusString.equals( "A" ) )
        {
            status = ScmFileStatus.ADDED;
        }
        else if ( statusString.equals( "M" ) )
        {
            status = ScmFileStatus.MODIFIED;
        }
        else if ( statusString.equals( "D" ) )
        {
            status = ScmFileStatus.DELETED;
        }
        else if ( statusString.equals( "C" ) )
        {
            status = ScmFileStatus.CONFLICT;
        }
        else if ( statusString.equals( "?" ) )
        {
            status = ScmFileStatus.UNKNOWN;
        }
        else if ( statusString.equals( "U" ) || statusString.equals( "P" ) )
        {
            // skip remote changes
            return;
        }
        else
        {
            logger.info( "Unknown file status: '" + statusString + "'." );

            return;
        }

        // If the file isn't a file; don't add it.
        if ( !new File( workingDirectory, file ).isFile() )
        {
            return;
        }

        changedFiles.add( new ScmFile( file, status ) );
    }

    public List getChangedFiles()
    {
        return changedFiles;
    }
}
