package org.apache.maven.scm.provider.svn.svnexe.command.checkout;

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
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SvnCheckOutConsumer
    implements StreamConsumer
{
    private final static String CHECKED_OUT_REVISION_TOKEN = "Checked out revision";

    private ScmLogger logger;

    private File workingDirectory;

    private List checkedOutFiles = new ArrayList();

    private int revision;

    public SvnCheckOutConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;

        this.workingDirectory = workingDirectory;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    public void consumeLine( String line )
    {
        logger.debug( line );

        if ( line.length() <= 3 )
        {
            logger.warn( "Unexpected input, the line must be at least three characters long. Line: '" + line + "'." );

            return;
        }

        String statusString = line.substring( 0, 1 );

        String file = line.substring( 3 ).trim();

        ScmFileStatus status;

        if ( line.startsWith( CHECKED_OUT_REVISION_TOKEN ) )
        {
            String revisionString = line.substring( CHECKED_OUT_REVISION_TOKEN.length() + 1, line.length() - 1 );

            try
            {
                revision = Integer.parseInt( revisionString );
            }
            catch ( NumberFormatException ex )
            {
                // ignored
            }

            return;
        }
        else if ( statusString.equals( "A" ) )
        {
            status = ScmFileStatus.ADDED;
        }
        else if ( statusString.equals( "U" ) )
        {
            status = ScmFileStatus.UPDATED;
        }
        else
        {
            logger.info( "Unknown file status: '" + statusString + "'." );

            return;
        }

        // If the file isn't a file; don't add it.
        if ( !new File( workingDirectory, file ).isFile() )
        {
            logger.debug( "Skipping non-file: " + file );
            return;
        }

        checkedOutFiles.add( new ScmFile( file, status ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List getCheckedOutFiles()
    {
        return checkedOutFiles;
    }

    public int getRevision()
    {
        return revision;
    }
}
