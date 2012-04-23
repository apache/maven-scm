package org.apache.maven.scm.provider.jazz.command.changelog;

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
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

// To get a changelog, we need to get a list of changesets (scm history), and then for each changeset listed,
// get the details of each changeset (scm list changesets X, Y, Z).
// 
// We do not appear to be able to get a list of changes between a range of dates, so all of them are returned.
//
// See the following links for additional information on the RTC "history" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_history.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_history.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_history.html
//
//
// See the following links for additional information on the RTC "list changesets" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_list.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_list.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_list.html
//

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzChangeLogCommand
    extends AbstractChangeLogCommand
{
    /**
     * {@inheritDoc}
     */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern )
        throws ScmException
    {
        if ( branch != null && StringUtils.isNotEmpty( branch.getName() ) )
        {
            throw new ScmException( "This SCM provider doesn't support branches." );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing changelog command..." );
        }

        // This acts as a two phase operation.
        // The first pass is to call the "scm history" command to get a list
        // of the changeSets from Jazz SCM. It is stored in the revision of the
        // changeSets array.
        List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
        JazzScmCommand historyCommand = createHistoryCommand( repo, fileSet );
        JazzHistoryConsumer changeLogConsumer = new JazzHistoryConsumer( repo, getLogger(), changeSets );
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );
        int status = historyCommand.execute( changeLogConsumer, errConsumer );
        if ( status != 0 || errConsumer.hasBeenFed() )
        {
            return new ChangeLogScmResult( historyCommand.getCommandString(),
                                           "Error code for Jazz SCM history command - " + status,
                                           errConsumer.getOutput(), false );
        }

        // Now, call the "scm list changesets" command, passing in the list of changesets from the first pass.
        JazzScmCommand listChangesetsCommand = createListChangesetCommand( repo, fileSet, changeSets );
        JazzListChangesetConsumer listChangesetConsumer = new JazzListChangesetConsumer( repo, getLogger(), changeSets, datePattern );
        errConsumer = new ErrorConsumer( getLogger() );
        status = listChangesetsCommand.execute( listChangesetConsumer, errConsumer );
        if ( status != 0 || errConsumer.hasBeenFed() )
        {
            return new ChangeLogScmResult( listChangesetsCommand.getCommandString(),
                                           "Error code for Jazz SCM list changesets command - " + status,
                                           errConsumer.getOutput(), false );
        }

        // Build the result and return it.
        ChangeLogSet changeLogSet = new ChangeLogSet( changeSets, startDate, endDate );

        // Return the "main" command used, namely "scm history"
        return new ChangeLogScmResult( historyCommand.getCommandString(), changeLogSet );
    }

    protected JazzScmCommand createHistoryCommand( ScmProviderRepository repo, ScmFileSet fileSet )
    {
        JazzScmCommand command = new JazzScmCommand( JazzConstants.CMD_HISTORY, repo, fileSet, getLogger() );
        command.addArgument( JazzConstants.ARG_MAXIMUM );
        command.addArgument( "10000000" );      // Beyond me as to why they didn't make 0 = all.
                                                // And just to really annoy us, it defaults to 10.
                                                // So we put something stupidly large in there instead.

        return command;
    }

    protected JazzScmCommand createListChangesetCommand( ScmProviderRepository repo, ScmFileSet fileSet, List<ChangeSet> changeSets )
    {
        JazzScmProviderRepository jazzRepo = (JazzScmProviderRepository) repo;
        JazzScmCommand command = new JazzScmCommand( JazzConstants.CMD_LIST, JazzConstants.CMD_SUB_CHANGESETS, repo, fileSet, getLogger() );
        command.addArgument( JazzConstants.ARG_WORKSPACE );
        command.addArgument( jazzRepo.getWorkspace() );
        for (int i=0; i<changeSets.size(); i++)
        {
            command.addArgument( changeSets.get( i ).getRevision() );
        }
        return command;
    }
}
