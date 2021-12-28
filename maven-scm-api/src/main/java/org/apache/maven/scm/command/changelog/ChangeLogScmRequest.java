package org.apache.maven.scm.command.changelog;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRequest;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.repository.ScmRepository;

import java.util.Date;

/**
 * @author Petr Kozelka
 * @since 1.8
 */
public class ChangeLogScmRequest
    extends ScmRequest
{
    private static final long serialVersionUID = 20120620L;

    public ChangeLogScmRequest( ScmRepository scmRepository, ScmFileSet scmFileSet )
    {
        super( scmRepository, scmFileSet );
    }

    public ScmBranch getScmBranch()
        throws ScmException
    {
        return (ScmBranch) parameters.getScmVersion( CommandParameter.BRANCH, null );
    }

    public void setScmBranch( ScmBranch scmBranch )
        throws ScmException
    {
        parameters.setScmVersion( CommandParameter.BRANCH, scmBranch );
    }

    public Date getStartDate()
        throws ScmException
    {
        return parameters.getDate( CommandParameter.START_DATE );
    }

    /**
     * @param startDate the start date of the period
	 * @throws ScmException if any
     */
    public void setStartDate( Date startDate )
        throws ScmException
    {
        parameters.setDate( CommandParameter.START_DATE, startDate );
    }

    public Date getEndDate()
        throws ScmException
    {
        return parameters.getDate( CommandParameter.END_DATE );
    }

    /**
     * @param endDate the end date of the period
	 * @throws ScmException if any
     */
    public void setEndDate( Date endDate )
        throws ScmException
    {
        parameters.setDate( CommandParameter.END_DATE, endDate );
    }

    public int getNumDays()
        throws ScmException
    {
        return parameters.getInt( CommandParameter.START_DATE );
    }

    /**
     * @param numDays the number days before the current time if startdate and enddate are null
	 * @throws ScmException if any
     */
    public void setNumDays( int numDays )
        throws ScmException
    {
        parameters.setInt( CommandParameter.NUM_DAYS, numDays );
    }

    public ScmVersion getStartRevision()
        throws ScmException
    {
        return parameters.getScmVersion( CommandParameter.START_SCM_VERSION, null );
    }

    /**
     * @param startRevision the start branch/tag/revision
	 * @throws ScmException if any
     */
    public void setStartRevision( ScmVersion startRevision )
        throws ScmException
    {
        parameters.setScmVersion( CommandParameter.START_SCM_VERSION, startRevision );
    }

    public ScmVersion getEndRevision()
        throws ScmException
    {
        return parameters.getScmVersion( CommandParameter.END_SCM_VERSION, null );
    }

    /**
     * @param endRevision the end branch/tag/revision
	 * @throws ScmException if any
     */
    public void setEndRevision( ScmVersion endRevision )
        throws ScmException
    {
        parameters.setScmVersion( CommandParameter.END_SCM_VERSION, endRevision );
    }

    public String getDatePattern()
        throws ScmException
    {
        return parameters.getString( CommandParameter.CHANGELOG_DATE_PATTERN, null );
    }

    /**
     * @param datePattern the date pattern used in changelog output returned by scm tool
	 * @throws ScmException if any
     */
    public void setDatePattern( String datePattern )
        throws ScmException
    {
        parameters.setString( CommandParameter.CHANGELOG_DATE_PATTERN, datePattern );
    }

    public Integer getLimit()
        throws ScmException
    {
        final int limit = parameters.getInt( CommandParameter.LIMIT, -1 );
        return limit > 0 ? limit : null;
    }

    /**
     * @param limit the maximal count of returned changesets
	 * @throws ScmException if any
     */
    public void setLimit( Integer limit )
        throws ScmException
    {
        if ( limit != null )
        {
            parameters.setInt( CommandParameter.LIMIT, limit );
        }
        else
        {
            parameters.remove( CommandParameter.LIMIT );
        }
    }

    public void setDateRange( Date startDate, Date endDate )
        throws ScmException
    {
        setStartDate( startDate );
        setEndDate( endDate );
    }

    public void setRevision( ScmVersion revision )
        throws ScmException
    {
        parameters.setScmVersion( CommandParameter.SCM_VERSION, revision );
    }

    public ScmVersion getRevision()
        throws ScmException
    {
        return parameters.getScmVersion( CommandParameter.SCM_VERSION, null );
    }
}
