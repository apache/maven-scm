package org.apache.maven.scm.provider.bazaar.command.diff;

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
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a>
 * @version $Id$
 */
public class BazaarDiffConsumer
    extends BazaarConsumer
{

    private static final String MODIFIED_FILE_TOKEN = "=== modified file ";

    private static final String ADDED_FILE_TOKEN = "=== added file ";

    private static final String DELETED_FILE_TOKEN = "=== deleted file ";

    private static final String NO_NEWLINE_TOKEN = "\\ No newline at end of file";

    private static final String FROM_FILE_TOKEN = "---";

    private static final String TO_FILE_TOKEN = "+++";

    private static final String ADDED_LINE_TOKEN = "+";

    private static final String REMOVED_LINE_TOKEN = "-";

    private static final String UNCHANGED_LINE_TOKEN = " ";

    private static final String RANGE_TOKEN = "@@";

    private ScmLogger logger;

    private File workingDirectory;

    private String currentFile;

    private StringBuilder currentDifference;

    private List<ScmFile> changedFiles = new ArrayList<ScmFile>();

    private Map<String,CharSequence> differences = new HashMap<String,CharSequence>();

    private StringBuffer patch = new StringBuffer();

    public BazaarDiffConsumer( ScmLogger logger, File workingDirectory )
    {
        super( logger );
        this.logger = logger;
        this.workingDirectory = workingDirectory;
    }

    /** {@inheritDoc} */
    public void doConsume( ScmFileStatus status, String line )
    {
        String tmpLine = new String( line );
        patch.append( line ).append( "\n" );

        // Parse line
        if ( line.startsWith( MODIFIED_FILE_TOKEN ) )
        {
            tmpLine = line.substring( MODIFIED_FILE_TOKEN.length() );
            tmpLine = tmpLine.trim();
            status = ScmFileStatus.MODIFIED;
            addChangedFile( status, line, tmpLine );
        }
        else if ( line.startsWith( ADDED_FILE_TOKEN ) )
        {
            tmpLine = line.substring( ADDED_FILE_TOKEN.length() );
            tmpLine = tmpLine.trim();
            status = ScmFileStatus.ADDED;
            addChangedFile( status, line, tmpLine );
        }
        else if ( line.startsWith( DELETED_FILE_TOKEN ) )
        {
            tmpLine = line.substring( DELETED_FILE_TOKEN.length() );
            tmpLine = tmpLine.trim();
            status = ScmFileStatus.DELETED;
            addChangedFile( status, line, tmpLine );
        }
        else if ( line.startsWith( TO_FILE_TOKEN ) || line.startsWith( FROM_FILE_TOKEN ) )
        {
            // ignore (to avoid conflicts with add and remove tokens)
        }
        else if ( line.startsWith( ADDED_LINE_TOKEN ) || line.startsWith( REMOVED_LINE_TOKEN )
            || line.startsWith( UNCHANGED_LINE_TOKEN ) || line.startsWith( RANGE_TOKEN )
            || line.startsWith( NO_NEWLINE_TOKEN ) )
        {
            currentDifference.append( line ).append( "\n" );
        }
    }

    /**
     * This method takes into account two types of diff output. <br>
     * - Bazaar 0.7 format: dir/dir/myfile  <br>
     * - Bazaar 0.8 format: a/dir/dir/myfile <br>
     *
     * @param status  Eg. modified or added
     * @param line    The original bazaar output to process (for logging)
     * @param tmpLine The bazaar output to process
     */
    private void addChangedFile( ScmFileStatus status, String line, String tmpLine )
    {
        tmpLine = tmpLine.substring( 1, tmpLine.length() - 1 );
        boolean ok = addChangedFile( status, tmpLine );

        if ( !ok )
        {
            int index = tmpLine.indexOf( '/' );
            if ( index > -1 )
            {
                tmpLine = tmpLine.substring( index + 1 );
                ok = addChangedFile( status, tmpLine );
            }
        }

        if ( !ok )
        {
            if ( logger.isWarnEnabled() )
            {
                logger.warn( "Could not figure out of line: " + line );
            }
        }
    }

    /**
     * @param status
     * @param tmpLine
     * @return True if tmpLine was a valid file and thus added to the changeset
     */
    private boolean addChangedFile( ScmFileStatus status, String tmpLine )
    {
        File tmpFile = new File( workingDirectory, tmpLine );
        if ( status.equals( ScmFileStatus.DELETED ) )
        {
            return true;
        }

        if ( tmpFile.isFile() )
        {
            currentFile = tmpLine;
            currentDifference = new StringBuilder();
            differences.put( currentFile, currentDifference );
            changedFiles.add( new ScmFile( tmpLine, status ) );
            return true;
        }

        return false;
    }

    public List<ScmFile> getChangedFiles()
    {
        return changedFiles;
    }

    public Map<String,CharSequence> getDifferences()
    {
        return differences;
    }

    public String getPatch()
    {
        return patch.toString();
    }
}
