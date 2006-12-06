package org.apache.maven.scm.provider.bazaar.command.changelog;

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
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarChangeLogConsumer
    extends BazaarConsumer
{

    private static final String BAZAAR_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss Z";

    private static final String START_LOG_TAG = "-----";

    private static final String REVNO_TAG = "revno: ";

    private static final String AUTHOR_TAG = "committer: ";

    private static final String TIME_STAMP_TOKEN = "timestamp: ";

    private static final String MESSAGE_TOKEN = "message:";

    private static final String BRANCH_NICK_TOKEN = "branch nick: ";

    private static final String MERGED_TOKEN = "merged: ";

    private List logEntries = new ArrayList();

    private ChangeSet currentChange;

    private ChangeSet lastChange;

    private boolean isMergeEntry;

    private String currentRevision;

    private StringBuffer currentComment;

    private String userDatePattern;

    /**
     * Null means not parsing message nor files, UNKNOWN means parsing message
     */
    private ScmFileStatus currentStatus = null;

    public BazaarChangeLogConsumer( ScmLogger logger, String userDatePattern )
    {
        super( logger );

        this.userDatePattern = userDatePattern;
    }

    public List getModifications()
    {
        return logEntries;
    }

    public void doConsume( ScmFileStatus status, String line )
    {
        String tmpLine = line;

        // Parse line
        if ( line.startsWith( START_LOG_TAG ) )
        {
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
            currentComment = new StringBuffer();
            currentStatus = null;
            currentRevision = "";
            isMergeEntry = false;
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
        else if ( line.startsWith( AUTHOR_TAG ) )
        {
            tmpLine = line.substring( AUTHOR_TAG.length() );
            tmpLine = tmpLine.trim();
            currentChange.setAuthor( tmpLine );
        }
        else if ( line.startsWith( TIME_STAMP_TOKEN ) )
        {
            tmpLine = line.substring( TIME_STAMP_TOKEN.length() + 3 );
            tmpLine = tmpLine.trim();
            Date date = parseDate( tmpLine, userDatePattern, BAZAAR_TIME_PATTERN );
            currentChange.setDate( date );
        }
        else if ( line.startsWith( MESSAGE_TOKEN ) )
        {
            currentStatus = ScmFileStatus.UNKNOWN;
        }
        else if ( status != null )
        {
            currentStatus = status;
        }
        else if ( currentStatus == ScmFileStatus.UNKNOWN )
        {
            currentComment.append( line );
            currentChange.setComment( currentComment.toString() );
            currentComment.append( "\n" );
        }
        else if ( currentStatus != null )
        {
            tmpLine = tmpLine.trim();
            ChangeFile changeFile = new ChangeFile( tmpLine, currentRevision );
            currentChange.addFile( changeFile );
        }
        else if ( line.startsWith( BRANCH_NICK_TOKEN ) )
        {
            //ignore
        }
        else
        {
            getLogger().warn( "Could not figure out of: " + line );
        }
    }
}
