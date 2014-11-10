package org.apache.maven.scm.provider.hg.command.changelog;

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
import java.util.Locale;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.command.HgConsumer;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @author <a href="mailto:hr.mohr@gmail.com">Mads Mohr Christensen</a>
 */
public class HgChangeLogConsumer
    extends HgConsumer
{

    private static final String TIME_PATTERN = "yyyy-MM-dd HH:mm:ss Z";

    private static final String REVNO_TAG = "changeset:";

    private static final String TAG_TAG = "tag:";

    private static final String BRANCH_TAG = "branch:";

    private static final String AUTHOR_TAG = "user:";

    private static final String TIME_STAMP_TOKEN = "date:";

    private static final String MESSAGE_TOKEN = "description:";

    private static final String FILES_TOKEN = "files:";

    private List<ChangeSet> logEntries = new ArrayList<ChangeSet>();

    private ChangeSet currentChange;

    private String currentRevision;

    @SuppressWarnings( "unused" )
    private String currentTag; // don't know what to do with this

    @SuppressWarnings( "unused" )
    private String currentBranch; // don't know what to do with this

    private String userDatePattern;

    public HgChangeLogConsumer( ScmLogger logger, String userDatePattern )
    {
        super( logger );
        this.userDatePattern = userDatePattern;
    }

    public List<ChangeSet> getModifications()
    {
        return logEntries;
    }

    /**
     * {@inheritDoc}
     */
    public void consumeLine( String line )
    {
        // override default behaviour which tries to pick through things for some standard messages.  that
        // does not apply here
        String trimmedLine = line.trim();
        doConsume( null, trimmedLine );
    }

    /**
     * {@inheritDoc}
     */
    public void doConsume( ScmFileStatus status, String line )
    {
        String tmpLine;

        // new changeset
        if ( line.startsWith( REVNO_TAG ) )
        {
            //Init a new changeset
            currentChange = new ChangeSet();
            currentChange.setFiles( new ArrayList<ChangeFile>( 0 ) );
            logEntries.add( currentChange );

            // parse revision
            tmpLine = line.substring( REVNO_TAG.length() ).trim();
            currentRevision = tmpLine.substring( tmpLine.indexOf( ':' ) + 1 );
            currentChange.setRevision( currentRevision );
        }
        else if ( line.startsWith( BRANCH_TAG ) )
        {
            tmpLine = line.substring( BRANCH_TAG.length() ).trim();
            currentBranch = tmpLine;
        }
        else if ( line.startsWith( AUTHOR_TAG ) )
        {
            tmpLine = line.substring( AUTHOR_TAG.length() ).trim();
            currentChange.setAuthor( tmpLine );
        }
        else if ( line.startsWith( TIME_STAMP_TOKEN ) )
        {
            tmpLine = line.substring( TIME_STAMP_TOKEN.length() ).trim();
            Date date = parseDate( tmpLine, userDatePattern, TIME_PATTERN, Locale.ENGLISH );
            currentChange.setDate( date );
        }
        else if ( line.startsWith( TAG_TAG ) )
        {
            tmpLine = line.substring( TAG_TAG.length() ).trim();
            currentTag = tmpLine;
        }
        else if ( line.startsWith( FILES_TOKEN ) )
        {
            tmpLine = line.substring( FILES_TOKEN.length() ).trim();
            String[] files = tmpLine.split( " " );
            for ( int i = 0; i < files.length; i++ )
            {
                String file = files[i];
                ChangeFile changeFile = new ChangeFile( file, currentRevision );
                currentChange.addFile( changeFile );
            }
        }
        else if ( line.startsWith( MESSAGE_TOKEN ) )
        {
            currentChange.setComment( "" );
        }
        else
        {
            StringBuilder comment = new StringBuilder( currentChange.getComment() );
            comment.append( line );
            comment.append( '\n' );
            currentChange.setComment( comment.toString() );
        }
    }
}
