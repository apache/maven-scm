package org.apache.maven.scm.provider.perforce.command.changelog;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * Parse the tagged output from "p4 describe -s [change] [change] [...]".
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class PerforceDescribeConsumer
    extends AbstractConsumer
{
    
    private List<ChangeSet> entries = new ArrayList<ChangeSet>();

    /**
     * State machine constant: expecting revision
     */
    private static final int GET_REVISION = 1;

    /**
     * State machine constant: eat the first blank line
     */
    private static final int GET_COMMENT_BEGIN = 2;

    /**
     * State machine constant: expecting comments
     */
    private static final int GET_COMMENT = 3;

    /**
     * State machine constant: expecting "Affected files"
     */
    private static final int GET_AFFECTED_FILES = 4;

    /**
     * State machine constant: expecting blank line
     */
    private static final int GET_FILES_BEGIN = 5;

    /**
     * State machine constant: expecting files
     */
    private static final int GET_FILE = 6;

    /**
     * Current status of the parser
     */
    private int status = GET_REVISION;

    /**
     * The current log entry being processed by the parser
     */
    @SuppressWarnings( "unused" )
    private String currentRevision;

    /**
     * The current log entry being processed by the parser
     */
    private ChangeSet currentChange;

    /**
     * the current file being processed by the parser
     */
    private String currentFile;

    /**
     * The location of files within the Perforce depot that we are processing
     * e.g. //depot/projects/foo/bar
     */
    private String repoPath;

    private String userDatePattern;

    private static final String REVISION_PATTERN = "^Change (\\d+) " + // changelist number
        "by (.*)@[^ ]+ " + // author
        "on (.*)"; // date
    /**
     * The comment section ends with a blank line
     */
    private static final String COMMENT_DELIMITER = "";
    /**
     * The changelist ends with a blank line
     */
    private static final String CHANGELIST_DELIMITER = "";

    private static final String FILE_PATTERN = "^\\.\\.\\. (.*)#(\\d+) ";

    /**
     * The regular expression used to match header lines
     */
    private RE revisionRegexp;

    /**
     * The regular expression used to match file paths
     */
    private RE fileRegexp;

    public PerforceDescribeConsumer( String repoPath, String userDatePattern, ScmLogger logger )
    {
        super( logger );

        this.repoPath = repoPath;
        this.userDatePattern = userDatePattern;

        try
        {
            revisionRegexp = new RE( REVISION_PATTERN );
            fileRegexp = new RE( FILE_PATTERN );
        }
        catch ( RESyntaxException ignored )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Could not create regexps to parse Perforce descriptions", ignored );
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List<ChangeSet> getModifications() throws ScmException
    {
        return entries;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        switch ( status )
        {
            case GET_REVISION:
                processGetRevision( line );
                break;
            case GET_COMMENT_BEGIN:
                status = GET_COMMENT;
                break;
            case GET_COMMENT:
                processGetComment( line );
                break;
            case GET_AFFECTED_FILES:
                processGetAffectedFiles( line );
                break;
            case GET_FILES_BEGIN:
                status = GET_FILE;
                break;
            case GET_FILE:
                processGetFile( line );
                break;
            default:
                throw new IllegalStateException( "Unknown state: " + status );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Add a change log entry to the list (if it's not already there)
     * with the given file.
     *
     * @param entry a {@link ChangeSet} to be added to the list if another
     *              with the same key (p4 change number) doesn't exist already.
     * @param file  a {@link ChangeFile} to be added to the entry
     */
    private void addEntry( ChangeSet entry, ChangeFile file )
    {
        entry.addFile( file );
    }

    /**
     * Each file matches the fileRegexp.
     *
     * @param line A line of text from the Perforce log output
     */
    private void processGetFile( String line )
    {
        if ( line.equals( CHANGELIST_DELIMITER ) ) {
            entries.add( 0, currentChange );
            status = GET_REVISION;
            return;
        }
        if ( !fileRegexp.match( line ) )
        {
            return;
        }

        currentFile = fileRegexp.getParen( 1 );

	// Although Perforce allows files to be submitted anywhere in the
	// repository in a single changelist, we're only concerned about the
	// local files.
        if( currentFile.startsWith( repoPath ) ) {
            currentFile = currentFile.substring( repoPath.length() + 1 );
            addEntry( currentChange, new ChangeFile( currentFile, fileRegexp.getParen( 2 ) ) );
        }
    }

    /**
     * Most of the relevant info is on the revision line matching the
     * 'pattern' string.
     *
     * @param line A line of text from the perforce log output
     */
    private void processGetRevision( String line )
    {
        if ( !revisionRegexp.match( line ) )
        {
            return;
        }
        currentChange = new ChangeSet();
        currentRevision = revisionRegexp.getParen( 1 );
        currentChange.setAuthor( revisionRegexp.getParen( 2 ) );
        currentChange.setDate( revisionRegexp.getParen( 3 ), userDatePattern );

        status = GET_COMMENT_BEGIN;
    }

    /**
     * Process the current input line in the GET_COMMENT state.  This
     * state gathers all of the comments that are part of a log entry.
     *
     * @param line a line of text from the perforce log output
     */
    private void processGetComment( String line )
    {
        if ( line.equals( COMMENT_DELIMITER ) )
        {
            status = GET_AFFECTED_FILES;
        }
        else
        {
            // remove prepended tab
            currentChange.setComment( currentChange.getComment() + line.substring(1) + "\n" );
        }
    }

    /**
     * Process the current input line in the GET_COMMENT state.  This
     * state gathers all of the comments that are part of a log entry.
     *
     * @param line a line of text from the perforce log output
     */
    private void processGetAffectedFiles( String line )
    {
        if ( !line.equals( "Affected files ..." ) )
        {
            return;
        }
        status = GET_FILES_BEGIN;
    }
}
