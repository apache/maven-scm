package org.apache.maven.scm.provider.bazaar.command.changelog;

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
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarChangeLogConsumer
    extends BazaarConsumer
{

    private static final SimpleDateFormat BAZAAR_TIME_FORMAT = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z" );

    private static final String START_LOG_TAG = "-----";

    private static final String REVNO_TAG = "revno: ";

    private static final String AUTHOR_TAG = "committer: ";

    private static final String TIME_STAMP_TOKEN = "timestamp: ";

    private static final String MESSAGE_TOKEN = "message:";

    private static final String BRANCH_NICK_TOKEN = "branch nick: ";

    private final File workingDir;

    private List logEntries = new ArrayList();

    private ChangeSet currentChange;

    private String currentRevision;

    private StringBuffer currentComment;

    /**
     * null means not parsing message nor files, UNKNOWN means parsing message
     */
    private ScmFileStatus currentStatus = null;

    public BazaarChangeLogConsumer( ScmLogger logger, File workingDir )
    {
        super( logger );

        this.workingDir = workingDir;
    }

    public List getModifications()
    {
        return logEntries;
    }

    public void doConsume( ScmFileStatus status, String line )
    {
        String tmpLine = new String( line );

        // Parse line
        if ( line.startsWith( START_LOG_TAG ) )
        {
            currentChange = new ChangeSet();
            logEntries.add( currentChange );
            currentComment = new StringBuffer();
            currentStatus = null;
            currentRevision = "";
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
            try
            {
                Date date = BAZAAR_TIME_FORMAT.parse( tmpLine );
                currentChange.setDate( date );
            }
            catch ( ParseException e )
            {
                logger.warn( "Could not figure out of date: " + tmpLine );
            }
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
            currentComment.append( line ).append( "\n" );
        }
        else if ( currentStatus != null )
        {
            tmpLine = tmpLine.trim();
            File tmpFile = new File( workingDir, tmpLine );
            if ( tmpFile.isFile() )
            {
                ChangeFile changeFile = new ChangeFile( tmpLine, currentRevision );
                currentChange.addFile( changeFile );
            }
        }
        else if ( line.startsWith( BRANCH_NICK_TOKEN ) )
        {
            //ignore
        }
        else
        {
            logger.warn( "Could not figure out of: " + line );
        }
    }
}
