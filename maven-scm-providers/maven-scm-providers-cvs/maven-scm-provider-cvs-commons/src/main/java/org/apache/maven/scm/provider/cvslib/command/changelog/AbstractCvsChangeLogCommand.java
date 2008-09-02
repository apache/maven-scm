package org.apache.maven.scm.provider.cvslib.command.changelog;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.util.CvsUtil;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractCvsChangeLogCommand
    extends AbstractChangeLogCommand
    implements CvsCommand
{
    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          ScmVersion startVersion, ScmVersion endVersion,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, null, null, null, startVersion, endVersion, datePattern );
    }

    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( repo, fileSet, startDate, endDate, branch, null, null, datePattern );
    }

    private ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet, Date startDate,
                                                        Date endDate, ScmBranch branch, ScmVersion startVersion,
                                                        ScmVersion endVersion, String datePattern )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "log", repository, fileSet );

        if ( startDate != null )
        {
            SimpleDateFormat outputDate = new SimpleDateFormat( getDateFormat() );

            String dateRange;

            if ( endDate == null )
            {
                dateRange = ">" + outputDate.format( startDate );
            }
            else
            {
                dateRange = outputDate.format( startDate ) + "<" + outputDate.format( endDate );
            }

            cl.createArg().setValue( "-d" );

            addDateRangeParameter( cl, dateRange );
        }

        if ( branch != null && StringUtils.isNotEmpty( branch.getName() ) )
        {
            cl.createArg().setValue( "-r" + branch.getName() );
        }

        if ( startVersion != null  || endVersion != null )
        {
            StringBuffer sb = new StringBuffer();
            sb.append( "-r" );
            if ( startVersion != null && StringUtils.isNotEmpty( startVersion.getName() ) )
            {
                sb.append( startVersion.getName() );
            }
            sb.append( "::" );
            if ( endVersion != null && StringUtils.isNotEmpty( endVersion.getName() ) )
            {
                sb.append( endVersion.getName() );
            }

            cl.createArg().setValue( sb.toString() );
        }

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        return executeCvsCommand( cl, startDate, endDate, startVersion, endVersion, datePattern );
    }

    protected abstract ChangeLogScmResult executeCvsCommand( Commandline cl, Date startDate, Date endDate,
                                                             ScmVersion startVersion, ScmVersion endVersion,
                                                             String datePattern )
        throws ScmException;

    protected String getDateFormat()
    {
        return CvsUtil.getSettings().getChangeLogCommandDateFormat();
    }

    protected void addDateRangeParameter( Commandline cl, String dateRange )
    {
        // There's a difference between UNIX-like OS and Windows
        // See http://jira.codehaus.org/browse/SCM-187
        if ( Os.isFamily( "windows" ) )
        {
            cl.createArg().setValue( "\"" + dateRange + "\"" );
        }
        else
        {
            cl.createArg().setValue( dateRange );
        }
    }
}
