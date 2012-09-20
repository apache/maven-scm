package org.apache.maven.scm.provider.bazaar.command.changelog;

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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import org.apache.maven.scm.provider.bazaar.BazaarUtils;
import org.apache.maven.scm.provider.bazaar.command.BazaarConstants;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjorn Eikli Smorgrav</a>
 * @author Olivier Lamy
 *
 */
public class BazaarChangeLogCommand
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

    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern )
        throws ScmException
    {
        return executeChangeLogCommand( fileSet, startDate, endDate, datePattern, null );
    }

    private ChangeLogScmResult executeChangeLogCommand( ScmFileSet fileSet,
                                                        Date startDate, Date endDate,
                                                        String datePattern, Integer limit )
        throws ScmException
    {
        List<String> cmd = new ArrayList<String>();
        cmd.addAll( Arrays.asList( BazaarConstants.LOG_CMD, BazaarConstants.VERBOSE_OPTION ) );
        if ( limit != null && limit > 0 )
        {
            cmd.add( BazaarConstants.LIMIT_OPTION );
            cmd.add( Integer.toString( limit ) );
        }

        BazaarChangeLogConsumer consumer = new BazaarChangeLogConsumer( getLogger(), datePattern );
        ScmResult result = BazaarUtils.execute( consumer, getLogger(), fileSet.getBasedir(),
            cmd.toArray( new String[cmd.size()] ) );

        List<ChangeSet> logEntries = consumer.getModifications();
        List<ChangeSet> inRangeAndValid = new ArrayList<ChangeSet>();
        startDate = startDate == null ? new Date( 0 ) : startDate; //From 1. Jan 1970
        endDate = endDate == null ? new Date() : endDate; //Upto now

        for ( ChangeSet change : logEntries )
        {
            if ( change.getFiles().size() > 0 )
            {
                if ( !change.getDate().before( startDate ) && !change.getDate().after( endDate ) )
                {
                    inRangeAndValid.add( change );
                }
            }
        }

        ChangeLogSet changeLogSet = new ChangeLogSet( inRangeAndValid, startDate, endDate );
        return new ChangeLogScmResult( changeLogSet, result );
    }
}
