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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class PerforceChangeLogConsumer
    extends AbstractConsumer
{
    /**
     * Date formatter for perforce timestamp
     */
    private static final String PERFORCE_TIMESTAMP_PATTERN = "yyyy/MM/dd HH:mm:ss";

    private List<ChangeSet> entries = new ArrayList<ChangeSet>();

    /**
     * State machine constant: expecting revision and/or file information
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
     * The comment section ends with a blank line
     */
    private static final String COMMENT_DELIMITER = "";

    /**
     * A file line begins with two slashes
     */
    private static final String FILE_BEGIN_TOKEN = "//";

    /**
     * Current status of the parser
     */
    private int status = GET_REVISION;

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

    /**
     * The regular expression used to match header lines
     */
    private RE revisionRegexp;

    private Date startDate;

    private Date endDate;

    private String userDatePattern;

    private static final String PATTERN = "^\\.\\.\\. #(\\d+) " + // revision number
        "change (\\d+) .* " + // changelist number
        "on (.*) " + // date
        "by (.*)@"; // author

    public PerforceChangeLogConsumer( String path, Date startDate, Date endDate, String userDatePattern,
                                      ScmLogger logger )
    {
        super( logger );

        this.startDate = startDate;
        this.endDate = endDate;
        this.userDatePattern = userDatePattern;
        this.repoPath = path;

        try
        {
            revisionRegexp = new RE( PATTERN );
        }
        catch ( RESyntaxException ignored )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "Could not create regexp to parse perforce log file", ignored );
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List<ChangeSet> getModifications() throws ScmException
    {
    	
    	// Here there are one entry for each couple (changelist,file). We merge
    	// entries to have only one entry per changelist
    	
    	// Date > ChangeSet
        Map<Date,ChangeSet> groupedEntries = new LinkedHashMap<Date,ChangeSet>();
        for ( int i = 0; i < entries.size(); i++ )
        {
            ChangeSet cs = (ChangeSet) entries.get( i );
            ChangeSet hit = (ChangeSet) groupedEntries.get( cs.getDate() );
            if ( hit != null )
            {
                if ( cs.getFiles().size() != 1 )
                {
                    throw new ScmException( "Merge of entries failed. Bad entry size: " + cs.getFiles().size() );
                }
                hit.addFile( (ChangeFile) cs.getFiles().get( 0 ) );
            }
            else
            {
                groupedEntries.put( cs.getDate(), cs );
            }
        }

        List<ChangeSet> result = new ArrayList<ChangeSet>();
        result.addAll( groupedEntries.values() );

        return result;
    	
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
        // ----------------------------------------------------------------------
        // Check that we are inside the requested date range
        // ----------------------------------------------------------------------

        if ( startDate != null && entry.getDate().before( startDate ) )
        {
            return;
        }

        if ( endDate != null && entry.getDate().after( endDate ) )
        {
            return;
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        entry.addFile( file );

        entries.add( entry );
    }

    /**
     * Most of the relevant info is on the revision line matching the
     * 'pattern' string.
     *
     * @param line A line of text from the perforce log output
     */
    private void processGetRevision( String line )
    {
        if ( line.startsWith( FILE_BEGIN_TOKEN ) )
        {
            currentFile = line.substring( repoPath.length() + 1 );
            return;
        }

        if ( !revisionRegexp.match( line ) )
        {
            return;
        }

        currentChange = new ChangeSet();
        currentChange.setDate( parseDate( revisionRegexp.getParen( 3 ), userDatePattern, PERFORCE_TIMESTAMP_PATTERN ) );
        currentChange.setAuthor( revisionRegexp.getParen( 4 ) );

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
            addEntry( currentChange, new ChangeFile( currentFile, revisionRegexp.getParen( 1 ) ) );

            status = GET_REVISION;
        }
        else
        {
            currentChange.setComment( currentChange.getComment() + line + "\n" );
        }
    }
}
