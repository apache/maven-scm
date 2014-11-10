package org.apache.maven.scm.provider.git.gitexe.command.blame;

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

import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses the --porcelain format of git-blame
 *
 * For more information about the porcelain format, please read the official
 * <a href="http://www.kernel.org/pub/software/scm/git/docs/git-blame.html#_the_porcelain_format">
 * GIT blame porcelain format</a> description.
 *
 * @author Evgeny Mandrikov
 * @author Olivier Lamy
 * @author Mark Struberg
 * @since 1.4
 */
public class GitBlameConsumer
    extends AbstractConsumer
{
    private static final String GIT_COMMITTER_PREFIX = "committer";
    private static final String GIT_COMMITTER      = GIT_COMMITTER_PREFIX + " ";
    private static final String GIT_COMMITTER_TIME = GIT_COMMITTER_PREFIX + "-time ";
    private static final String GIT_AUTHOR         = "author ";


    private List<BlameLine> lines = new ArrayList<BlameLine>();

    /**
     * Since the porcelain format only contains the commit information
     * the first time a specific sha-1 commit appears, we need to store
     * this information somwehere.
     *
     * key: the sha-1 of the commit
     * value: the {@link BlameLine} containing the full committer/author info
     */
    private Map<String, BlameLine> commitInfo = new HashMap<String, BlameLine>();

    private boolean expectRevisionLine = true;

    private String revision  = null;
    private String author    = null;
    private String committer = null;
    private Date   time      = null;

    public GitBlameConsumer( ScmLogger logger )
    {
        super( logger );
    }

    public void consumeLine( String line )
    {
        if ( line == null )
        {
            return;
        }

        if ( expectRevisionLine )
        {
            // this is the revision line
            String parts[] = line.split( "\\s", 4 );

            if ( parts.length >= 1 )
            {
                revision = parts[0];

                BlameLine oldLine = commitInfo.get( revision );

                if ( oldLine != null )
                {
                    // restore the commit info
                    author    = oldLine.getAuthor();
                    committer = oldLine.getCommitter();
                    time      = oldLine.getDate();
                }

                expectRevisionLine = false;
            }
        }
        else
        {
            if ( line.startsWith( GIT_AUTHOR ) )
            {
                author = line.substring( GIT_AUTHOR.length() );
                return;
            }

            if ( line.startsWith( GIT_COMMITTER ) )
            {
                committer = line.substring( GIT_COMMITTER.length() );
                return;
            }

            if ( line.startsWith( GIT_COMMITTER_TIME ) )
            {
                String timeStr = line.substring( GIT_COMMITTER_TIME.length() );
                time = new Date( Long.parseLong( timeStr ) * 1000L );
                return;
            }


            if ( line.startsWith( "\t" ) )
            {
                // this is the content line.
                // we actually don't need the content, but this is the right time to add the blame line
                BlameLine blameLine = new BlameLine( time, revision, author, committer );
                getLines().add( blameLine );

                // keep commitinfo for this sha-1
                commitInfo.put( revision, blameLine );

                if ( getLogger().isDebugEnabled() )
                {
                    DateFormat df = SimpleDateFormat.getDateTimeInstance();
                    getLogger().debug( author + " " + df.format( time ) );
                }

                expectRevisionLine = true;
            }

        }
    }

    public List<BlameLine> getLines()
    {
        return lines;
    }
}
