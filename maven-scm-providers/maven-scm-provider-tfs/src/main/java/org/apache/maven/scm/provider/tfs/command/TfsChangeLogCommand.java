package org.apache.maven.scm.provider.tfs.command;

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

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;
import org.apache.maven.scm.util.AbstractConsumer;

public class TfsChangeLogCommand
    extends AbstractChangeLogCommand
{

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository r, ScmFileSet f, Date startDate,
                                                          Date endDate, ScmBranch branch, String datePattern )
        throws ScmException
    {
        ArrayList changeLogs = new ArrayList();
        Iterator iter = f.getFileList().iterator();
        if ( !iter.hasNext() )
        {
            ArrayList dir = new ArrayList();
            // No files to iterate
            dir.add( f.getBasedir() );
            iter = dir.iterator();
        }
        TfsCommand command = null;
        // tf history takes only one file arg
        while ( iter.hasNext() )
        {
            TfsChangeLogConsumer out = new TfsChangeLogConsumer( getLogger() );
            ErrorStreamConsumer err = new ErrorStreamConsumer();

            command = createCommand( r, f, ( (File) iter.next() ) );
            int status = command.execute( out, err );

            if ( status != 0 || ( !out.hasBeenFed() && err.hasBeenFed() ) )
                return new ChangeLogScmResult( command.getCommandline(), "Error code for TFS changelog command - "
                    + status, err.getOutput(), false );
            changeLogs.addAll( out.getLogs() );
        }
        return new ChangeLogScmResult( command.getCommandline(), new ChangeLogSet( changeLogs, startDate, endDate ) );

    }

    protected TfsCommand createCommand( ScmProviderRepository r, ScmFileSet f, File file )
    {
        TfsCommand command = new TfsCommand( "history", r, f, getLogger() );
        command.addArgument( "-format:detailed" );
        command.addArgument( file.getName() );
        return command;
    }
}

class TfsChangeLogConsumer
    extends AbstractConsumer
{

    private static final String PATTERN =
        "^[^:]*:[ \t]([0-9]*)\n" + "[^:]*:[ \t](.*)\n[^:]*:[ \t](.*)\n"
            + "[^:]*:((?:\n.*)*)\n\n[^\n :]*:(?=\n  )((?:\n[ \t]+.*)*)";

    private static final String PATTERN_ITEM = "\n  ([^$]+) (\\$/.*)";

    ArrayList logs = new ArrayList();

    String buffer = "";

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

    public List getLogs()
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
                List files = new ArrayList();
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

        List formats = new ArrayList();

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
