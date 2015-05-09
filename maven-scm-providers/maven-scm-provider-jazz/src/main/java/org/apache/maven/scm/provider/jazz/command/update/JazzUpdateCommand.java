package org.apache.maven.scm.provider.jazz.command.update;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.changelog.JazzChangeLogCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;

//
// The Maven SCM Plugin "update" goal is equivalent to the RTC "accept" command.
//
// NOTE: What is not clear from the docs, is that the accept command will also
// update the sandbox with the changes that have been accepted into the repository.
// However, I have checked with Rational Support, and this is indeed the expected
// behaviour. This may come from the fact that you can accept changes into a
// repository workspace, without having a sandbox loaded; though this makes no
// sense to us from a maven usage context (as we only work in a sandbox).
//
// See the following links for additional information on the RTC "create snapshot" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_accept.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_accept.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_accept.html
//

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzUpdateCommand
    extends AbstractUpdateCommand
{
    /**
     * {@inheritDoc}
     */
    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing update command..." );
        }

        JazzUpdateConsumer updateConsumer = new JazzUpdateConsumer( repo, getLogger() );
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );

        JazzScmCommand updateCmd = createAcceptCommand( repo, fileSet );
        int status = updateCmd.execute( updateConsumer, errConsumer );

        if ( status != 0 )
        {
            return new UpdateScmResult( updateCmd.getCommandString(),
                                        "Error code for Jazz SCM update command - " + status, errConsumer.getOutput(), 
                                        false );
        }

        if ( getLogger().isDebugEnabled() )
        {
            if ( !updateConsumer.getUpdatedFiles().isEmpty() )
            {
                getLogger().debug( "Iterating over \"Update\" results" );
                for ( ScmFile file : updateConsumer.getUpdatedFiles() )
                {
                    getLogger().debug( file.getPath() + " : " + file.getStatus() );
                }
            }
            else
            {
                getLogger().debug( "There are no updated files" );
            }
        }

        // Now, just (re)load the workspace into the sand box.
        // We can use the checkout directory for this.
        return new UpdateScmResult( updateCmd.getCommandString(), updateConsumer.getUpdatedFiles() );
    }

    public JazzScmCommand createAcceptCommand( ScmProviderRepository repo, ScmFileSet fileSet )
    {
        JazzScmCommand command = new JazzScmCommand( JazzConstants.CMD_ACCEPT, repo, fileSet, getLogger() );

        command.addArgument( JazzConstants.ARG_FLOW_COMPONENTS );

        return command;
    }

    /**
     * {@inheritDoc}
     */
    protected ChangeLogCommand getChangeLogCommand()
    {
        JazzChangeLogCommand command = new JazzChangeLogCommand();

        command.setLogger( getLogger() );

        return command;
    }

}
