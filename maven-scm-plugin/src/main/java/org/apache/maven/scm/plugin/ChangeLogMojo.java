package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

/**
 * Dump changelog contents to console. It is mainly used to test maven-scm-api's changelog command.
 *
 * @author <a href="dantran@gmail.com">Dan Tran</a>
 * @version $Id$
 * @goal changelog
 * @aggregator
 */
public class ChangeLogMojo
    extends AbstractScmMojo
{
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * Start Date.
     *
     * @parameter expression="${startDate}"
     */
    private String startDate;

    /**
     * End Date.
     *
     * @parameter expression="${endDate}"
     */
    private String endDate;

    /**
     * Date Format in changelog output of scm tool.
     *
     * @parameter expression="${dateFormat}"
     */
    private String dateFormat;

    /**
     * Date format to use for the specified startDate and/or endDate.
     *
     * @parameter expression="${dateFormat}" default-value="yyyy-MM-dd"
     */
    private String userDateFormat = DEFAULT_DATE_FORMAT;

    public void execute()
        throws MojoExecutionException
    {
        SimpleDateFormat localFormat = new SimpleDateFormat( userDateFormat );

        try
        {
            ScmRepository repository = getScmRepository();

            ScmProvider provider = getScmManager().getProviderByRepository( repository );

            ChangeLogScmResult result = provider.changeLog( repository, getFileSet(),
                                                            this.parseDate( localFormat, this.startDate ),
                                                            this.parseDate( localFormat, this.endDate ), 0, null,
                                                            dateFormat );
            checkResult( result );

            ChangeLogSet changeLogSet = result.getChangeLog();

            for ( Iterator i = changeLogSet.getChangeSets().iterator(); i.hasNext(); )
            {
                ChangeSet changeSet = (ChangeSet) i.next();

                System.out.println( changeSet.toString() );
            }

        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Cannot run changelog command : ", e );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run changelog command : ", e );
        }
    }

    /**
     * Converts the localized date string pattern to date object.
     *
     * @return A date
     */
    private Date parseDate( SimpleDateFormat format, String date )
        throws MojoExecutionException
    {
        if ( date == null || date.trim().length() == 0 )
        {
            return null;
        }

        try
        {
            return format.parse( date.toString() );
        }
        catch ( ParseException e )
        {
            throw new MojoExecutionException( "Please use this date pattern: " + format.toLocalizedPattern().toString(),
                                              e );
        }
    }
}
