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

import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public abstract class AbstractConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    /**
     * AbstractConsumer constructor.
     *
     * @param logger The logger to use in the consumer
     */
    public AbstractConsumer( ScmLogger logger )
    {
        setLogger( logger );
    }

    public ScmLogger getLogger()
    {
        return logger;
    }

    public void setLogger( ScmLogger logger )
    {
        this.logger = logger;
    }

    /**
     * Converts the date timestamp from the output into a date object.
     *
     * @return A date representing the timestamp of the log entry.
     */
    protected Date parseDate( String date, String userPattern, String defaultPattern )
    {
        return parseDate( date, userPattern, defaultPattern, null );
    }

    /**
     * Converts the date timestamp from the output into a date object.
     *
     * @return A date representing the timestamp of the log entry.
     */
    protected Date parseDate( String date, String userPattern, String defaultPattern, Locale locale )
    {
        DateFormat format;

        if ( StringUtils.isNotEmpty( userPattern ) )
        {
            format = new SimpleDateFormat( userPattern );
        }
        else
        {
            if ( StringUtils.isNotEmpty( defaultPattern ) )
            {
                if ( locale != null )
                {
                    format = new SimpleDateFormat( defaultPattern, locale );
                }
                else
                {
                    format = new SimpleDateFormat( defaultPattern );
                }
            }
            else
            {
                // Use the English short date pattern if no pattern is specified
                format = DateFormat.getDateInstance( DateFormat.SHORT, Locale.ENGLISH );
            }
        }

        try
        {
            return format.parse( date );
        }
        catch ( ParseException e )
        {
            if ( getLogger() != null )
            {
                getLogger().error( "ParseException Caught", e );
            }

            return null;
        }
    }
}
