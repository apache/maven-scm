package org.apache.maven.scm.provider.cvslib.command.changelog;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.maven.scm.command.changelog.ChangeLogConsumer;
import org.apache.maven.scm.command.changelog.ChangeLogEntry;
import org.apache.maven.scm.command.changelog.ChangeLogFile;
import org.codehaus.plexus.logging.Logger;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsChangeLogConsumer
    implements ChangeLogConsumer
{
    /**
     * Custom date/time formatter. Rounds ChangeLogEntry times to the nearest
     * minute.
     */
    private static final SimpleDateFormat ENTRY_KEY_TIMESTAMP_FORMAT = new SimpleDateFormat( "yyyyMMddHHmm" );

    /**
     * rcs entries, in reverse (date, time, author, comment) order
     */
    private Map entries = new TreeMap( Collections.reverseOrder() );

    // state machine constants for reading cvs output
    /** expecting file information */
    private static final int GET_FILE = 1;

    /** expecting date */
    private static final int GET_DATE = 2;

    /** expecting comments */
    private static final int GET_COMMENT = 3;

    /** expecting revision */
    private static final int GET_REVISION = 4;

    /** Marks start of file data */
    private static final String START_FILE = "Working file: ";

    /** Marks end of file */
    private static final String END_FILE = "==================================="
        + "==========================================";

    /** Marks start of revision */
    private static final String START_REVISION = "----------------------------";

    /** Marks revision data */
    private static final String REVISION_TAG = "revision ";

    /** Marks date data */
    private static final String DATE_TAG = "date: ";

    /** current status of the parser */
    private int status = GET_FILE;

    /** the current log entry being processed by the parser */
    private ChangeLogEntry currentLogEntry = null;

    /** the current file being processed by the parser */
    private ChangeLogFile currentFile = null;
    private Logger logger;

    public CvsChangeLogConsumer( Logger logger )
    {
        this.logger = logger;
    }

    public List getModifications()
    {
        return new ArrayList( entries.values() );
    }

    public void consumeLine( String line )
    {

        try
        {
            switch ( getStatus() )
            {
            case GET_FILE:
                processGetFile( line );
                break;
            case GET_REVISION:
                processGetRevision( line );
                break;
            case GET_DATE:
                processGetDate( line );
                break;
            case GET_COMMENT:
                processGetComment( line );
                break;
            default:
                throw new IllegalStateException( "Unknown state: " + status );
            }
        }
        catch( Throwable ex )
        {
            logger.warn( "Exception in the cvs changelog consumer.", ex );
        }
    }

    /**
     * Add a change log entry to the list (if it's not already there) with the
     * given file.
     *
     * @param entry a {@link ChangeLogEntry}to be added to the list if another
     *              with the same key doesn't exist already. If the entry's author
     *              is null, the entry wont be added
     * @param file  a {@link ChangeLogFile}to be added to the entry
     */
    private void addEntry( ChangeLogEntry entry, ChangeLogFile file )
    {
        // do not add if entry is not populated
        if ( entry.getAuthor() == null )
        {
            return;
        }

        String key = ENTRY_KEY_TIMESTAMP_FORMAT.format( entry.getDate() ) + entry.getAuthor() + entry.getComment();

        if ( !entries.containsKey( key ) )
        {
            entry.addFile( file );
            entries.put( key, entry );
        }
        else
        {
            ChangeLogEntry existingEntry = (ChangeLogEntry) entries.get( key );
            existingEntry.addFile( file );
        }
    }

    /**
     * Process the current input line in the Get File state.
     *
     * @param line a line of text from the cvs log output
     */
    private void processGetFile( String line )
    {
        if ( line.startsWith( START_FILE ) )
        {
            setCurrentLogEntry( new ChangeLogEntry() );
            setCurrentFile( new ChangeLogFile( line.substring( START_FILE.length(), line.length() ) ) );
            setStatus( GET_REVISION );
        }
    }

    /**
     * Process the current input line in the Get Revision state.
     *
     * @param line a line of text from the cvs log output
     */
    private void processGetRevision( String line )
    {
        if ( line.startsWith( REVISION_TAG ) )
        {
            getCurrentFile().setRevision( line.substring( REVISION_TAG.length() ) );
            setStatus( GET_DATE );
        }
        else if ( line.startsWith( END_FILE ) )
        {
            // If we encounter an end of file line, it means there
            // are no more revisions for the current file.
            // there could also be a file still being processed.
            setStatus( GET_FILE );
            addEntry( getCurrentLogEntry(), getCurrentFile() );
        }
    }

    /**
     * Process the current input line in the Get Date state.
     *
     * @param line a line of text from the cvs log output
     */
    private void processGetDate( String line )
    {
        if ( line.startsWith( DATE_TAG ) )
        {
            StringTokenizer tokenizer = new StringTokenizer( line, " ;" );
            // date: YYYY/mm/dd HH:mm:ss; author: name
            tokenizer.nextToken(); // date tag
            String date = tokenizer.nextToken();
            String time = tokenizer.nextToken();
            getCurrentLogEntry().setDate( date + " " + time );
            tokenizer.nextToken(); // author tag
            // assumes author can't contain spaces
            String author = tokenizer.nextToken();
            getCurrentLogEntry().setAuthor( author );
            setStatus( GET_COMMENT );
        }
    }

    /**
     * Process the current input line in the Get Comment state.
     *
     * @param line a line of text from the cvs log output
     */
    private void processGetComment( String line )
    {
        if ( line.startsWith( START_REVISION ) )
        {
            // add entry, and set state to get revision
            addEntry( getCurrentLogEntry(), getCurrentFile() );
            // new change log entry
            setCurrentLogEntry( new ChangeLogEntry() );
            // same file name, but different rev
            setCurrentFile( new ChangeLogFile( getCurrentFile().getName() ) );
            setStatus( GET_REVISION );
        }
        else if ( line.startsWith( END_FILE ) )
        {
            addEntry( getCurrentLogEntry(), getCurrentFile() );
            setStatus( GET_FILE );
        }
        else
        {
            // keep gathering comments
            getCurrentLogEntry().setComment( getCurrentLogEntry().getComment() + line + "\n" );
        }
    }

    /**
     * Getter for property currentFile.
     *
     * @return Value of property currentFile.
     */
    private ChangeLogFile getCurrentFile()
    {
        return currentFile;
    }

    /**
     * Setter for property currentFile.
     *
     * @param currentFile New value of property currentFile.
     */
    private void setCurrentFile( ChangeLogFile currentFile )
    {
        this.currentFile = currentFile;
    }

    /**
     * Getter for property currentLogEntry.
     *
     * @return Value of property currentLogEntry.
     */
    private ChangeLogEntry getCurrentLogEntry()
    {
        return currentLogEntry;
    }

    /**
     * Setter for property currentLogEntry.
     *
     * @param currentLogEntry New value of property currentLogEntry.
     */
    private void setCurrentLogEntry( ChangeLogEntry currentLogEntry )
    {
        this.currentLogEntry = currentLogEntry;
    }

    /**
     * Getter for property status.
     *
     * @return Value of property status.
     */
    private int getStatus()
    {
        return status;
    }

    /**
     * Setter for property status.
     *
     * @param status New value of property status.
     */
    private void setStatus( int status )
    {
        this.status = status;
    }
}
