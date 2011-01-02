package org.apache.maven.scm.provider.vss.commands.changelog;

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
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.apache.maven.scm.util.AbstractConsumer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class VssChangeLogConsumer
    extends AbstractConsumer
{
    /**
     * Custom date/time formatter. Rounds ChangeLogEntry times to the nearest
     * minute.
     */
    private static final SimpleDateFormat ENTRY_KEY_TIMESTAMP_FORMAT = new SimpleDateFormat( "yyyyMMddHHmm" );

    // state machine constants for reading Starteam output

    /**
     * expecting file information
     */
    private static final int GET_FILE = 1;

    /**
     * expecting file path information
     */
    private static final int GET_FILE_PATH = 2;

    /**
     * expecting date
     */
    private static final int GET_AUTHOR = 3;

    /**
     * expecting comments
     */
    private static final int GET_COMMENT = 4;

    /**
     * expecting revision
     */
    private static final int GET_REVISION = 5;

    /**
     * unknown vss history line status
     */
    private static final int GET_UNKNOWN = 6;

    /**
     * Marks start of file data
     */
    private static final String START_FILE = "*****  ";

    /**
     * Marks start of file data
     */
    private static final String START_FILE_PATH = "$/";

    /**
     * Marks start of revision
     */
    private static final String START_REVISION = "Version";

    /**
     * Marks author data
     */
    private static final String START_AUTHOR = "User: ";

    /**
     * Marks comment data
     */
    private static final String START_COMMENT = "Comment: ";

    /**
     * rcs entries, in reverse (date, time, author, comment) order
     */
    private Map<String, ChangeSet> entries = new TreeMap<String, ChangeSet>( Collections.reverseOrder() );

    private ChangeFile currentFile;

    private ChangeSet currentChangeSet;

    /**
     * last status of the parser
     */
    private int lastStatus = GET_FILE;

    private VssScmProviderRepository repo;

    private String userDatePattern;

    public VssChangeLogConsumer( VssScmProviderRepository repo, String userDatePattern, ScmLogger logger )
    {
        super( logger );
        this.userDatePattern = userDatePattern;
        this.repo = repo;
    }

    public List<ChangeSet> getModifications()
    {
        return new ArrayList<ChangeSet>( entries.values() );
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        switch ( getLineStatus( line ) )
        {
            case GET_FILE:
                processGetFile( line );
                break;
            case GET_REVISION:
                processGetRevision( line );
                break;
            case GET_AUTHOR:
                processGetAuthor( line );
                break;
            case GET_FILE_PATH:
                processGetFilePath( line );
                break;
            case GET_COMMENT:
                processGetComment( line );
                break;
            default:
                break;
        }
    }

    /**
     * Process the current input line in the Get Comment state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processGetComment( String line )
    {
        String[] commentLine = line.split( ":" );
        if ( commentLine.length == 2 )
        {
            currentChangeSet.setComment( commentLine[1] );
        }
        //Comment suite on a new line
        else
        {
            String comment = currentChangeSet.getComment();
            comment = comment + " " + line;
            currentChangeSet.setComment( comment );
        }
    }

    /**
     * Process the current input line in the Get Author state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processGetAuthor( String line )
    {
        String[] result = line.split( "\\s" );
        Vector<String> vector = new Vector<String>();
        for ( int i = 0; i < result.length; i++ )
        {
            if ( !result[i].equals( "" ) )
            {
                vector.add( result[i] );
            }
        }
        currentChangeSet.setAuthor( vector.get( 1 ) );
        currentChangeSet.setDate(
            parseDate( vector.get( 3 ) + " " + vector.get( 5 ), userDatePattern, "dd.MM.yy HH:mm" ) );
    }

    /**
     * Process the current input line in the Get File state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processGetFile( String line )
    {
        currentChangeSet = ( new ChangeSet() );
        String[] fileLine = line.split( " " );
        currentFile = new ChangeFile( fileLine[2] );
    }

    /**
     * Process the current input line in the Get File Path state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processGetFilePath( String line )
    {
        if ( currentFile != null )
        {
            String fileName = currentFile.getName();

            String path = line.substring( line.indexOf( "$" ), line.length() );
            String longPath = path.substring( repo.getProject()
                .length() + 1, path.length() ) + "/" + fileName;
            currentFile.setName( longPath );
            addEntry( currentChangeSet, currentFile );
        }
    }

    /**
     * Process the current input line in the Get Revision state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processGetRevision( String line )
    {
        String[] revisionLine = line.split( " " );
        currentFile.setRevision( revisionLine[1] );
    }

    /**
     * Identify the status of a vss history line
     *
     * @param line The line to process
     * @return status
     */
    private int getLineStatus( String line )
    {
        int argument = GET_UNKNOWN;
        if ( line.startsWith( START_FILE ) )
        {
            argument = GET_FILE;
        }
        else if ( line.startsWith( START_REVISION ) )
        {
            argument = GET_REVISION;
        }
        else if ( line.startsWith( START_AUTHOR ) )
        {
            argument = GET_AUTHOR;
        }
        else if ( line.indexOf( START_FILE_PATH ) != -1 )
        {
            argument = GET_FILE_PATH;
        }
        else if ( line.startsWith( START_COMMENT ) )
        {
            argument = GET_COMMENT;
        }
        else if ( lastStatus == GET_COMMENT )
        {
            //Comment suite on a new line
            argument = lastStatus;
        }
        lastStatus = argument;

        return argument;
    }

    /**
     * Add a change log entry to the list (if it's not already there) with the
     * given file.
     *
     * @param entry a {@link ChangeSet}to be added to the list if another
     *              with the same key doesn't exist already. If the entry's author
     *              is null, the entry wont be added
     * @param file  a {@link ChangeFile}to be added to the entry
     */
    private void addEntry( ChangeSet entry, ChangeFile file )
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
            ChangeSet existingEntry = (ChangeSet) entries.get( key );
            existingEntry.addFile( file );
        }
    }
}
