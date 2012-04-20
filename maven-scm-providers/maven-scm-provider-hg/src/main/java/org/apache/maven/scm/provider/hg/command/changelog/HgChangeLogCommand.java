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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class HgChangeLogCommand
    extends AbstractChangeLogCommand
    implements Command
{
    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository scmProviderRepository,
                                                          ScmFileSet fileSet, Date startDate, Date endDate,
                                                          ScmBranch branch, String datePattern )
        throws ScmException
    {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder dateInterval = new StringBuilder();
        // TRICK: Mercurial 1.9.3 don't accept 1970-01-01
        dateInterval.append(dateFormat.format(
                startDate == null ? new Date( 1000L*60*60*24) : startDate)); // From 2. Jan 1970
        dateInterval.append(" to ");
        dateInterval.append(dateFormat.format(endDate == null ? new Date() : endDate)); // Upto now
        
        String[] cmd = new String[] { HgCommandConstants.LOG_CMD,
                HgCommandConstants.VERBOSE_OPTION,
                HgCommandConstants.NO_MERGES_OPTION,
                HgCommandConstants.DATE_OPTION,
                dateInterval.toString()
                };
        HgChangeLogConsumer consumer = new HgChangeLogConsumer( getLogger(), datePattern );
        ScmResult result = HgUtils.execute( consumer, getLogger(), fileSet.getBasedir(), cmd );

        List<ChangeSet> logEntries = consumer.getModifications();
        ChangeLogSet changeLogSet = new ChangeLogSet( logEntries, startDate, endDate );
        return new ChangeLogScmResult( changeLogSet, result );
    }
}
