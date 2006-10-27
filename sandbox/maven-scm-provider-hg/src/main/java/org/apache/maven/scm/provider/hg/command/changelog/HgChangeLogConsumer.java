package org.apache.maven.scm.provider.hg.command.changelog;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.command.HgConsumer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgChangeLogConsumer
    extends HgConsumer
{

    private static final String TIME_PATTERN = "EEE MMM dd HH:mm:ss yyyy Z";

    private static final String REVNO_TAG = "changeset: ";

    private static final String REPO_TAG   = "tag:         ";

    private static final String AUTHOR_TAG = "user: ";

    private static final String TIME_STAMP_TOKEN = "date: ";

    private static final String MESSAGE_TOKEN = "description:";

    private static final String MERGED_TOKEN = "merged: ";

    private static final String FILES_TOKEN = "files: ";

    private String prevLine = "";
    private String prevPrevLine = "";

    private ArrayList logEntries = new ArrayList();

    private ChangeSet currentChange;

    private ChangeSet lastChange;

    private boolean isMergeEntry;

    private String currentRevision;

    private String currentTag;

    private String userDatePattern;

    private boolean spoolingComments;

    private List currentComment = null;

    /**
     * Null means not parsing message nor files, UNKNOWN means parsing message
     */
    private ScmFileStatus currentStatus = null;

    public HgChangeLogConsumer( ScmLogger logger, String userDatePattern )
    {
        super( logger );

        this.userDatePattern = userDatePattern;
    }

    public List getModifications()
    {
        return logEntries;
    }

    public void consumeLine(String line) {
        // override default behaviour which tries to pick through things for some standard messages.  that
        // does not apply here
        doConsume(null, line);
    }

    public void doConsume( ScmFileStatus status, String line )
    {
        String tmpLine = line;
        // If current status == null then this is a new entry
        // If the line == "" and previous line was "", then this is also a new entry
        if ((line.equals("") && (prevLine.equals("") && prevPrevLine.equals(""))) || currentComment == null)
        {
            if (currentComment != null) {
                StringBuffer comment = new StringBuffer();
                for (int i = 0; i < currentComment.size() - 1; i++) {
                    comment.append(currentComment.get(i));
                    if (i + 1 < currentComment.size() - 1)
                        comment.append('\n');
                }
                currentChange.setComment( comment.toString() );
            }

            spoolingComments = false;

            //If last entry was part a merged entry
            if ( isMergeEntry && lastChange != null )
            {
                String comment = lastChange.getComment();
                comment += "\n[MAVEN]: Merged from " + currentChange.getAuthor();
                comment += "\n[MAVEN]:    " + currentChange.getDateFormatted();
                comment += "\n[MAVEN]:    " + currentChange.getComment();
                lastChange.setComment( comment );
            }

            //Init a new changeset
            currentChange = new ChangeSet();
            currentChange.setFiles( new ArrayList() );
            logEntries.add( currentChange );

            //Reset memeber vars
            currentComment = new ArrayList();
            currentStatus = null;
            currentRevision = "";
            isMergeEntry = false;
        }

        if (spoolingComments)
        {
            currentComment.add( line );
        }
        else if ( line.startsWith( MESSAGE_TOKEN ) )
        {
            spoolingComments = true;
        }
        else if ( line.startsWith( MERGED_TOKEN ) )
        {
            //This is part of lastChange and is not a separate log entry
            isMergeEntry = true;
            logEntries.remove( currentChange );
            if ( logEntries.size() > 0 )
            {
                lastChange = (ChangeSet) logEntries.get( logEntries.size() - 1 );
            }
            else
            {
                getLogger().warn( "First entry was unexpectedly a merged entry" );
                lastChange = null;
            }
        }
        else if ( line.startsWith( REVNO_TAG ) )
        {
            tmpLine = line.substring( REVNO_TAG.length() );
            tmpLine = tmpLine.trim();
            currentRevision = tmpLine;
        }
        else if ( line.startsWith( REPO_TAG ) )
        {
            // TODO: Get the tag name of the file?
            currentTag = "tagged";
        }
        else if ( line.startsWith( AUTHOR_TAG ) )
        {
            tmpLine = line.substring( AUTHOR_TAG.length() );
            tmpLine = tmpLine.trim();
            currentChange.setAuthor( tmpLine );
        }
        else if ( line.startsWith( TIME_STAMP_TOKEN ) )
        {
            // TODO: FIX Date Parsing to match Mercurial or fix with template
            tmpLine = line.substring( TIME_STAMP_TOKEN.length() ).trim();
            Date date = parseDate( tmpLine, userDatePattern, TIME_PATTERN );
            currentChange.setDate( date );
        }
        else if (line.startsWith(FILES_TOKEN))
        {
            tmpLine = line.substring( FILES_TOKEN.length() ).trim();
            String[] files = tmpLine.split(" ");
            for (int i = 0; i < files.length; i++) {
                String file = files[i];
                ChangeFile changeFile = new ChangeFile( file, currentRevision );
                currentChange.addFile( changeFile );
            }
        }
        else
        {
            getLogger().warn( "Could not figure out: " + line );
        }

        if ( status != null )
        {
            currentStatus = status;
        }

        // record previous line
        prevLine = line;
        prevPrevLine = prevLine;
    }
}
