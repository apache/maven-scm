package org.apache.maven.scm.provider.accurev.command.blame;

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

import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;
import org.apache.regexp.RE;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 * @since 1.4
 */
public class AccuRevBlameConsumer
  extends AbstractConsumer
{
    private static final String ACCUREV_TIMESTAMP_PATTERN = "yyyy/MM/dd HH:mm:ss";

    /* 3 godin 2009/11/18 16:26:33 */
    private static final String LINE_PATTERN = "\\s+(\\d+)\\s+(\\w+)\\s+([^ ]+ [^ ]+)";

    /**
     * @see #LINE_PATTERN
     */
    private RE lineRegexp;

    private List lines = new ArrayList();

    public AccuRevBlameConsumer( ScmLogger logger )
    {
        super( logger );
        lineRegexp = new RE( LINE_PATTERN );
    }

    public void consumeLine( String line )
    {
        if ( lineRegexp.match( line ) )
        {
            String revision = lineRegexp.getParen( 1 ).trim();
            String author = lineRegexp.getParen( 2 ).trim();
            String dateStr = lineRegexp.getParen( 3 ).trim();

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( author + " " + dateStr );
            }

            Date date = parseDate( dateStr, null, ACCUREV_TIMESTAMP_PATTERN );

            lines.add( new BlameLine( date, revision, author ) );
        }
    }

    public List getLines()
    {
        return lines;
    }
}
