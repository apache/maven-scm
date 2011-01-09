package org.apache.maven.scm.provider.perforce.command.blame;

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

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;
import org.apache.regexp.RE;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgeny Mandrikov
 * @since 1.4
 */
public class PerforceFilelogConsumer
    extends AbstractConsumer
{
    private static final String PERFORCE_TIMESTAMP_PATTERN = "yyyy/MM/dd";

    private static final String LINE_PATTERN = "#(\\d+).*on (.*) by (.*)@";

    private RE lineRegexp;

    private Map<String, Date> dates = new HashMap<String,Date>();

    private Map<String,String> authors = new HashMap<String,String>();

    public PerforceFilelogConsumer( ScmLogger logger )
    {
        super( logger );
        lineRegexp = new RE( LINE_PATTERN );
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( lineRegexp.match( line ) )
        {
            String revision = lineRegexp.getParen( 1 );
            String dateTimeStr = lineRegexp.getParen( 2 );
            String author = lineRegexp.getParen( 3 );

            Date dateTime = parseDate( dateTimeStr, null, PERFORCE_TIMESTAMP_PATTERN );

            dates.put( revision, dateTime );
            authors.put( revision, author );
        }
    }

    public String getAuthor( String revision )
    {
        return (String) authors.get( revision );
    }

    public Date getDate( String revision )
    {
        return (Date) dates.get( revision );
    }
}
