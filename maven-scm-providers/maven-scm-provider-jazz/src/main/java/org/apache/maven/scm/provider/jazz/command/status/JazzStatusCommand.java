package org.apache.maven.scm.provider.jazz.command.status;

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
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;

//
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
public class JazzStatusCommand
    extends AbstractStatusCommand
{
    /**
     * {@inheritDoc}
     */
    public StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing status command..." );
        }

        JazzStatusConsumer statusConsumer = new JazzStatusConsumer( repo, getLogger() );
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );

        JazzScmCommand statusCmd = createStatusCommand( repo, fileSet );
        int status = statusCmd.execute( statusConsumer, errConsumer );
        if ( status != 0 )
        {
            return new StatusScmResult( statusCmd.getCommandString(),
                                        "Error code for Jazz SCM status command - " + status, errConsumer.getOutput(),
                                        false );
        }

        if ( getLogger().isDebugEnabled() )
        {
            if ( !statusConsumer.getChangedFiles().isEmpty() )
            {
                getLogger().debug( "Iterating over \"Status\" results" );
                for ( ScmFile file : statusConsumer.getChangedFiles() )
                {
                    getLogger().debug( file.getPath() + " : " + file.getStatus() );
                }
            }
            else
            {
                getLogger().debug( "There are no differences" );
            }
        }

        return new StatusScmResult( statusCmd.getCommandString(), statusConsumer.getChangedFiles() );
    }

    public JazzScmCommand createStatusCommand( ScmProviderRepository repo, ScmFileSet fileSet )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_STATUS, null, repo, false, fileSet, getLogger() );

        command.addArgument( JazzConstants.ARG_STATUS_WIDE_PRINT_OUT );
        return command;
    }
}
