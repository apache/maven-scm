package org.apache.maven.scm.provider.svn.svnexe.command.blame;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Evgeny Mandrikov
 * @author Olivier Lamy
 * @since 1.4
 */
public class SvnBlameConsumer
    extends AbstractConsumer
{
    private static final String SVN_TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private static final Pattern LINE_PATTERN = Pattern.compile( "line-number=\"(.*)\"" );

    private static final Pattern REVISION_PATTERN = Pattern.compile( "revision=\"(.*)\"" );

    private static final Pattern AUTHOR_PATTERN = Pattern.compile( "<author>(.*)</author>" );

    private static final Pattern DATE_PATTERN = Pattern.compile( "<date>(.*)T(.*)\\.(.*)Z</date>" );


    private SimpleDateFormat dateFormat;

    private List<BlameLine> lines = new ArrayList<BlameLine>();

    public SvnBlameConsumer( ScmLogger logger )
    {
        super( logger );

        dateFormat = new SimpleDateFormat( SVN_TIMESTAMP_PATTERN );
        dateFormat.setTimeZone( TimeZone.getTimeZone( "UTC" ) );
    }

    private int lineNumber;

    private String revision;

    private String author;

    public void consumeLine( String line )
    {
        Matcher matcher;
        if ( ( matcher = LINE_PATTERN.matcher( line ) ).find() )
        {
            String lineNumberStr = matcher.group( 1 );
            lineNumber = Integer.parseInt( lineNumberStr );
        }
        else if ( ( matcher = REVISION_PATTERN.matcher( line ) ).find() )
        {
            revision = matcher.group( 1 );
        }
        else if ( ( matcher = AUTHOR_PATTERN.matcher( line ) ).find() )
        {
            author = matcher.group( 1 );
        }
        else if ( ( matcher = DATE_PATTERN.matcher( line ) ).find() )
        {
            String date = matcher.group( 1 );
            String time = matcher.group( 2 );
            Date dateTime = parseDateTime( date + " " + time );
            lines.add( new BlameLine( dateTime, revision, author ) );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Author of line " + lineNumber + ": " + author + " (" + date + ")" );
            }
        }
    }

    protected Date parseDateTime( String dateTimeStr )
    {
        try
        {
            return dateFormat.parse( dateTimeStr );
        }
        catch ( ParseException e )
        {
            getLogger().error( "skip ParseException: " + e.getMessage() + " during parsing date " + dateTimeStr, e );
            return null;
        }
    }

    public List<BlameLine> getLines()
    {
        return lines;
    }
}
