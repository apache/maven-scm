package org.apache.maven.scm.provider.perforce.command.changelog;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * ====================================================================
 */

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.scm.command.changelog.ChangeLogConsumer;
import org.apache.maven.scm.command.changelog.ChangeLogEntry;
import org.apache.maven.scm.command.changelog.ChangeLogFile;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class PerforceChangeLogConsumer implements ChangeLogConsumer
{
    /** Date formatter for perforce timestamp */
    private static final SimpleDateFormat PERFORCE_TIMESTAMP =
        new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    /**
     * RCS entries, in reverse changelist number order
     */
    private Map entries = new TreeMap(Collections.reverseOrder());

    /** State machine constant: expecting revision and/or file information */
    private static final int GET_REVISION = 1;

    /** State machine constant: eat the first blank line */
    private static final int GET_COMMENT_BEGIN = 2;

    /** State machine constant: expecting comments */
    private static final int GET_COMMENT = 3;

    /** The comment section ends with a blank line */
    private static final String COMMENT_DELIMITER = "";

    /** A file line begins with two slashes */
    private static final String FILE_BEGIN_TOKEN = "//";

    /** Current status of the parser */
    private int status = GET_REVISION;
    
    /** The current log entry being processed by the parser */
    private ChangeLogEntry currentLogEntry;

    /** the current file being processed by the parser */
    private String currentFile;
    
    /** The regular expression used to match header lines */
    private RE revisionRegexp;

    /** the before date */
    private Date beforeDate;

    /** the to date */
    private Date toDate;

    private static final String pattern =
        "^\\.\\.\\. #(\\d+) " +           // revision number
        "change (\\d+) .* " +             // changelist number
        "on (.*) " +                      // date 
        "by (.*)@";                       // author

    /** 
     * Default constructor.
     */
    public PerforceChangeLogConsumer()
    {
        try
        {
            revisionRegexp = new RE(pattern);
        }
        catch (RESyntaxException ignored) {
            //LOG.error("Could not create regexp to parse perforce log file", ignored);
        }
    }
    
    /* (non-Javadoc)
     * @see org.apache.maven.scm.command.changelog.ChangeLogConsumer#getModifications()
     */
    public Collection getModifications()
    {
        return entries.values();
    }

    /* (non-Javadoc)
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine(String line)
    {
        switch (status)
        {
            case GET_REVISION:
                processGetRevision(line);
                break;
            case GET_COMMENT_BEGIN:
                status = GET_COMMENT;
                break;
            case GET_COMMENT:
                processGetComment(line);
                break;
            default:
                throw new IllegalStateException("Unknown state: " + status);
        }
    }
    
    /**
     * Add a change log entry to the list (if it's not already there)
     * with the given file.
     * @param entry a {@link ChangeLogEntry} to be added to the list if another
     *      with the same key (p4 change number) doesn't exist already.
     * @param file a {@link ChangeLogFile} to be added to the entry
     */
    private void addEntry(ChangeLogEntry entry, ChangeLogFile file)
    {
        System.out.println();
        System.out.println(entry.toXML());
        if (beforeDate != null) {
            if (entry.getDate().before(beforeDate) || entry.getDate().after(toDate)) {
                return;
            }
        }
        Integer key = new Integer(revisionRegexp.getParen(2));
        if (!entries.containsKey(key)) {
            entry.addFile(file);
            entries.put(key, entry);
        } else {
            ChangeLogEntry existingEntry = (ChangeLogEntry) entries.get(key);
            existingEntry.addFile(file);
        }
    }
 
    /**
     * Most of the relevant info is on the revision line matching the
     * 'pattern' string.
     *
     * @param line A line of text from the perforce log output
     */
    private void processGetRevision(String line) 
    {
        if (line.startsWith(FILE_BEGIN_TOKEN)) {
            currentFile = line;
            return;
        }
        
        if (!revisionRegexp.match(line)) {
            return;
        }

        currentLogEntry = new ChangeLogEntry();
        currentLogEntry.setDate(parseDate(revisionRegexp.getParen(3)));
        currentLogEntry.setAuthor(revisionRegexp.getParen(4));

        status = GET_COMMENT_BEGIN;
    }

    /**
     * Process the current input line in the GET_COMMENT state.  This
     * state gathers all of the comments that are part of a log entry.
     *
     * @param line a line of text from the perforce log output
     */
    private void processGetComment(String line)
    {
        if (line.equals(COMMENT_DELIMITER)) {
            addEntry(currentLogEntry, new ChangeLogFile(currentFile, revisionRegexp.getParen(1)));
            status = GET_REVISION;
        } else {
            currentLogEntry.setComment(currentLogEntry.getComment() + line + "\n");
        }
    }

    /** 
     * Converts the date timestamp from the perforce output into a date
     * object.
     * 
     * @return A date representing the timestamp of the log entry.
     */
    private Date parseDate(String date)
    {
        try
        {
            return PERFORCE_TIMESTAMP.parse(date);
        }
        catch (ParseException e) {
            //LOG.error("ParseException Caught", e);
            return null;        
        }
    }
    
    /**
     * Set the beforeDate and toDate member based on the number of days
     * obtained from the ChangeLog.
     *
     * @param numDays The number of days of log output to
     * generate.
     */
    public void setDateRange(int numDays)
    {
        beforeDate = new Date(
            System.currentTimeMillis() - (long) numDays * 24 * 60 * 60 * 1000);
        toDate = new Date(
            System.currentTimeMillis() + (long) 1 * 24 * 60 * 60 * 1000);
    }
    
    public void setStartDate(Date startDate)
    {
        beforeDate = startDate;
    }
    
    public Date getStartDate()
    {
        return beforeDate;
    }
    
    public void setEndDate(Date endDate)
    {
        toDate = endDate;
    }
    
    public Date getEndDate()
    {
        return toDate;
    }
}
