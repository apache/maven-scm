package org.apache.maven.scm.provider.jazz.command.add;

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
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;
import org.apache.maven.scm.provider.jazz.command.status.JazzStatusCommand;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

// RTC does not have the equivalent of the "add" goal. The closest we have is the "share" command, however
// that only shares directories (not individual or specific files), and it is recursive (which can not be
// switched off!).
//
// The Maven SCM plugin "add" goal's job is to add files to source control, but not commit them.
// The SVN equivalent of this is "svn add", which places a file under source control (Working Copy in svn terms)
// but does not commit them (svn commit).
// So, this provider will use the RTC "checkin" command for the implementation of the "add" goal.
// This will checkin the code into a remote repository workspace. It will not deliver.
//
// Additionally, "svn add" does not take a message, whereas commit does. Under RTC, the only way we can preserve
// the message, is to create a changeset. So we do that in the "checkin" goal, not the "add" goal.
// 
// The Maven SCM plugin "add" goal is roughly equivalent to the RTC "checkin" command.
//
// See the following links for additional information on the RTC "checkin" command.
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_checkin.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_checkin.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_checkin.html
//
// Currently this implementation does not use the comment message.
// Perhaps in the future this method can be used to deliver a change-set with the given comment message.
// However some users may want the checkin goal to only check in to the desired repository workspace.
// While some users may want the checkin goal to both check in to the desired repository workspace and deliver it to a
// stream.
// Currently this implementation only checks in the unresolved changes to the repository workspace.
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
public class JazzAddCommand
    extends AbstractAddCommand
{
    /**
     * {@inheritDoc}
     */
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing add command..." );
        }

        // The message can only be used when we create a change set, which is only done on the checkin command.
        // So that can be ignored.
        // The binary flag, is not needed for RTC.
        return executeAddCommand( repo, fileSet );
    }

    public AddScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        // NOTE: THIS IS ALSO CALLED DIRECTLY FROM THE CHECKIN COMMAND.
        //
        // The "checkin" command does not produce consumable output as to which individual files were checked in. (in
        // 2.0.0.2 at least). Since only "locally modified" changes get checked in, we call a "status" command to
        // generate a list of these files.
        File baseDir = fileSet.getBasedir();
        File parentFolder = ( baseDir.getParentFile() != null ) ? baseDir.getParentFile() : baseDir;

        List<ScmFile> changedScmFiles = new ArrayList<ScmFile>();
        List<File> changedFiles = new ArrayList<File>();
        List<ScmFile> commitedFiles = new ArrayList<ScmFile>();

        JazzStatusCommand statusCmd = new JazzStatusCommand();
        statusCmd.setLogger( getLogger() );
        StatusScmResult statusCmdResult = statusCmd.executeStatusCommand( repo, fileSet );
        List<ScmFile> statusScmFiles = statusCmdResult.getChangedFiles();

        for ( ScmFile file : statusScmFiles )
        {
            getLogger().debug( "Iterating over statusScmFiles: " + file );
            if ( file.getStatus() == ScmFileStatus.ADDED || file.getStatus() == ScmFileStatus.DELETED
                || file.getStatus() == ScmFileStatus.MODIFIED )
            {
                changedScmFiles.add( new ScmFile( file.getPath(), ScmFileStatus.CHECKED_IN ) );
                changedFiles.add( new File( parentFolder, file.getPath() ) );
            }
        }

        List<File> files = fileSet.getFileList();
        if ( files.size() == 0 )
        {
            // Either commit all local changes
            commitedFiles = changedScmFiles;
        }
        else
        {
            // Or commit specific files
            for ( File file : files )
            {
                if ( fileExistsInFileList( file, changedFiles ) )
                {
                    commitedFiles.add( new ScmFile( file.getPath(), ScmFileStatus.CHECKED_IN ) );
                }
            }
        }

        // Now that we have a list of files to process, we can "add" (scm checkin) them.
        JazzAddConsumer addConsumer = new JazzAddConsumer( repo, getLogger() );
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );
        JazzScmCommand command = createAddCommand( repo, fileSet );

        int status = command.execute( addConsumer, errConsumer );
        if ( status != 0 || errConsumer.hasBeenFed() )
        {
            return new AddScmResult( command.getCommandString(),
                                     "Error code for Jazz SCM add (checkin) command - " + status,
                                     errConsumer.getOutput(), false );
        }

        return new AddScmResult( command.getCommandString(), addConsumer.getFiles() );
    }

    public JazzScmCommand createAddCommand( ScmProviderRepository repo, ScmFileSet fileSet )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_CHECKIN, null, repo, false, fileSet, getLogger() );

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

    private boolean fileExistsInFileList( File file, List<File> fileList )
    {
        boolean exists = false;
        for ( File changedFile : fileList )
        {
            if ( changedFile.compareTo( file ) == 0 )
            {
                exists = true;
                break;
            }
        }
        return exists;
    }

}
