package org.apache.maven.scm.provider.jazz.command.checkout;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

//
// The Maven SCM plugin "checkout" goal is equivalent to the RTC "load" command.
//
// See the following links for additional information on the RTC "load" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_load.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_load.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_load.html
//

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzCheckOutCommand
    extends AbstractCheckOutCommand
{
    /**
     * {@inheritDoc}
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                       ScmVersion scmVersion, boolean recursive, boolean shallow )
        throws ScmException
    {
        // TODO - Figure out how this recursive boolean impacts Jazz SCM "checkout" (load).

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing checkout command..." );
        }

        JazzScmProviderRepository jazzRepo = (JazzScmProviderRepository) repo;

        JazzScmCommand checkoutCmd = createJazzLoadCommand( jazzRepo, fileSet, scmVersion );
        JazzCheckOutConsumer checkoutConsumer = new JazzCheckOutConsumer( repo, getLogger() );
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );

        int status = checkoutCmd.execute( checkoutConsumer, errConsumer );
        if ( status != 0 )
        {
            return new CheckOutScmResult( checkoutCmd.getCommandString(),
                                          "Error code for Jazz SCM checkout (load) command - " + status,
                                          errConsumer.getOutput(), false );
        }

        return new CheckOutScmResult( checkoutCmd.getCommandString(), checkoutConsumer.getCheckedOutFiles() );
    }

    public JazzScmCommand createJazzLoadCommand( JazzScmProviderRepository repo, ScmFileSet fileSet,
                                                 ScmVersion scmVersion )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_LOAD, JazzConstants.ARG_FORCE, repo, fileSet, getLogger() );

        if ( fileSet != null )
        {
            command.addArgument( JazzConstants.ARG_LOCAL_WORKSPACE_PATH );
            command.addArgument( fileSet.getBasedir().getAbsolutePath() );
        }

        // This works in tandem with the Tag Command.
        // Currently, RTC can not check out directly from a snapshot.
        // So, as a work around, the Tag Command creates a workspace name of the same name as the snapshot.
        // The functionality here (in using the ScmTag or ScmBranch) assumes that the workspace has been
        // created as a part of the Tag Command. 

        String workspace = repo.getRepositoryWorkspace();
        if ( scmVersion != null && StringUtils.isNotEmpty( scmVersion.getName() ) )
        {
            // Just in case we ever do something different for Tags (snapshots) and Branches (streams)
            if ( scmVersion instanceof ScmTag )
            {
                workspace = scmVersion.getName();
            }
            else if ( scmVersion instanceof ScmBranch )
            {
                workspace = scmVersion.getName();
            }
        }

        command.addArgument( workspace );

        return command;
    }
}
