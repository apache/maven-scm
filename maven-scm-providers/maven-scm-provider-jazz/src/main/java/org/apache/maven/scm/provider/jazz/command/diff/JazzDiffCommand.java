package org.apache.maven.scm.provider.jazz.command.diff;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.DebugLoggerConsumer;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;
import org.apache.maven.scm.provider.jazz.command.status.JazzStatusCommand;

// The Maven SCM plugin "diff" goal may have different interpretations in RTC depending on how
// the user is using RTC. In one instance, the user may expect the diff to report back on the differences between
// the local 'sandbox' and their connected repository workspace (ie. What files are 'unresolved'). 
// Other users may want the diff the report back the differences between their connected repository workspace
// and the stream that it flows with (ie. What files are 'outgoing' / 'incoming').
// As a first step, we would have to figure out how to distinguish between these two use cases when using this goal.

// Whilst, the above is true, based upon the SVN implementation, its diff does a difference
// between the local working copy (sandbox) vs's repository (workspace repository).
//
// So this implementation will compare the sandbox with the workspace repository (even if there is
// a valid flow target). As the "scm diff" command does not support this direct comparison (I have
// had an Enhancement Work Item opened to do so), we will call the "scm status" command to get all
// of the change files, and then iterate through all of them to get a diff of all of them. The combined
// output of all of the various diffs will then be returned as a single output operation.
// -Chris 24/02/12

// The following RTC commands may be useful however it retrieving the required information. 
//
// 1. RTC "compare" command:  Compare two workspaces/streams/baselines/snapshots, showing differing baselines and change sets. 
// See the following links for additional information on the RTC "compare" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_compare.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_compare.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_compare.html
//
// 2. RTC "diff" command:  Compare two states of a file. 
// See the following links for additional information on the RTC "diff" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_diff.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_diff.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_diff.html
//
// 3. RTC "status" command:  Show modification status of items in a workspace. 
// See the following links for additional information on the RTC "status" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_status.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_status.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_status.html
//

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzDiffCommand
    extends AbstractDiffCommand
{
    /**
     * {@inheritDoc}
     */
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                ScmVersion startRevision, ScmVersion endRevision )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing diff command..." );
        }

        File baseDir = fileSet.getBasedir();
        File parentFolder = ( baseDir.getParentFile() != null ) ? baseDir.getParentFile() : baseDir;

        // First execute the status command to get the list of changed files.
        JazzStatusCommand statusCmd = new JazzStatusCommand();
        statusCmd.setLogger( getLogger() );
        StatusScmResult statusCmdResult = statusCmd.executeStatusCommand( repo, fileSet );
        List<ScmFile> statusScmFiles = statusCmdResult.getChangedFiles();

        // In this case, we also use it across multiple calls to "scm diff" so that we
        // sum all output into on.
        JazzScmCommand diffCmd = null;
        StringBuilder patch = new StringBuilder();
        Map<String, CharSequence> differences = new HashMap<String, CharSequence>();

        // Now lets iterate through them
        for ( ScmFile file : statusScmFiles )
        {
            if ( file.getStatus() == ScmFileStatus.MODIFIED )
            {
                // The "scm status" command returns files relative to the sandbox root.
                // Whereas the "scm diff" command needs them relative to the working directory.
                File fullPath = new File( parentFolder, file.getPath() );
                String relativePath = fullPath.toString().substring( baseDir.toString().length() );
                getLogger().debug( "Full Path     : '" + fullPath + "'" );
                getLogger().debug( "Relative Path : '" + relativePath + "'" );

                // Now call "scm diff on it"
                // In this case, we use the DebugLoggerConsumer's ability to store captured output
                DebugLoggerConsumer diffConsumer = new DebugLoggerConsumer( getLogger() );
                ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );
                diffCmd = createDiffCommand( repo, fileSet, relativePath );
                int status = diffCmd.execute( diffConsumer, errConsumer );
                if ( status != 0 || errConsumer.hasBeenFed() )
                {
                    // Return a false result (not the usual SCMResult)
                    return new DiffScmResult( diffCmd.toString(), "The scm diff command failed.",
                                              errConsumer.getOutput(), false );
                }
                // Append to patch (all combined)
                patch.append( diffConsumer.getOutput() );
                // Set the differences map <File, <CharSequence>
                differences.put( relativePath, diffConsumer.getOutput() );
            }
        }

        return new DiffScmResult( diffCmd.toString(), statusCmdResult.getChangedFiles(), differences,
                                  patch.toString() );
    }

    public JazzScmCommand createDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet, String relativePath )
    {
        JazzScmCommand command = new JazzScmCommand( JazzConstants.CMD_DIFF, repo, fileSet, getLogger() );
        command.addArgument( JazzConstants.ARG_FILE );
        command.addArgument( relativePath );
        return command;
    }
}
