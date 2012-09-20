package org.apache.maven.scm.provider.starteam.command.changelog;

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
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.util.AbstractConsumer;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 *
 */
public class StarteamChangeLogConsumer
    extends AbstractConsumer
{
    private SimpleDateFormat localFormat = new SimpleDateFormat( "", Locale.getDefault() );

    private List<ChangeSet> entries = new ArrayList<ChangeSet>();

    private String workingDirectory;

    private String currentDir = "";

    // state machine constants for reading Starteam output

    /**
     * expecting file information
     */
    private static final int GET_FILE = 1;

    /**
     * expecting date
     */
    private static final int GET_AUTHOR = 2;

    /**
     * expecting comments
     */
    private static final int GET_COMMENT = 3;

    /**
     * expecting revision
     */
    private static final int GET_REVISION = 4;


    /**
     * Marks current directory data
     */
    private static final String DIR_MARKER = "(working dir: ";

    /**
     * Marks start of file data
     */
    private static final String START_FILE = "History for: ";


    /**
     * Marks end of file
     */
    private static final String END_FILE =
        "===================================" + "==========================================";

    /**
     * Marks start of revision
     */
    private static final String START_REVISION = "----------------------------";

    /**
     * Marks revision data
     */
    private static final String REVISION_TAG = "Branch Revision: ";

    /**
     * Marks author data
     */
    private static final String AUTHOR_TAG = "Author: ";

    /**
     * Marks date data
     */
    private static final String DATE_TAG = " Date: ";

    /**
     * current status of the parser
     */
    private int status = GET_FILE;

    /**
     * the current log entry being processed by the parser
     */
    private ChangeSet currentChange = null;

    /**
     * the current file being processed by the parser
     */
    private ChangeFile currentFile = null;

    /**
     * the before date
     */
    private Date startDate;

    /**
     * the to date
     */
    private Date endDate;

    private String userDateFormat;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public StarteamChangeLogConsumer( File workingDirectory, ScmLogger logger, Date startDate, Date endDate,
                                      String userDateFormat )
    {
        super( logger );

        this.workingDirectory = workingDirectory.getPath().replace( '\\', '/' );

        this.startDate = startDate;

        this.endDate = endDate;

        this.userDateFormat = userDateFormat;

        //work around for all en_US compatible locales, where Starteam
        // stcmd hist output uses a different format, ugly eh?
        // makesure to change the test file as well if this ever got fixed

        if ( "M/d/yy h:mm a".equals( localFormat.toLocalizedPattern() ) )
        {
            this.localFormat = new SimpleDateFormat( "M/d/yy h:mm:ss a z" );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List<ChangeSet> getModifications()
    {
        return entries;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( line );
        }

        int pos = 0;

        if ( ( pos = line.indexOf( DIR_MARKER ) ) != -1 )
        {
            processDirectory( line, pos );
            return;
        }

        // current state transitions in the state machine - starts with Get File
        //      Get File                -> Get Revision
        //      Get Revision            -> Get Date or Get File
        //      Get Date                -> Get Comment
        //      Get Comment             -> Get Comment or Get Revision
        switch ( getStatus() )
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
            case GET_COMMENT:
                processGetComment( line );
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
     *              with the same key doesn't exist already. If the entry's author
     *              is null, the entry wont be added
     * @param file  a {@link ChangeFile} to be added to the entry
     */
    private void addEntry( ChangeSet entry, ChangeFile file )
    {
        // do not add if entry is not populated
        if ( entry.getAuthor() == null )
        {
            return;
        }

        // do not add if entry is out of date range
        if ( startDate != null && entry.getDate().before( startDate ) )
        {
            return;
        }

        if ( endDate != null && entry.getDate().after( endDate ) )
        {
            return;
        }

        entry.addFile( file );

        entries.add( entry );
    }

    private void processDirectory( String line, int pos )
    {
        String dirPath = line.substring( pos + DIR_MARKER.length(), line.length() - 1 ).replace( '\\', '/' );
        try
        {
            this.currentDir = StarteamCommandLineUtils.getRelativeChildDirectory( this.workingDirectory, dirPath );
        }
        catch ( IllegalStateException e )
        {
            String error = "Working and checkout directories are not on the same tree";

            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( error );

                getLogger().error( "Working directory: " + workingDirectory );

                getLogger().error( "Checked out directory: " + dirPath );
            }

            throw new IllegalStateException( error );
        }
    }

    /**
     * Process the current input line in the Get File state.
     *
     * @param line a line of text from the Starteam log output
     */
    private void processGetFile( String line )
    {
        if ( line.startsWith( START_FILE ) )
        {
            setCurrentChange( new ChangeSet() );

            setCurrentFile(
                new ChangeFile( this.currentDir + "/" + line.substring( START_FILE.length(), line.length() ) ) );

            setStatus( GET_REVISION );
        }
    }

    /**
     * Process the current input line in the Get Revision state.
     *
     * @param line a line of text from the Starteam log output
     */
    private void processGetRevision( String line )
    {
        int pos;

        if ( ( pos = line.indexOf( REVISION_TAG ) ) != -1 )
        {
            getCurrentFile().setRevision( line.substring( pos + REVISION_TAG.length() ) );

            setStatus( GET_AUTHOR );
        }
        else if ( line.startsWith( END_FILE ) )
        {
            // If we encounter an end of file line, it means there
            // are no more revisions for the current file.
            // there could also be a file still being processed.
            setStatus( GET_FILE );

            addEntry( getCurrentChange(), getCurrentFile() );
        }
    }

    /**
     * Process the current input line in the Get Author/Date state.
     *
     * @param line a line of text from the Starteam log output
     */
    private void processGetAuthor( String line )
    {
        if ( line.startsWith( AUTHOR_TAG ) )
        {
            int posDateTag = line.indexOf( DATE_TAG );

            String author = line.substring( AUTHOR_TAG.length(), posDateTag );

            getCurrentChange().setAuthor( author );

            String date = line.substring( posDateTag + DATE_TAG.length() );

            Date dateObj = parseDate( date, userDateFormat, localFormat.toPattern() );

            if ( dateObj != null )
            {
                getCurrentChange().setDate( dateObj );
            }
            else
            {
                getCurrentChange().setDate( date, userDateFormat );
            }

            setStatus( GET_COMMENT );
        }
    }

    /**
     * Process the current input line in the Get Comment state.
     *
     * @param line a line of text from the Starteam log output
     */
    private void processGetComment( String line )
    {
        if ( line.startsWith( START_REVISION ) )
        {
            // add entry, and set state to get revision
            addEntry( getCurrentChange(), getCurrentFile() );

            // new change log entry
            setCurrentChange( new ChangeSet() );

            // same file name, but different rev
            setCurrentFile( new ChangeFile( getCurrentFile().getName() ) );

            setStatus( GET_REVISION );
        }
        else if ( line.startsWith( END_FILE ) )
        {
            addEntry( getCurrentChange(), getCurrentFile() );

            setStatus( GET_FILE );
        }
        else
        {
            // keep gathering comments
            getCurrentChange().setComment( getCurrentChange().getComment() + line + "\n" );
        }
    }

    /**
     * Getter for property currentFile.
     *
     * @return Value of property currentFile.
     */
    private ChangeFile getCurrentFile()
    {
        return currentFile;
    }

    /**
     * Setter for property currentFile.
     *
     * @param currentFile New value of property currentFile.
     */
    private void setCurrentFile( ChangeFile currentFile )
    {
        this.currentFile = currentFile;
    }

    /**
     * Getter for property currentChange.
     *
     * @return Value of property currentChange.
     */
    private ChangeSet getCurrentChange()
    {
        return currentChange;
    }

    /**
     * Setter for property currentChange.
     *
     * @param currentChange New value of property currentChange.
     */
    private void setCurrentChange( ChangeSet currentChange )
    {
        this.currentChange = currentChange;
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
