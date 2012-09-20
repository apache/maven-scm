package org.apache.maven.scm.provider.git.gitexe.command.changelog;

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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Olivier Lamy
 *
 */
public class GitChangeLogConsumer
    extends AbstractConsumer
{
    /**
     * Date formatter for git timestamp
     * we use iso format cli git log --date=iso sample : 2008-08-06 01:37:18 +0200
     */
    private static final String GIT_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss Z";

    /**
     * State machine constant: expecting header
     */
    private static final int STATUS_GET_HEADER = 1;

    /**
     * State machine constant: expecting author information
     */
    private static final int STATUS_GET_AUTHOR = 2;

    /**
     * State machine constant: expecting parent hash information
     */
    private static final int STATUS_RAW_TREE = 21;

    /**
     * State machine constant: expecting parent hash information
     */
    private static final int STATUS_RAW_PARENT = 22;

    /**
     * State machine constant: expecting author name, email and timestamp information
     */
    private static final int STATUS_RAW_AUTHOR = 23;

    /**
     * State machine constant: expecting committer name, email and timestamp information
     */
    private static final int STATUS_RAW_COMMITTER = 24;

    /**
     * State machine constant: expecting date information
     */
    private static final int STATUS_GET_DATE = 3;

    /**
     * State machine constant: expecting file information
     */
    private static final int STATUS_GET_FILE = 4;

    /**
     * State machine constant: expecting comments
     */
    private static final int STATUS_GET_COMMENT = 5;

    /**
     * The pattern used to match git header lines
     */
    private static final String HEADER_PATTERN = "^commit (.*)";

    /**
     * The pattern used to match git author lines
     */
    private static final String AUTHOR_PATTERN = "^Author: (.*)";

    /**
     * The pattern used to match git tree hash lines (raw mode)
     */
    private static final String RAW_TREE_PATTERN = "^tree ([:xdigit:]+)";

    /**
     * The pattern used to match git parent hash lines (raw mode)
     */
    private static final String RAW_PARENT_PATTERN = "^parent ([:xdigit:]+)";

    /**
     * The pattern used to match git author lines (raw mode)
     */
    private static final String RAW_AUTHOR_PATTERN = "^author (.+ <.+>) ([:digit:]+) (.*)";

    /**
     * The pattern used to match git author lines (raw mode)
     */
    private static final String RAW_COMMITTER_PATTERN = "^committer (.+ <.+>) ([:digit:]+) (.*)";

    /**
     * The pattern used to match git date lines
     */
    private static final String DATE_PATTERN = "^Date:\\s*(.*)";

    /**
     * The pattern used to match git file lines
     */
    private static final String FILE_PATTERN =
        "^:\\d* \\d* [:xdigit:]*\\.* [:xdigit:]*\\.* ([:upper:])[:digit:]*\\t([^\\t]*)(\\t(.*))?";

    /**
     * Current status of the parser
     */
    private int status = STATUS_GET_HEADER;

    /**
     * List of change log entries
     */
    private List<ChangeSet> entries = new ArrayList<ChangeSet>();

    /**
     * The current log entry being processed by the parser
     */
    private ChangeSet currentChange;

    /**
     * The current revision of the entry being processed by the parser
     */
    private String currentRevision;

    /**
     * The current comment of the entry being processed by the parser
     */
    private StringBuilder currentComment;

    /**
     * The regular expression used to match header lines
     */
    private RE headerRegexp;

    /**
     * The regular expression used to match author lines
     */
    private RE authorRegexp;

    /**
     * The regular expression used to match tree hash lines in raw mode
     */
    private RE rawTreeRegexp;

    /**
     * The regular expression used to match parent hash lines in raw mode
     */
    private RE rawParentRegexp;

    /**
     * The regular expression used to match author lines in raw mode
     */
    private RE rawAuthorRegexp;

    /**
     * The regular expression used to match committer lines in raw mode
     */
    private RE rawCommitterRegexp;

    /**
     * The regular expression used to match date lines
     */
    private RE dateRegexp;

    /**
     * The regular expression used to match file lines
     */
    private RE fileRegexp;

    private String userDateFormat;

    /**
     * Default constructor.
     */
    public GitChangeLogConsumer( ScmLogger logger, String userDateFormat )
    {
        super( logger );

        this.userDateFormat = userDateFormat;

        try
        {
            headerRegexp = new RE( HEADER_PATTERN );
            authorRegexp = new RE( AUTHOR_PATTERN );
            dateRegexp = new RE( DATE_PATTERN );
            fileRegexp = new RE( FILE_PATTERN );
            rawTreeRegexp = new RE( RAW_TREE_PATTERN );
            rawParentRegexp = new RE( RAW_PARENT_PATTERN );
            rawAuthorRegexp = new RE( RAW_AUTHOR_PATTERN );
            rawCommitterRegexp = new RE( RAW_COMMITTER_PATTERN );
        }
        catch ( RESyntaxException ex )
        {
            throw new RuntimeException(
                "INTERNAL ERROR: Could not create regexp to parse git log file. This shouldn't happen. Something is probably wrong with the oro installation.",
                ex );
        }
    }

    public List<ChangeSet> getModifications()
    {
        // this is needed since the processFile does not always get a the end-sequence correctly.
        processGetFile( "" );

        return entries;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void consumeLine( String line )
    {
        switch ( status )
        {
            case STATUS_GET_HEADER:
                processGetHeader( line );
                break;
            case STATUS_GET_AUTHOR:
                processGetAuthor( line );
                break;
            case STATUS_GET_DATE:
                processGetDate( line, null );
                break;
            case STATUS_GET_COMMENT:
                processGetComment( line );
                break;
            case STATUS_GET_FILE:
                processGetFile( line );
                break;
            case STATUS_RAW_TREE:
                processGetRawTree( line );
                break;
            case STATUS_RAW_PARENT:
                processGetRawParent( line );
                break;
            case STATUS_RAW_AUTHOR:
                processGetRawAuthor( line );
                break;
            case STATUS_RAW_COMMITTER:
                processGetRawCommitter( line );
                break;
            default:
                throw new IllegalStateException( "Unknown state: " + status );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Process the current input line in the GET_HEADER state.  The
     * author, date, and the revision of the entry are gathered.  Note,
     * Git does not have per-file revisions, instead, the entire
     * branch is given a single revision number, which is also used for
     * the revision number of each file.
     *
     * @param line A line of text from the git log output
     */
    private void processGetHeader( String line )
    {
        if ( !headerRegexp.match( line ) )
        {
            return;
        }

        currentRevision = headerRegexp.getParen( 1 );

        currentChange = new ChangeSet();

        currentChange.setRevision( currentRevision );

        status = STATUS_GET_AUTHOR;
    }

    /**
     * Process the current input line in the STATUS_GET_AUTHOR state.  This
     * state gathers all of the author information that are part of a log entry.
     *
     * @param line a line of text from the git log output
     */
    private void processGetAuthor( String line )
    {
        // this autodetects 'raw' format
        if ( rawTreeRegexp.match( line ) )
        {
            status = STATUS_RAW_TREE;
            processGetRawTree( line );
            return;
        }

        if ( !authorRegexp.match( line ) )
        {
            return;
        }
        String author = authorRegexp.getParen( 1 );

        currentChange.setAuthor( author );

        status = STATUS_GET_DATE;
    }

    /**
     * Process the current input line in the STATUS_RAW_TREE state.  This
     * state gathers tree hash part of a log entry.
     *
     * @param line a line of text from the git log output
     */
    private void processGetRawTree( String line )
    {
        if ( !rawTreeRegexp.match( line ) )
        {
            return;
        }
        //here we could set treeHash if it appears in the model: currentChange.setTreeHash( rawTreeRegexp.getParen( 1 ) );
        status = STATUS_RAW_PARENT;
    }

    /**
     * Process the current input line in the STATUS_RAW_PARENT state.  This
     * state gathers parent revisions of a log entry.
     *
     * @param line a line of text from the git log output
     */
    private void processGetRawParent( String line )
    {
        if ( !rawParentRegexp.match( line ) )
        {
            status = STATUS_RAW_AUTHOR;
            processGetRawAuthor( line );
            return;
        }
        String parentHash = rawParentRegexp.getParen( 1 );

        addParentRevision( parentHash );
    }

    /**
     * In git log, both parent and merged revisions are called parent. Fortunately, the real parent comes first in the log.
     * This method takes care of the difference.
     *
     * @param hash -
     */
    private void addParentRevision( String hash )
    {
        if ( currentChange.getParentRevision() == null )
        {
            currentChange.setParentRevision( hash );
        }
        else
        {
            currentChange.addMergedRevision( hash );
        }
    }

    /**
     * Process the current input line in the STATUS_RAW_AUTHOR state.  This
     * state gathers all the author information of a log entry.
     *
     * @param line a line of text from the git log output
     */
    private void processGetRawAuthor( String line )
    {
        if ( !rawAuthorRegexp.match( line ) )
        {
            return;
        }
        String author = rawAuthorRegexp.getParen( 1 );
        currentChange.setAuthor( author );

        String datestring = rawAuthorRegexp.getParen( 2 );
        String tz = rawAuthorRegexp.getParen( 3 );

        // with --format=raw option (which gets us to this methods), date is always in seconds since beginning of time
        // even explicit --date=iso is ignored, so we ignore both userDateFormat and GIT_TIMESTAMP_PATTERN here
        Calendar c = Calendar.getInstance( TimeZone.getTimeZone( tz ) );
        c.setTimeInMillis( Long.parseLong( datestring ) * 1000 );
        currentChange.setDate( c.getTime() );

        status = STATUS_RAW_COMMITTER;
    }

    /**
     * Process the current input line in the STATUS_RAW_AUTHOR state.  This
     * state gathers all the committer information of a log entry.
     *
     * @param line a line of text from the git log output
     */
    private void processGetRawCommitter( String line )
    {
        if ( !rawCommitterRegexp.match( line ) )
        {
            return;
        }
        // here we could set committer and committerDate, the same way as in processGetRawAuthor
        status = STATUS_GET_COMMENT;
    }

    /**
     * Process the current input line in the STATUS_GET_DATE state.  This
     * state gathers all of the date information that are part of a log entry.
     *
     * @param line a line of text from the git log output
     */
    private void processGetDate( String line, Locale locale )
    {
        if ( !dateRegexp.match( line ) )
        {
            return;
        }

        String datestring = dateRegexp.getParen( 1 );

        Date date = parseDate( datestring.trim(), userDateFormat, GIT_TIMESTAMP_PATTERN, locale );

        currentChange.setDate( date );

        status = STATUS_GET_COMMENT;
    }

    /**
     * Process the current input line in the GET_COMMENT state.  This
     * state gathers all of the comments that are part of a log entry.
     *
     * @param line a line of text from the git log output
     */
    private void processGetComment( String line )
    {
        if ( line.length() < 4 )
        {
            if ( currentComment == null )
            {
                currentComment = new StringBuilder();
            }
            else
            {
                currentChange.setComment( currentComment.toString() );
                status = STATUS_GET_FILE;
            }
        }
        else
        {
            if ( currentComment.length() > 0 )
            {
                currentComment.append( '\n' );
            }

            currentComment.append( line.substring( 4 ) );
        }
    }

    /**
     * Process the current input line in the GET_FILE state.  This state
     * adds each file entry line to the current change log entry.  Note,
     * the revision number for the entire entry is used for the revision
     * number of each file.
     *
     * @param line A line of text from the git log output
     */
    private void processGetFile( String line )
    {
        if ( line.length() == 0 )
        {
            if ( currentChange != null )
            {
                entries.add( currentChange );
            }

            resetChangeLog();

            status = STATUS_GET_HEADER;
        }
        else
        {
            if ( !fileRegexp.match( line ) )
            {
                return;
            }
            final String actionChar = fileRegexp.getParen( 1 );
            // action is currently not used
            final ScmFileStatus action;
            String name = fileRegexp.getParen( 2 );
            String originalName = null;
            String originalRevision = null;
            if ( "A".equals( actionChar ) )
            {
                action = ScmFileStatus.ADDED;
            }
            else if ( "M".equals( actionChar ) )
            {
                action = ScmFileStatus.MODIFIED;
            }
            else if ( "D".equals( actionChar ) )
            {
                action = ScmFileStatus.DELETED;
            }
            else if ( "R".equals( actionChar ) )
            {
                action = ScmFileStatus.RENAMED;
                originalName = name;
                name = fileRegexp.getParen( 4 );
                originalRevision = currentChange.getParentRevision();
            }
            else if ( "C".equals( actionChar ) )
            {
                action = ScmFileStatus.COPIED;
                originalName = name;
                name = fileRegexp.getParen( 4 );
                originalRevision = currentChange.getParentRevision();
            }
            else
            {
                action = ScmFileStatus.UNKNOWN;
            }

            final ChangeFile changeFile = new ChangeFile( name, currentRevision );
            changeFile.setAction( action );
            changeFile.setOriginalName( originalName );
            changeFile.setOriginalRevision( originalRevision );
            currentChange.addFile( changeFile );
        }
    }

    private void resetChangeLog()
    {
        currentComment = null;
        currentChange = null;
    }
}
