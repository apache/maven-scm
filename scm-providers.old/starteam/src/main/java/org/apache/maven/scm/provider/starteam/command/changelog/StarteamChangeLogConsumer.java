package org.apache.maven.scm.provider.starteam.command.changelog;

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

import org.apache.maven.scm.command.changelog.ChangeLogConsumer;
import org.apache.maven.scm.command.changelog.ChangeLogEntry;
import org.apache.maven.scm.command.changelog.ChangeLogFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamChangeLogConsumer implements ChangeLogConsumer
{
    /**
     * Custom date/time formatter.  Rounds ChangeLogEntry times to the nearest
     * minute.
     */
    private static final SimpleDateFormat ENTRY_KEY_TIMESTAMP_FORMAT = 
        new SimpleDateFormat("yyyyMMddHHmm");

    private SimpleDateFormat localFormat = new SimpleDateFormat();
    
    /**
     * rcs entries, in reverse (date, time, author, comment) order
     */
    private Map entries = new TreeMap(Collections.reverseOrder());
  
    // state machine constants for reading Starteam output
    /** expecting file information */
    private static final int GET_FILE = 1;
    /** expecting date */
    private static final int GET_AUTHOR = 2;
    /** expecting comments */
    private static final int GET_COMMENT = 3;
    /** expecting revision */
    private static final int GET_REVISION = 4;
    /** Marks start of file data*/
    private static final String START_FILE = "History for: ";
    /** Marks end of file */
    private static final String END_FILE = "==================================="
        + "==========================================";
    /** Marks start of revision */
    private static final String START_REVISION = "----------------------------";
    /** Marks revision data */
    private static final String REVISION_TAG = "Branch Revision: ";
    /** Marks author data */
    private static final String AUTHOR_TAG = "Author: ";
    /** Marks date data */
    private static final String DATE_TAG = " Date: ";

    /** current status of the parser */
    private int status = GET_FILE;
    
    /** the current log entry being processed by the parser*/
    private ChangeLogEntry currentLogEntry = null;
    
    /** the current file being processed by the parser */
    private ChangeLogFile currentFile = null;

    /** the before date */
    private Date beforeDate;

    /** the to date */
    private Date toDate;

    /** the test mode (In test mode, we dont't use the date range*/
    private boolean testMode = false;
    
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
        // current state transitions in the state machine - starts with Get File
        //      Get File                -> Get Revision
        //      Get Revision            -> Get Date or Get File
        //      Get Date                -> Get Comment
        //      Get Comment             -> Get Comment or Get Revision
        switch (getStatus())
        {
            case GET_FILE:
                processGetFile(line);
                break;
            case GET_REVISION:
                processGetRevision(line);
                break;
            case GET_AUTHOR:
                processGetAuthor(line);
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
     *      with the same key doesn't exist already. If the entry's author
     *      is null, the entry wont be added
     * @param file a {@link ChangeLogFile} to be added to the entry
     */
    private void addEntry(ChangeLogEntry entry, ChangeLogFile file)
    {
        // do not add if entry is not populated
        if (entry.getAuthor() == null)
        {
            return;
        }

        // do not add if entry is out of date range
        if (!testMode)
        {
            if (beforeDate != null && toDate != null)
            {
                if (entry.getDate().before(beforeDate) || entry.getDate().after(toDate))
                {
                    return;
                }
            }
        }
        
        String key = ENTRY_KEY_TIMESTAMP_FORMAT.format(entry.getDate())
            + entry.getAuthor() + entry.getComment();
        
        if (!entries.containsKey(key))
        {
            entry.addFile(file);
            entries.put(key, entry);
        }
        else
        {
            ChangeLogEntry existingEntry = (ChangeLogEntry) entries.get(key);
            existingEntry.addFile(file);
        }
    }
 
    /**
     * Process the current input line in the Get File state.
     * @param line a line of text from the Starteam log output
     */
    private void processGetFile(String line)
    {
        if (line.startsWith(START_FILE))
        {
            setCurrentLogEntry(new ChangeLogEntry());
            setCurrentFile(new ChangeLogFile(line.substring(START_FILE.length(),
                line.length())));
            setStatus(GET_REVISION);
        }
    }

    /**
     * Process the current input line in the Get Revision state.
     * @param line a line of text from the Starteam log output
     */
    private void processGetRevision(String line) 
    {
        int pos;
        if ((pos=line.indexOf(REVISION_TAG)) != -1)
        {
            getCurrentFile().setRevision(line.substring(pos + REVISION_TAG.length()));
            setStatus(GET_AUTHOR);
        }
        else if (line.startsWith(END_FILE))
        {
            // If we encounter an end of file line, it means there 
            // are no more revisions for the current file.
            // there could also be a file still being processed.
            setStatus(GET_FILE);
            addEntry(getCurrentLogEntry(), getCurrentFile());
        }
    }

    /**
     * Process the current input line in the Get Author/Date state.
     * @param line a line of text from the Starteam log output
     */
    private void processGetAuthor(String line)
    {
        if (line.startsWith(AUTHOR_TAG))
        {
            int posDateTag = line.indexOf(DATE_TAG);
            String author = line.substring(AUTHOR_TAG.length(), posDateTag);
            getCurrentLogEntry().setAuthor(author);
            String date = line.substring(posDateTag + DATE_TAG.length());
            getCurrentLogEntry().setDate(parseDate(date));
            setStatus(GET_COMMENT);
        }
    }

    /**
     * Process the current input line in the Get Comment state.
     * @param line a line of text from the Starteam log output
     */
    private void processGetComment(String line)
    {
        if (line.startsWith(START_REVISION))
        {
            // add entry, and set state to get revision
            addEntry(getCurrentLogEntry(), getCurrentFile());
            // new change log entry
            setCurrentLogEntry(new ChangeLogEntry());
            // same file name, but different rev
            setCurrentFile(new ChangeLogFile(getCurrentFile().getName()));
            setStatus(GET_REVISION);
        }
        else if (line.startsWith(END_FILE))
        {
            addEntry(getCurrentLogEntry(), getCurrentFile());
            setStatus(GET_FILE);
        }
        else
        {
            // keep gathering comments
            getCurrentLogEntry().setComment(
                getCurrentLogEntry().getComment() + line + "\n");
        }
    }

    /**
     * Getter for property currentFile.
     * @return Value of property currentFile.
     */
    private ChangeLogFile getCurrentFile()
    {
        return currentFile;
    }
    
    /**
     * Setter for property currentFile.
     * @param currentFile New value of property currentFile.
     */
    private void setCurrentFile(ChangeLogFile currentFile)
    {
        this.currentFile = currentFile;
    }
    
    /**
     * Getter for property currentLogEntry.
     * @return Value of property currentLogEntry.
     */
    private ChangeLogEntry getCurrentLogEntry()
    {
        return currentLogEntry;
    }
    
    /**
     * Setter for property currentLogEntry.
     * @param currentLogEntry New value of property currentLogEntry.
     */
    private void setCurrentLogEntry(ChangeLogEntry currentLogEntry)
    {
        this.currentLogEntry = currentLogEntry;
    }
    
    /**
     * Getter for property status.
     * @return Value of property status.
     */
    private int getStatus()
    {
        return status;
    }
    
    /**
     * Setter for property status.
     * @param status New value of property status.
     */
    private void setStatus(int status)
    {
        this.status = status;
    }
    
    /** 
     * Converts the date timestamp from the svn output into a date
     * object.
     * 
     * @return A date representing the timestamp of the log entry.
     */
    private Date parseDate(String date)
    {
        try
        {
            return localFormat.parse(date.toString());
        }
        catch (ParseException e)
        {
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
