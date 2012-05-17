package org.apache.maven.scm.provider.jazz.command.unedit;

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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.unedit.AbstractUnEditCommand;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.DebugLoggerConsumer;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;

// In RTC the need to 'edit' or 'lock' a file is not required. It is actually encouraged to not 
// lock 'text' based files and to only lock binary file types.
//
// The Maven SCM plugin "edit" goal has been implemented by using the RTC "lock acquire/release" commands. 
//
// See the following links for additional information on the RTC "lock acquire" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_lock_acquire.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_lock_acquire.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_lock_acquire.html
//
// See the following links for additional information on the RTC "lock release" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_lock_release.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_lock_release.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_lock_release.html
//

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzUnEditCommand
    extends AbstractUnEditCommand
{
    /**
     * {@inheritDoc}
     */
    protected ScmResult executeUnEditCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing unedit command..." );
        }

        DebugLoggerConsumer uneditConsumer = new DebugLoggerConsumer( getLogger() );
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );

        JazzScmCommand uneditCmd = createUneditCommand( repo, fileSet );
        int status = uneditCmd.execute( uneditConsumer, errConsumer );

        if ( status != 0 || errConsumer.hasBeenFed() )
        {
            return new UnEditScmResult( uneditCmd.getCommandString(),
                                        "Error code for Jazz SCM unedit command - " + status, errConsumer.getOutput(),
                                        false );
        }

        return new UnEditScmResult( uneditCmd.getCommandString(), "Successfully Completed.", uneditConsumer.getOutput(),
                                    true );
    }

    public JazzScmCommand createUneditCommand( ScmProviderRepository repo, ScmFileSet fileSet )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_LOCK, JazzConstants.CMD_SUB_RELEASE, repo, fileSet, getLogger() );

        List<File> files = fileSet.getFileList();
        if ( files != null && !files.isEmpty() )
        {
            for ( File file : files )
            {
                command.addArgument( file.getPath() ); // Un-Lock only the files specified
            }
        }
        else
        {
            command.addArgument( "." ); // Un-Lock all files
        }

        return command;
    }
}
