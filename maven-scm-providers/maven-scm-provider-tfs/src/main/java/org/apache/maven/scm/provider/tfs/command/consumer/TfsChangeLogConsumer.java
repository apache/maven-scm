package org.apache.maven.scm.provider.tfs.command.consumer;

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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;

/**
 * @author Olivier Lamy
 *
 */
public class TfsChangeLogConsumer
    extends AbstractConsumer
{

    private static final String PATTERN =
        "^[^:]*:[ \t]([0-9]*)\n" + "[^:]*:[ \t](.*)\n[^:]*:[ \t](.*)\n"
            + "[^:]*:((?:\n.*)*)\n\n[^\n :]*:(?=\n  )((?:\n[ \t]+.*)*)";

    private static final String PATTERN_ITEM = "\n  ([^$]+) (\\$/.*)";

    private List<ChangeSet> logs = new ArrayList<ChangeSet>();

    private String buffer = "";

    boolean fed = false;

    public TfsChangeLogConsumer( ScmLogger logger )
    {
        super( logger );
    }

    public void consumeLine( String line )
    {
        fed = true;
        if ( line.startsWith( "-----" ) )
        {
            addChangeLog();
        }
        buffer += line + "\n";
    }

    public List<ChangeSet> getLogs()
    {
        addChangeLog();
        return logs;
    }

    private void addChangeLog()
    {
        if ( !buffer.equals( "" ) )
        {
            Pattern p = Pattern.compile( PATTERN );
            Matcher m = p.matcher( buffer );
            if ( m.find() )
            {
                String revision = m.group( 1 ).trim();
                String username = m.group( 2 ).trim();
                String dateString = m.group( 3 ).trim();
                String comment = m.group( 4 ).trim();
                Pattern itemPattern = Pattern.compile( PATTERN_ITEM );
                Matcher itemMatcher = itemPattern.matcher( m.group( 5 ) );
                List<ChangeFile> files = new ArrayList<ChangeFile>();
                while ( itemMatcher.find() )
                {
                    ChangeFile file = new ChangeFile( itemMatcher.group( 2 ).trim(), revision );
                    files.add( file );
                }
                Date date;
                try
                {
                    date = parseDate( dateString );
                }
                catch ( ParseException e )
                {
                    getLogger().error( "Date parse error", e );
                    throw new RuntimeException( e );
                }

                ChangeSet change = new ChangeSet( date, comment, username, files );
                logs.add( change );
            }
            buffer = "";
        }
    }

    public boolean hasBeenFed()
    {
        return fed;
    }

    @SuppressWarnings( "deprecation" )
    protected static Date parseDate( String dateString )
        throws ParseException
    {
        Date date = null;
        try
        {
            // Use the depricated Date.parse method as this is very good at
            // detecting
            // dates commonly output by the US and UK standard locales of
            // dotnet that
            // are output by the Microsoft command line client.
            date = new Date( Date.parse( dateString ) );
        }
        catch ( IllegalArgumentException e )
        {
            // ignore - parse failed.
        }
        if ( date == null )
        {
            // The old fashioned way did not work. Let's try it using a more
            // complex
            // alternative.
            DateFormat[] formats = createDateFormatsForLocaleAndTimeZone( null, null );
            return parseWithFormats( dateString, formats );
        }
        return date;
    }

    private static Date parseWithFormats( String input, DateFormat[] formats )
        throws ParseException
    {
        ParseException parseException = null;
        for ( int i = 0; i < formats.length; i++ )
        {
            try
            {
                return formats[i].parse( input );
            }
            catch ( ParseException ex )
            {
                parseException = ex;
            }
        }

        throw parseException;
    }

    /**
     * Build an array of DateFormats that are commonly used for this locale and timezone.
     */
    private static DateFormat[] createDateFormatsForLocaleAndTimeZone( Locale locale, TimeZone timeZone )
    {
        if ( locale == null )
        {
            locale = Locale.getDefault();
        }

        if ( timeZone == null )
        {
            timeZone = TimeZone.getDefault();
        }

        List<DateFormat> formats = new ArrayList<DateFormat>();

        for ( int dateStyle = DateFormat.FULL; dateStyle <= DateFormat.SHORT; dateStyle++ )
        {
            for ( int timeStyle = DateFormat.FULL; timeStyle <= DateFormat.SHORT; timeStyle++ )
            {
                DateFormat df = DateFormat.getDateTimeInstance( dateStyle, timeStyle, locale );
                if ( timeZone != null )
                {
                    df.setTimeZone( timeZone );
                }
                formats.add( df );
            }
        }

        for ( int dateStyle = DateFormat.FULL; dateStyle <= DateFormat.SHORT; dateStyle++ )
        {
            DateFormat df = DateFormat.getDateInstance( dateStyle, locale );
            df.setTimeZone( timeZone );
            formats.add( df );
        }

        return (DateFormat[]) formats.toArray( new DateFormat[formats.size()] );
    }

}
