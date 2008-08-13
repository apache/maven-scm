package org.apache.maven.scm.provider.synergy.consumer;

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
import org.apache.maven.scm.provider.synergy.util.SynergyTask;
import org.apache.maven.scm.provider.synergy.util.SynergyUtil;
import org.apache.maven.scm.util.AbstractConsumer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Mainly inspired from CruiseControl
 *
 * @author <a href="julien.henry@capgemini.com">Julien Henry</a>
 * @version $Id$
 */
public class SynergyGetCompletedTasksConsumer
    extends AbstractConsumer
{

    /**
     * The date format as returned by your installation of CM Synergy. Fri Dec 3
     * 17:51:56 2004
     */
    private String ccmDateFormat = "EEE MMM dd HH:mm:ss yyyy";

    private String language = "en";

    private String country = "US";

    public static String OUTPUT_FORMAT = "%displayname" + SynergyUtil.SEPARATOR + "%owner" + SynergyUtil.SEPARATOR +
        "%completion_date" + SynergyUtil.SEPARATOR + "%task_synopsis" + SynergyUtil.SEPARATOR;

    private List entries = new ArrayList();

    /**
     * @return the tasks
     */
    public List getTasks()
    {
        return entries;
    }

    public SynergyGetCompletedTasksConsumer( ScmLogger logger )
    {
        super( logger );
        String dateFormat = System.getProperty( "maven.scm.synergy.ccmDateFormat" );
        if ( dateFormat != null && !dateFormat.equals( "" ) )
        {
            this.ccmDateFormat = dateFormat;
        }
        logger.debug( "dateFormat = " + this.ccmDateFormat );
        String language = System.getProperty( "maven.scm.synergy.language" );
        if ( language != null && !language.equals( "" ) )
        {
            this.language = language;
        }
        logger.debug( "language = " + this.language );
        String country = System.getProperty( "maven.scm.synergy.country" );
        if ( country != null && !country.equals( "" ) )
        {
            this.country = country;
        }
        logger.debug( "country = " + this.country );
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        getLogger().debug( "Consume: " + line );
        StringTokenizer tokenizer = new StringTokenizer( line.trim(), SynergyUtil.SEPARATOR );
        if ( tokenizer.countTokens() == 4 )
        {
            SynergyTask task = new SynergyTask();
            task.setNumber( Integer.parseInt( tokenizer.nextToken() ) );
            task.setUsername( tokenizer.nextToken() );
            try
            {
                task.setModifiedTime( new SimpleDateFormat( ccmDateFormat, new Locale( language, country ) )
                    .parse( tokenizer.nextToken() ) );
            }
            catch ( ParseException e )
            {
                getLogger().error( "Wrong date format", e );
            }
            task.setComment( tokenizer.nextToken() );

            // Add the task to the list
            entries.add( task );
        }
        else
        {
            getLogger().error(
                "Invalid token count in SynergyGetCompletedTasksConsumer [" + tokenizer.countTokens() + "]" );
            while ( tokenizer.hasMoreElements() )
            {
                getLogger().debug( tokenizer.nextToken() );
            }
        }

    }
}
