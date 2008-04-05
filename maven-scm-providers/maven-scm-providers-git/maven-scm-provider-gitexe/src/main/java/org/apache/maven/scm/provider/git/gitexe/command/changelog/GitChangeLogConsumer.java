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
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.git.GitChangeSet;
import org.apache.maven.scm.util.AbstractConsumer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitChangeLogConsumer
    extends AbstractConsumer
{
    /**
     * Date formatter for git timestamp
     */
    private static final String GIT_TIMESTAMP_PATTERN = "MMM dd HH:mm:ss yyyy Z";

    /**
     * State machine constant: expecting header
     */
    private static final int STATUS_GET_HEADER = 1;

    /**
     * State machine constant: expecting author information
     */
    private static final int STATUS_GET_AUTHOR = 2;
    
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
     * The pattern used to match git date lines
     */
    private static final String DATE_PATTERN = "^Date:\\s*\\w\\w\\w\\s(.*)";

    /**
     * The pattern used to match git file lines
     */
//X    private static final String FILE_PATTERN = "^:\\d* \\d* [:xdigit:]*\\.* [:xdigit:]*\\.* ([:upper:]) (.*)";
    private static final String FILE_PATTERN = "^:\\d* \\d* [:xdigit:]*\\.* [:xdigit:]*\\.* ([:upper:])\\t(.*)";
    
    /**
     * Current status of the parser
     */
    private int status = STATUS_GET_HEADER;

    /**
     * List of change log entries
     */
    private List entries = new ArrayList();

    /**
     * The current log entry being processed by the parser
     */
    private GitChangeSet currentChange;

    /**
     * The current revision of the entry being processed by the parser
     */
    private String currentRevision;

    /**
     * The current comment of the entry being processed by the parser
     */
    private StringBuffer currentComment;

    /**
     * The regular expression used to match header lines
     */
    private RE headerRegexp;

    /**
     * The regular expression used to match author lines
     */
    private RE authorRegexp;
    
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
            dateRegexp   = new RE( DATE_PATTERN   );
            fileRegexp   = new RE( FILE_PATTERN   );
        }
        catch ( RESyntaxException ex )
        {
            throw new RuntimeException(
                "INTERNAL ERROR: Could not create regexp to parse git log file. This shouldn't happen. Something is probably wrong with the oro installation.",
                ex );
        }
    }

    public List getModifications()
    {
        // this is needed since the processFile does not always get a the end-sequence correctly. 
        processGetFile( "" );
        
        return entries;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

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
                processGetDate( line );
                break;
            case STATUS_GET_COMMENT:
                processGetComment( line );
                break;
            case STATUS_GET_FILE:
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
     * Process the current input line in the GET_HEADER state.  The
     * author, date, and the revision of the entry are gathered.  Note,
     * Subversion does not have per-file revisions, instead, the entire
     * repository is given a single revision number, which is used for
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

        currentChange = new GitChangeSet();
        
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
        if ( !authorRegexp.match( line ) )
        {
            return;
        }
        String author = authorRegexp.getParen( 1 );
        
        currentChange.setAuthor( author );
        
        status = STATUS_GET_DATE;
    }

    /**
     * Process the current input line in the STATUS_GET_DATE state.  This
     * state gathers all of the date information that are part of a log entry.
     *
     * @param line a line of text from the git log output
     */
    private void processGetDate( String line )
    {
        if ( !dateRegexp.match( line ) )
        {
            return;
        }
        
        String datestring = dateRegexp.getParen( 1 );
        
        Date date = parseDate( datestring.trim() , userDateFormat, GIT_TIMESTAMP_PATTERN );
        
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
            if (currentComment == null)
            {
                currentComment = new StringBuffer();
            }
            else
            {
                currentChange.setComment( currentComment.toString() );
                status = STATUS_GET_FILE;
            }
        }
        else 
        {
            if ( currentComment.length() > 0 ) {
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
            // String action = fileRegexp.getParen( 1 );
            // action is currently not used
            
            String name = fileRegexp.getParen( 2 );
            
            currentChange.addFile( new ChangeFile( name, currentRevision ) );
        }
    }

    private void resetChangeLog() {
    	currentComment = null;
    	currentChange = null;
    }
}
