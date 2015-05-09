package org.apache.maven.scm.provider.jazz.command.tag;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.DebugLoggerConsumer;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// The Maven SCM Plugin "tag" goal is equivalent to the RTC "create snapshot" command.
//
// Once the tag (snapshot in RTC terms) has been created, a repository workspace is then created
// based upon that snapshot. This is done to allow the checkout of a tag (maven release plugin) to function.
// As, currently, the underlying scm command does not allow us to check out (load in RTC terms) a snapshot directly.
//
// See the following links for additional information on the RTC "create snapshot" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_create.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_create.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_create.html
//
// See the following links for additional information on the RTC "deliver" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_deliver.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_deliver.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_deliver.html

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzTagCommand
    extends AbstractTagCommand
{
    /**
     * {@inheritDoc}
     */
    protected ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag,
                                           ScmTagParameters scmTagParameters )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing tag command..." );
        }

        JazzScmProviderRepository jazzRepo = (JazzScmProviderRepository) repo;

        getLogger().debug( "Creating Snapshot..." );
        StreamConsumer tagConsumer =
            new DebugLoggerConsumer( getLogger() );      // No need for a dedicated consumer for this
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );
        JazzScmCommand tagCreateSnapshotCmd =
            createTagCreateSnapshotCommand( jazzRepo, fileSet, tag, scmTagParameters );
        int status = tagCreateSnapshotCmd.execute( tagConsumer, errConsumer );

        if ( status != 0 )
        {
            return new TagScmResult( tagCreateSnapshotCmd.getCommandString(),
                                     "Error code for Jazz SCM tag (SNAPSHOT) command - " + status,
                                     errConsumer.getOutput(), false );
        }

        // ------------------------------------------------------------------
        // We create the workspace based on the tag here, as the scm tool
        // can not currently check directly out from a snapshot (only a workspace).
        getLogger().debug( "Creating Workspace from Snapshot..." );
        JazzScmCommand tagCreateWorkspaceCmd = createTagCreateWorkspaceCommand( jazzRepo, fileSet, tag );
        errConsumer = new ErrorConsumer( getLogger() );
        status = tagCreateWorkspaceCmd.execute( tagConsumer, errConsumer );

        if ( status != 0 )
        {
            return new TagScmResult( tagCreateWorkspaceCmd.getCommandString(),
                                     "Error code for Jazz SCM tag (WORKSPACE) command - " + status,
                                     errConsumer.getOutput(), false );
        }
        // ------------------------------------------------------------------

        if ( jazzRepo.isPushChangesAndHaveFlowTargets() )
        {
            // isPushChanges = true, and we have something to deliver and promote to.
            getLogger().debug( "Promoting and delivering..." );

            // So we deliver the code to the target stream (or workspace)
            getLogger().debug( "Delivering..." );
            JazzScmCommand tagDeliverCommand = createTagDeliverCommand( jazzRepo, fileSet, tag );
            errConsumer = new ErrorConsumer( getLogger() );
            status = tagDeliverCommand.execute( tagConsumer, errConsumer );
            if ( status != 0 )
            {
                return new TagScmResult( tagDeliverCommand.getCommandString(),
                                         "Error code for Jazz SCM deliver command - " + status, errConsumer.getOutput(),
                                         false );
            }

            // And now we promote the snapshot to the target stream (or workspace)
            getLogger().debug( "Promoting snapshot..." );
            JazzScmCommand tagSnapshotPromoteCommand = createTagSnapshotPromoteCommand( jazzRepo, fileSet, tag );
            errConsumer = new ErrorConsumer( getLogger() );
            status = tagSnapshotPromoteCommand.execute( tagConsumer, errConsumer );
            if ( status != 0 )
            {
                return new TagScmResult( tagSnapshotPromoteCommand.getCommandString(),
                                         "Error code for Jazz SCM snapshot promote command - " + status,
                                         errConsumer.getOutput(), false );
            }
        }

        // We don't have a JazzTagConsumer so just build up all the files...
        List<ScmFile> taggedFiles = new ArrayList<ScmFile>( fileSet.getFileList().size() );
        for ( File f : fileSet.getFileList() )
        {
            taggedFiles.add( new ScmFile( f.getPath(), ScmFileStatus.TAGGED ) );
        }

        // We return the "main" or "primary" command executed.
        // This is similar to the git provider, where the main command is returned.
        // So we return tagSnapshotCmd and not tagWorkspaceCmd.
        return new TagScmResult( tagCreateSnapshotCmd.getCommandString(), taggedFiles );
    }

    // Create the JazzScmCommand to execute the "scm create snapshot ..." command
    // This will create a snapshot of the remote repository
    public JazzScmCommand createTagCreateSnapshotCommand( JazzScmProviderRepository repo, ScmFileSet fileSet,
                                                          String tag, ScmTagParameters scmTagParameters )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_CREATE, JazzConstants.CMD_SUB_SNAPSHOT, repo, fileSet, getLogger() );

        if ( tag != null && !tag.trim().equals( "" ) )
        {
            command.addArgument( JazzConstants.ARG_SNAPSHOT_NAME );
            command.addArgument( tag );
        }

        String message = scmTagParameters.getMessage();
        if ( message != null && !message.trim().equals( "" ) )
        {
            command.addArgument( JazzConstants.ARG_SNAPSHOT_DESCRIPTION );
            command.addArgument( message );
        }

        command.addArgument( repo.getRepositoryWorkspace() );

        return command;
    }

    // Create the JazzScmCommand to execute the "scm snapshot promote ..." command
    // This will promote the snapshot to the flow target (the stream or other workspace).
    public JazzScmCommand createTagSnapshotPromoteCommand( JazzScmProviderRepository repo, ScmFileSet fileSet,
                                                           String tag )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_SNAPSHOT, JazzConstants.CMD_SUB_PROMOTE, repo, fileSet, getLogger() );

        if ( repo.getFlowTarget() != null && !repo.getFlowTarget().equals( "" ) )
        {
            command.addArgument( repo.getFlowTarget() );
        }
        if ( tag != null && !tag.trim().equals( "" ) )
        {
            command.addArgument( tag );
        }

        return command;
    }

    // Create the JazzScmCommand to execute the "scm deliver ..." command
    // This will deliver the changes to the flow target (stream or other workspace).
    public JazzScmCommand createTagDeliverCommand( JazzScmProviderRepository repo, ScmFileSet fileSet, String tag )
    {
        JazzScmCommand command = new JazzScmCommand( JazzConstants.CMD_DELIVER, repo, fileSet, getLogger() );

        if ( repo.getWorkspace() != null && !repo.getWorkspace().equals( "" ) )
        {
            // Don't deliver from the workspace, as it has the release.properties etc files in it
            // and jazz will choke on them, so use the workspace that we just created (tag) instead.
            command.addArgument( JazzConstants.ARG_DELIVER_SOURCE );
            command.addArgument( tag );
        }

        if ( repo.getFlowTarget() != null && !repo.getFlowTarget().equals( "" ) )
        {
            command.addArgument( JazzConstants.ARG_DELIVER_TARGET );
            command.addArgument( repo.getFlowTarget() );
        }

        return command;
    }

    // Create the JazzScmCommand to execute the "scm create workspace ..." command
    // This will create a workspace of the same name as the tag.
    public JazzScmCommand createTagCreateWorkspaceCommand( JazzScmProviderRepository repo, ScmFileSet fileSet,
                                                           String tag )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_CREATE, JazzConstants.CMD_SUB_WORKSPACE, repo, fileSet, getLogger() );

        if ( tag != null && !tag.trim().equals( "" ) )
        {
            command.addArgument( tag );
            command.addArgument( JazzConstants.ARG_WORKSPACE_SNAPSHOT );
            command.addArgument( tag );
        }

        return command;
    }

}
