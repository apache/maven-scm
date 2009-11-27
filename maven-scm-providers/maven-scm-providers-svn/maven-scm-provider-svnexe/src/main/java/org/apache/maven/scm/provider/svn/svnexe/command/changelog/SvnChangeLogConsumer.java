package org.apache.maven.scm.provider.svn.svnexe.command.changelog;

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
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.svn.SvnChangeSet;
import org.apache.maven.scm.util.AbstractConsumer;
import org.apache.regexp.RE;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeLogConsumer
    extends AbstractConsumer
{
    /**
     * Date formatter for svn timestamp (after a little massaging)
     */
    private static final String SVN_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss zzzzzzzzz";

    /**
     * State machine constant: expecting header
     */
    private static final int GET_HEADER = 1;

    /**
     * State machine constant: expecting file information
     */
    private static final int GET_FILE = 2;

    /**
     * State machine constant: expecting comments
     */
    private static final int GET_COMMENT = 3;

    /**
     * A file line begins with a space character
     */
    private static final String FILE_BEGIN_TOKEN = " ";

    /**
     * The file section ends with a blank line
     */
    private static final String FILE_END_TOKEN = "";

    /**
     * The filename starts after 5 characters
     */
    private static final int FILE_START_INDEX = 5;

    /**
     * The comment section ends with a dashed line
     */
    private static final String COMMENT_END_TOKEN =
        "------------------------------------" + "------------------------------------";

    /**
     * Current status of the parser
     */
    private int status = GET_HEADER;

    /**
     * List of change log entries
     */
    private List entries = new ArrayList();

    /**
     * The current log entry being processed by the parser
     */
    private SvnChangeSet currentChange;

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
    private static final RE HEADER_REG_EXP =
        new RE("^(.+) \\| (.+) \\| (.+) \\|.*$");
    
    private static final int REVISION_GROUP = 1;
    private static final int AUTHOR_GROUP = 2;
    private static final int DATE_GROUP = 3;
    
    private static final RE REVISION_REG_EXP1 = new RE("rev (\\d+):");
    
    private static final RE REVISION_REG_EXP2 = new RE("r(\\d+)");
    
    private static final RE DATE_REG_EXP = new RE(
        "(\\d+-\\d+-\\d+ " +             // date 2002-08-24
        "\\d+:\\d+:\\d+) " +             // time 16:01:00
        "([\\-+])(\\d\\d)(\\d\\d)");     // gmt offset -0400);)

    private final String userDateFormat;

    /**
     * Default constructor.
     */
    public SvnChangeLogConsumer( ScmLogger logger, String userDateFormat )
    {
        super( logger );

        this.userDateFormat = userDateFormat;
    }

    public List getModifications()
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
        switch ( status )
        {
            case GET_HEADER:
                processGetHeader( line );
                break;
            case GET_FILE:
                processGetFile( line );
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
     * Process the current input line in the GET_HEADER state.  The
     * author, date, and the revision of the entry are gathered.  Note,
     * Subversion does not have per-file revisions, instead, the entire
     * repository is given a single revision number, which is used for
     * the revision number of each file.
     *
     * @param line A line of text from the svn log output
     */
    private void processGetHeader( String line )
    {
        if ( !HEADER_REG_EXP.match(line) )
        {
            // The header line is not found. Intentionally do nothing.
            return;
        }

        currentRevision = getRevision(HEADER_REG_EXP.getParen(
                REVISION_GROUP ));

        currentChange = new SvnChangeSet();

        currentChange.setAuthor( HEADER_REG_EXP.getParen( AUTHOR_GROUP ) );

        currentChange.setDate( getDate(HEADER_REG_EXP.getParen( DATE_GROUP )) );

        status = GET_FILE;
    }
    
    /**
     * Gets the svn revision, from the svn log revision output.
     * 
     * @param revisionOutput
     * @return the svn revision
     */
    private String getRevision(final String revisionOutput)
    {
        if (REVISION_REG_EXP1.match(revisionOutput))
        {
            return REVISION_REG_EXP1.getParen(1);
        }
        else if (REVISION_REG_EXP2.match(revisionOutput))
        {
            return REVISION_REG_EXP2.getParen(1);
        }
        else
        {
          throw new IllegalOutputException(revisionOutput);
        }
    }

    /**
     * Process the current input line in the GET_FILE state.  This state
     * adds each file entry line to the current change log entry.  Note,
     * the revision number for the entire entry is used for the revision
     * number of each file.
     *
     * @param line A line of text from the svn log output
     */
    private void processGetFile( String line )
    {
        if ( line.startsWith( FILE_BEGIN_TOKEN ) )
        {
            // Skip the status flags and just get the name of the file
            String name = line.substring( FILE_START_INDEX );
            currentChange.addFile( new ChangeFile( name, currentRevision ) );

            status = GET_FILE;
        }
        else if ( line.equals( FILE_END_TOKEN ) )
        {
            // Create a buffer for the collection of the comment now
            // that we are leaving the GET_FILE state.
            currentComment = new StringBuffer();

            status = GET_COMMENT;
        }
    }

    /**
     * Process the current input line in the GET_COMMENT state.  This
     * state gathers all of the comments that are part of a log entry.
     *
     * @param line a line of text from the svn log output
     */
    private void processGetComment( String line )
    {
        if ( line.equals( COMMENT_END_TOKEN ) )
        {
            currentChange.setComment( currentComment.toString() );

            entries.add( currentChange );

            status = GET_HEADER;
        }
        else
        {
            currentComment.append( line ).append( '\n' );
        }
    }

    /**
     * Converts the date time stamp from the svn output into a date
     * object.
     * 
     * @param dateOutput The date output from an svn log command.
     * @return A date representing the time stamp of the log entry.
     */
    private Date getDate(final String dateOutput)
    {
        if (!DATE_REG_EXP.match(dateOutput))
        {
            throw new IllegalOutputException(dateOutput);
        }
      
        final StringBuffer date = new StringBuffer();
        date.append(DATE_REG_EXP.getParen( 1 ));
        date.append(" GMT");
        date.append(DATE_REG_EXP.getParen( 2 ));
        date.append(DATE_REG_EXP.getParen( 3 ));
        date.append(':');
        date.append(DATE_REG_EXP.getParen( 4 ));

        return parseDate( date.toString(), userDateFormat, SVN_TIMESTAMP_PATTERN );
    }
}
