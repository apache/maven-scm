package org.apache.maven.scm.util;

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

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public abstract class AbstractConsumer
    implements StreamConsumer
{
    protected final Logger logger = LoggerFactory.getLogger( getClass() );

    /**
     * Converts the date timestamp from the output into a date object.
     *
     * @param date TODO
     * @param userPattern TODO
     * @param defaultPattern TODO
     * @return A date representing the timestamp of the log entry.
     */
    protected Date parseDate( String date, String userPattern, String defaultPattern )
    {
        return parseDate( date, userPattern, defaultPattern, null );
    }

    /**
     * Converts the date timestamp from the output into a date object.
     *
     * @param date TODO
     * @param userPattern TODO
     * @param defaultPattern TODO
     * @param locale TODO
     * @return A date representing the timestamp of the log entry.
     */
    protected Date parseDate( String date, String userPattern, String defaultPattern, Locale locale )
    {
        DateFormat format;

        String patternUsed = null;
        Locale localeUsed = null;

        if ( StringUtils.isNotEmpty( userPattern ) )
        {
            if ( locale != null )
            {
                format = new SimpleDateFormat( userPattern, locale );
                localeUsed = locale;
            }
            else
            {
                format = new SimpleDateFormat( userPattern );
                localeUsed = Locale.getDefault();
            }
            patternUsed = userPattern;
        }
        else
        {
            if ( StringUtils.isNotEmpty( defaultPattern ) )
            {
                if ( locale != null )
                {
                    format = new SimpleDateFormat( defaultPattern, locale );
                    localeUsed = locale;
                }
                else
                {
                    format = new SimpleDateFormat( defaultPattern );
                    localeUsed = Locale.getDefault();
                }
                patternUsed = defaultPattern;
            }
            else
            {
                // Use the English short date pattern if no pattern is specified
                format = DateFormat.getDateInstance( DateFormat.SHORT, Locale.ENGLISH );
                patternUsed = "DateFormat.SHORT";
                localeUsed = Locale.ENGLISH;
            }
        }

        try
        {
            return format.parse( date );
        }
        catch ( ParseException e )
        {
            if ( logger.isWarnEnabled() )
            {
                logger.warn(
                                   "skip ParseException: " + e.getMessage() + " during parsing date '" + date
                                       + "' with pattern '" + patternUsed + "' and locale '"
                                       + localeUsed + "'", e );
            }

            return null;
        }
    }
}
