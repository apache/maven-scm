package org.apache.maven.scm.command.changelog;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

import java.io.File;
import java.util.Date;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;




/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractChangeLogCommand
    extends AbstractCommand
{
    protected abstract ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repository, File workingDirectory, Date startDate, Date endDate, int numDays, String branch )
        throws ScmException;

    public ScmResult executeCommand( ScmProviderRepository repository, File workingDirectory, CommandParameters parameters )
        throws ScmException
    {
        Date startDate = parameters.getDate( CommandParameter.START_DATE, null );

        Date endDate = parameters.getDate( CommandParameter.END_DATE, null );

        int numDays = parameters.getInt( CommandParameter.NUM_DAYS, 0 );

        String branch = parameters.getString( CommandParameter.BRANCH, null );

        if ( numDays != 0 && ( startDate != null || endDate != null ) )
        {
            throw new ScmException( "Start or end date cannot be set if num days is set." );
        }

        if ( endDate != null && startDate == null )
        {
            throw new ScmException( "The end date is set but the start date isn't." );
        }

        return executeChangeLogCommand( repository, workingDirectory, startDate, endDate, numDays, branch );
    }
}
