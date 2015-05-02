package org.apache.maven.scm.provider.jazz.command.list;

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
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;

//
// See the following links for additional information on the RTC "list" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_list.html
// RTC 3.0
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_list.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_list.html
//

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzListCommand
    extends AbstractListCommand
{
    /**
     * {@inheritDoc}
     */
    protected ListScmResult executeListCommand( ScmProviderRepository repo, ScmFileSet fileSet, boolean recursive,
                                                ScmVersion version )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing list command..." );
        }

        JazzScmProviderRepository jazzRepo = (JazzScmProviderRepository) repo;

        JazzListConsumer listConsumer = new JazzListConsumer( repo, getLogger() );
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );

        JazzScmCommand listCmd = createListCommand( jazzRepo, fileSet, recursive, version );
        int status = listCmd.execute( listConsumer, errConsumer );
        if ( status != 0 || errConsumer.hasBeenFed() )
        {
            return new ListScmResult( listCmd.getCommandString(), "Error code for Jazz SCM list command - " + status,
                                      errConsumer.getOutput(), false );
        }

        return new ListScmResult( listCmd.getCommandString(), listConsumer.getFiles() );
    }

    public JazzScmCommand createListCommand( JazzScmProviderRepository repo, ScmFileSet fileSet, boolean recursive,
                                             ScmVersion version )
    {
        // recursive is implicit in the command, so it is ignored. NOTE: V4 appears to have changed this.
        // version is meaningless, so it is ignored.
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_LIST, JazzConstants.CMD_SUB_REMOTEFILES, repo, fileSet, getLogger() );
        if ( recursive )
        {
            command.addArgument( JazzConstants.ARG_DEPTH );
            command.addArgument( JazzConstants.ARG_DEPTH_INFINTE );
        }
        command.addArgument( repo.getRepositoryWorkspace() );
        command.addArgument( repo.getComponent() );
        return command;
    }

}
