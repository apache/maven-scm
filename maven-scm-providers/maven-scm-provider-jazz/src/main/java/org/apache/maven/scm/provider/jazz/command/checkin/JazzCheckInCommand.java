package org.apache.maven.scm.provider.jazz.command.checkin;

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

import java.io.File;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.add.JazzAddCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.DebugLoggerConsumer;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;
import org.apache.maven.scm.provider.jazz.command.status.JazzStatusCommand;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

// The Maven SCM Plugin "checkin" goal is equivalent to the RTC "checkin" command.
//
// This implementation of the Maven SCM Plugin "checkin" goal creates a change set with the message provided.
// It then uses the Jazz "scm "checkin" command to check the files into a remote workspace.
// If there is a flow target defined and the pushChanges flag is true (the default), then the remote workspace
// will be delivered ("scm deliver") to the flow target (a stream or other workspace).
// 
// Set the pushChanges flag to false, if you do not want the repository workspace delivered.
//
// NOTE: At this point, only a SINGLE flow target is supported. Jazz itself, allows for more than one.
//
// The differences between this and the "add" goal, are:
//      - The add goal will only checkin into the remote repository workspace.
//      - The add goal will never deliver.
//      - The add goal does not create a change set.
//
// This is the best we can do to mimic the implementations of the other providers, that provide a working
// "add" function (eg "svn add").
//
// Add may have had been able to use the "scm share" command, but that is recusive and only takes directory
// names; we are not able to specify specific or single files.
//
// See the following links for additional information on the RTC "checkin" command.
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_checkin.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_checkin.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_checkin.html
//
// See the following links for additional information on the RTC "deliver" command.
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_deliver.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_deliver.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_deliver.html
//

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzCheckInCommand
    extends AbstractCheckInCommand
{

    /**
     * {@inheritDoc}
     */
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                      String message, ScmVersion scmVersion )
        throws ScmException
    {
        if ( scmVersion != null && StringUtils.isNotEmpty( scmVersion.getName() ) )
        {
            throw new ScmException( "This provider command can't handle tags." );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing checkin command..." );
        }

        // Create a changeset. We need to do this, as otherwise the information contained in the message
        // will be lost forever.
        JazzScmCommand createChangesetCmd = createCreateChangesetCommand( repository, fileSet, message );
        DebugLoggerConsumer outputConsumer = new DebugLoggerConsumer( getLogger() );
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );

        int status = createChangesetCmd.execute( outputConsumer, errConsumer );
        if ( status != 0 )
        {
            return new CheckInScmResult( createChangesetCmd.getCommandString(),
                                         "Error code for Jazz SCM create changeset command - " + status,
                                         errConsumer.getOutput(), false );
        }

        // As we just created a change set, we now need to call the status command so we can parse the 
        // newly created change set.

        JazzStatusCommand statusCommand = new JazzStatusCommand();
        statusCommand.setLogger( getLogger() );
        statusCommand.executeStatusCommand( repository, fileSet );

        // NOTE: For isPushChangesAndHaveFlowTargets() to work, a scm status call must have been called first!!!
        // As the Workspace name and alias, and the Flow Target name and alias are needed.
        
        // Check to see if we've got a flow target and had a workItem defined (via -DworkItem=XXXX)
        JazzScmProviderRepository jazzRepo = (JazzScmProviderRepository) repository;
        if ( jazzRepo.isPushChangesAndHaveFlowTargets() && StringUtils.isNotEmpty( jazzRepo.getWorkItem() ) )
        {
            List<Integer> changeSetAliases = jazzRepo.getOutgoingChangeSetAliases();
            if ( changeSetAliases != null && !changeSetAliases.isEmpty() )
            {
                for ( Integer changeSetAlias : changeSetAliases )
                {
                    // Associate a work item if we need too.
                    JazzScmCommand changesetAssociateCmd = createChangesetAssociateCommand( repository, 
                        changeSetAlias );
                    outputConsumer = new DebugLoggerConsumer( getLogger() );
                    errConsumer = new ErrorConsumer( getLogger() );
        
                    status = changesetAssociateCmd.execute( outputConsumer, errConsumer );
                    if ( status != 0 )
                    {
                        return new CheckInScmResult( changesetAssociateCmd.getCommandString(),
                                                     "Error code for Jazz SCM changeset associate command - " + status,
                                                     errConsumer.getOutput(), false );
                    }
                }
            }
        }
        
        // Now check in the files themselves.
        return executeCheckInCommand( repository, fileSet, scmVersion );
    }

    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                      ScmVersion scmVersion )
        throws ScmException
    {
        // Call the Add command to perform the checkin into the repository workspace.
        JazzAddCommand addCommand = new JazzAddCommand();
        addCommand.setLogger( getLogger() );
        AddScmResult addResult = addCommand.executeAddCommand( repo, fileSet );

        // Now, if it has a flow target, deliver it.
        JazzScmProviderRepository jazzRepo = (JazzScmProviderRepository) repo;
        if ( jazzRepo.isPushChangesAndHaveFlowTargets() )
        {
            // Push if we need too
            JazzScmCommand deliverCmd = createDeliverCommand( (JazzScmProviderRepository) repo, fileSet );
            StreamConsumer deliverConsumer =
                new DebugLoggerConsumer( getLogger() );      // No need for a dedicated consumer for this
            ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );

            int status = deliverCmd.execute( deliverConsumer, errConsumer );
            if ( status != 0 )
            {
                return new CheckInScmResult( deliverCmd.getCommandString(),
                                             "Error code for Jazz SCM deliver command - " + status,
                                             errConsumer.getOutput(), false );
            }
        }

        // Return what was added.
        return new CheckInScmResult( addResult.getCommandLine(), addResult.getAddedFiles() );
    }

    public JazzScmCommand createCreateChangesetCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_CREATE, JazzConstants.CMD_SUB_CHANGESET, repo, false, fileSet,
                                getLogger() );
        command.addArgument( message );

        return command;
    }

    public JazzScmCommand createChangesetAssociateCommand( ScmProviderRepository repo, Integer changeSetAlias )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_CHANGESET, JazzConstants.CMD_SUB_ASSOCIATE, repo, false, null,
                                getLogger() );
        // Add the change set alias
        JazzScmProviderRepository jazzRepo = (JazzScmProviderRepository) repo;
        command.addArgument( changeSetAlias.toString() );
        // Add the work item number
        command.addArgument( jazzRepo.getWorkItem() );
        return command;
    }

    public JazzScmCommand createCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_CHECKIN, null, repo, false, fileSet, getLogger() );

        // TODO, this was taken out to quickly test how the release plugin works.
        // The release plugin has the fileSet.getbaseDir() as the project it is checking in
        // This happens to be a folder under the sandbox root, and so the checkin would fail because it needs
        // to check in at the sandbox root level (not sub folders)
        // The SCM Plugin has a basedir parameter that you can pass it, so everythig works ok from the scm-plugin alone
        // but the release-plugin doesn't look like it lets you do that. (or I didn't have enough time
        // to figure out how to do it properly).

        // if (fileSet != null) {
        // command.addArgument(JazzConstants.LOAD_ROOT_DIRECTORY_ARG);
        // command.addArgument(fileSet.getBasedir().getAbsolutePath());
        // }

        List<File> files = fileSet.getFileList();
        if ( files != null && !files.isEmpty() )
        {
            for ( File file : files )
            {
                command.addArgument( file.getPath() ); // Check in only the files specified
            }
        }
        else
        {
            command.addArgument( "." ); // This will check in all local changes
        }

        return command;
    }

    // Create the JazzScmCommand to execute the "scm deliver ..." command
    // This will deliver the changes to the flow target (stream or other workspace).
    public JazzScmCommand createDeliverCommand( JazzScmProviderRepository repo, ScmFileSet fileSet )
    {
        JazzScmCommand command = new JazzScmCommand( JazzConstants.CMD_DELIVER, repo, fileSet, getLogger() );

        if ( repo.getWorkspace() != null && !repo.getWorkspace().equals( "" ) )
        {
            command.addArgument( JazzConstants.ARG_DELIVER_SOURCE );
            command.addArgument( repo.getWorkspace() );
        }

        if ( repo.getFlowTarget() != null && !repo.getFlowTarget().equals( "" ) )
        {
            command.addArgument( JazzConstants.ARG_DELIVER_TARGET );
            command.addArgument( repo.getFlowTarget() );
        }

        // This command is needed so that the deliver operation will work.
        // Files that are not under source control (a--) [temp files etc]
        // will cause the deliver operation to fail with the error:
        // "Cannot deliver because there are one or more items that are not checked in.
        // Check in the changes or rerun with --overwrite-uncommitted."
        // However, from the maven perspective, we only need files that are
        // under source control to be delivered. Maven has already checked
        // for this (via the status command). 
        //
        // So we add this argument to allow the deliver to work.
        command.addArgument( JazzConstants.ARG_OVERWRITE_UNCOMMITTED );

        return command;
    }
}
