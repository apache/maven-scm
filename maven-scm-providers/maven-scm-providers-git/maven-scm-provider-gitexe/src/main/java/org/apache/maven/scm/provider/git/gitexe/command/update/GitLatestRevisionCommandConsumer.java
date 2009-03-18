package org.apache.maven.scm.provider.git.gitexe.command.update;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id$
 */
public class GitLatestRevisionCommandConsumer
    extends AbstractConsumer
{

    /**
     * The pattern used to match git log latest revision lines
     */
    private static final String LATESTREV_PATTERN = "^commit \\s*(.*)";

    /**
     * The regular expression used to match git log latest revision lines
     */
    private RE latestRevRegexp;

    private String latestRevision;

    public GitLatestRevisionCommandConsumer( ScmLogger logger )
    {
        super( logger );

        try
        {
            latestRevRegexp = new RE( LATESTREV_PATTERN );
        }
        catch ( RESyntaxException ex )
        {
            throw new RuntimeException(
                                        "INTERNAL ERROR: Could not create regexp to parse git log file. This shouldn't happen. Something is probably wrong with the oro installation.",
                                        ex );
        }

    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "GitLatestRevisionCommandConsumer consumeLine : " + line );
        }
        if ( line == null || StringUtils.isEmpty( line ) )
        {
            return;
        }

        processGetLatestRevision( line );
    }

    public String getLatestRevision()
    {
        return latestRevision;
    }

    /**
     * Process the current input line for the latest revision
     *
     * @param line A line of text from the git log output
     */
    private void processGetLatestRevision( String line )
    {
        if ( !latestRevRegexp.match( line ) )
        {
            return;
        }

        latestRevision = latestRevRegexp.getParen( 1 );
    }

}
