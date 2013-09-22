package org.apache.maven.scm.provider.hg.command.changelog;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @author Olivier Lamy
 *
 */
public class HgChangeLogCommand
    extends AbstractChangeLogCommand
    implements Command
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected ChangeLogScmResult executeChangeLogCommand( ChangeLogScmRequest request )
            throws ScmException
    {
        final ScmVersion startVersion = request.getStartRevision();
        final ScmVersion endVersion = request.getEndRevision();
        final ScmFileSet fileSet = request.getScmFileSet();
        final String datePattern = request.getDatePattern();
        if ( startVersion != null || endVersion != null )
        {
            final ScmProviderRepository scmProviderRepository = request.getScmRepository().getProviderRepository();
            return executeChangeLogCommand( scmProviderRepository, fileSet, startVersion, endVersion, datePattern );
        }
        return executeChangeLogCommand( fileSet, request.getStartDate(), request.getEndDate(),
            datePattern, request.getLimit() );
    }

    /**
     * {@inheritDoc}
     */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository scmProviderRepository,
                                                          ScmFileSet fileSet, Date startDate, Date endDate,
                                                          ScmBranch branch, String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( fileSet, startDate, endDate, datePattern, null );
    }

    private ChangeLogScmResult executeChangeLogCommand( ScmFileSet fileSet, Date startDate, Date endDate,
                                                          String datePattern, Integer limit )
        throws ScmException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
        StringBuilder dateInterval = new StringBuilder();
        // TRICK: Mercurial 1.9.3 don't accept 1970-01-01
        dateInterval.append(
            dateFormat.format( startDate == null ? new Date( 1000L * 60 * 60 * 24 ) : startDate ) ); // From 2. Jan 1970
        dateInterval.append( " to " );
        dateInterval.append( dateFormat.format( endDate == null ? new Date() : endDate ) ); // Upto now

        List<String> cmd = new ArrayList<String>();
        cmd.addAll( Arrays.asList( HgCommandConstants.LOG_CMD, HgCommandConstants.TEMPLATE_OPTION, HgCommandConstants.TEMPLATE_FORMAT,
                                   HgCommandConstants.NO_MERGES_OPTION, HgCommandConstants.DATE_OPTION,
                                   dateInterval.toString() ) );

        if ( limit != null && limit > 0 )
        {
            cmd.add( HgCommandConstants.LIMIT_OPTION );
            cmd.add( Integer.toString( limit ) );
        }

        HgChangeLogConsumer consumer = new HgChangeLogConsumer( getLogger(), datePattern );
        ScmResult result = HgUtils.execute( consumer, getLogger(), fileSet.getBasedir(), cmd.toArray( new String[ cmd.size() ] ) );

        List<ChangeSet> logEntries = consumer.getModifications();
        ChangeLogSet changeLogSet = new ChangeLogSet( logEntries, startDate, endDate );
        return new ChangeLogScmResult( changeLogSet, result );
    }

    @Override
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                          ScmVersion startVersion, ScmVersion endVersion,
                                                          String datePattern )
        throws ScmException
    {
        StringBuilder revisionInterval = new StringBuilder();
        if ( startVersion != null )
        {
            revisionInterval.append( startVersion.getName() );
        }
        revisionInterval.append( ":" );
        if ( endVersion != null )
        {
            revisionInterval.append( endVersion.getName() );
        }

        String[] cmd = new String[]{ HgCommandConstants.LOG_CMD, HgCommandConstants.TEMPLATE_OPTION, HgCommandConstants.TEMPLATE_FORMAT,
            HgCommandConstants.NO_MERGES_OPTION, HgCommandConstants.REVISION_OPTION, revisionInterval.toString() };
        HgChangeLogConsumer consumer = new HgChangeLogConsumer( getLogger(), datePattern );
        ScmResult result = HgUtils.execute( consumer, getLogger(), fileSet.getBasedir(), cmd );

        List<ChangeSet> logEntries = consumer.getModifications();
        Date startDate = null;
        Date endDate = null;
        if ( !logEntries.isEmpty() )
        {
            startDate = logEntries.get( 0 ).getDate();
            endDate = logEntries.get( logEntries.size() - 1 ).getDate();
        }
        ChangeLogSet changeLogSet = new ChangeLogSet( logEntries, startDate, endDate );
        changeLogSet.setStartVersion( startVersion );
        changeLogSet.setEndVersion( endVersion );
        return new ChangeLogScmResult( changeLogSet, result );
    }
}
