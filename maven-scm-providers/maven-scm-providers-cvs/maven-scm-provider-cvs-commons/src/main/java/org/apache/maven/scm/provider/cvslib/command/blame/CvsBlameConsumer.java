package org.apache.maven.scm.provider.cvslib.command.blame;

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

import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;
import org.apache.regexp.RE;

/**
 * @author Evgeny Mandrikov
 * @since 1.4
 */
public class CvsBlameConsumer
    extends AbstractConsumer
{
    private static final String CVS_TIMESTAMP_PATTERN = "dd-MMM-yy";

    /* 1.1          (tor      24-Mar-03): */

    private static final String LINE_PATTERN = "(.*)\\((.*)\\s+(.*)\\)";

    /**
     * @see #LINE_PATTERN
     */
    private RE lineRegexp;

    private List lines = new ArrayList();

    public CvsBlameConsumer( ScmLogger logger )
    {
        super( logger );

        lineRegexp = new RE( LINE_PATTERN );
    }

    public void consumeLine( String line )
    {
        if (line != null && line.indexOf( ":" ) > 0 )
        {
            String annotation = line.substring( 0, line.indexOf( ':' ) );
            if ( lineRegexp.match( annotation ) )
            {
                String revision = lineRegexp.getParen( 1 ).trim();
                String author = lineRegexp.getParen( 2 ).trim();
                String dateTimeStr = lineRegexp.getParen( 3 ).trim();

                Date dateTime = parseDate( dateTimeStr, null, CVS_TIMESTAMP_PATTERN );
                getLines().add( new BlameLine( dateTime, revision, author ) );

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( author + " " + dateTimeStr );
                }
            }
        }
    }

    public List getLines()
    {
        return lines;
    }

}
