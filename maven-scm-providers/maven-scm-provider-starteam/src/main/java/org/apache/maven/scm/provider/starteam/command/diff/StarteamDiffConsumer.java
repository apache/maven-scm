package org.apache.maven.scm.provider.starteam.command.diff;

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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamDiffConsumer
    implements StreamConsumer
{
    private final static String WORKING_DIR_TOKEN = "(working dir: ";

    private final static String PATCH_SEPARATOR_TOKEN = "--------------";

    private final static String REVISION_TOKEN = " Revision: ";

    private final static String ONDISK_TOKEN = " (on disk)";

    private final static String ADDED_LINE_TOKEN = "+";

    private final static String REMOVED_LINE_TOKEN = "-";

    private final static String UNCHANGED_LINE_TOKEN = " ";

    private ScmLogger logger;

    private String currentDir = "";

    private boolean diffBlockProcessingStarted = false;

    private boolean revisionBlockStarted = false;

    private String currentFile;

    private StringBuffer currentDifference;

    private List changedFiles = new ArrayList();

    private Map differences = new HashMap();

    private StringBuffer patch = new StringBuffer();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public StarteamDiffConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    public void consumeLine( String line )
    {
        int pos = 0;

        logger.debug( line );

        patch.append( line ).append( "\n" );

        if ( line.trim().length() == 0 )
        {
            return;
        }

        if ( ( pos = line.indexOf( WORKING_DIR_TOKEN ) ) != -1 )
        {
            processGetDir( line, pos );

            return;
        }

        if ( line.startsWith( PATCH_SEPARATOR_TOKEN ) )
        {
            diffBlockProcessingStarted = !diffBlockProcessingStarted;

            if ( diffBlockProcessingStarted )
            {
                if ( revisionBlockStarted )
                {
                    throw new IllegalStateException( "Missing second Revision line or local copy line " );
                }
            }

            return;
        }

        if ( ( pos = line.indexOf( REVISION_TOKEN ) ) != -1 )
        {
            if ( revisionBlockStarted )
            {
                revisionBlockStarted = false;
            }
            else
            {
                extractCurrentFile( line, pos );

                revisionBlockStarted = true;
            }

            return;
        }

        if ( ( pos = line.indexOf( ONDISK_TOKEN ) ) != -1 )
        {
            if ( revisionBlockStarted )
            {
                revisionBlockStarted = false;
            }
            else
            {
                throw new IllegalStateException( "Working copy line found at the wrong state " );
            }

            return;
        }

        if ( !diffBlockProcessingStarted )
        {
            logger.warn( "Unparseable line: '" + line + "'" );

            return;
        }

        if ( line.startsWith( ADDED_LINE_TOKEN ) || line.startsWith( REMOVED_LINE_TOKEN )
             || line.startsWith( UNCHANGED_LINE_TOKEN ) )
        {
            // add to buffer
            currentDifference.append( line ).append( "\n" );
        }
        else
        {
            logger.warn( "Unparseable line: '" + line + "'" );
        }
    }

    /**
     * Process the current input line in the Get Directory
     *
     * @param line a line of text from the Starteam log output
     */
    private void processGetDir( String line, int pos )
    {
        String dirPath = line.substring( pos + WORKING_DIR_TOKEN.length(), line.length() - 1 ).replace( '\\', '/' );

        this.currentDir = dirPath;
    }

    private void extractCurrentFile( String line, int pos )
    {
        currentFile = line.substring( 0, pos );

        changedFiles.add( new ScmFile( currentFile, ScmFileStatus.MODIFIED ) );

        currentDifference = new StringBuffer();

        differences.put( currentFile, currentDifference );
    }

    public List getChangedFiles()
    {
        return changedFiles;
    }

    public Map getDifferences()
    {
        return differences;
    }

    public String getPatch()
    {
        return patch.toString();
    }

}
