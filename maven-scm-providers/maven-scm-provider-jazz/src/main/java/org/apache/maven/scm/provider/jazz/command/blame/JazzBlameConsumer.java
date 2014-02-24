package org.apache.maven.scm.provider.jazz.command.blame;

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
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.consumer.AbstractRepositoryConsumer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//STATUS: NOT DONE

/**
 * Consume the output of the scm command for the "blame" operation.
 *
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzBlameConsumer
    extends AbstractRepositoryConsumer
{
    private static final String JAZZ_TIMESTAMP_PATTERN = "yyyy-MM-dd";

//  1 Deb (1008) 2011-12-14                       Test.txt
//  2 Deb (1005) 2011-12-14 59 My commit comment.

    private static final Pattern LINE_PATTERN = Pattern.compile( "(\\d+) (.*) \\((\\d+)\\) (\\d+-\\d+-\\d+) (.*)" );

    private List<BlameLine> fLines = new ArrayList<BlameLine>();

    private SimpleDateFormat dateFormat;

    /**
     * Construct the JazzBlameCommand consumer.
     *
     * @param repository The repository we are working with.
     * @param logger     The logger to use.
     */
    public JazzBlameConsumer( ScmProviderRepository repository, ScmLogger logger )
    {
        super( repository, logger );

        dateFormat = new SimpleDateFormat( JAZZ_TIMESTAMP_PATTERN );
        dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
    }

    /**
     * Process one line of output from the execution of the "scm annotate" command.
     *
     * @param line The line of output from the external command that has been pumped to us.
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        super.consumeLine( line );

        Matcher matcher = LINE_PATTERN.matcher( line );
        if ( matcher.matches() )
        {
            String lineNumberStr = matcher.group( 1 );
            String owner = matcher.group( 2 );
            String changeSetNumberStr = matcher.group( 3 );
            String dateStr = matcher.group( 4 );
            Date date = parseDate( dateStr, JAZZ_TIMESTAMP_PATTERN, null );
            fLines.add( new BlameLine( date, changeSetNumberStr, owner ) );
        }
    }

    public List<BlameLine> getLines()
    {
        return fLines;
    }
}