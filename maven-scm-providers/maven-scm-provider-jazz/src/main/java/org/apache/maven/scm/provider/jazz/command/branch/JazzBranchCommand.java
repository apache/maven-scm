package org.apache.maven.scm.provider.jazz.command.branch;

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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.branch.AbstractBranchCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;

// STATUS: NOT DONE 
//
// The current scm command does not have the functionality to create streams.
//
// Once it does, we will need to figure out how users intend this goal to work in RTC.
//
// There is no direct equivalent of the "branch" command in RTC.
// A branch in RTC is roughly equivalent to a Stream or even a Repository Workspace, depending on how
// the flow targets have been defined, thus the branch command could be used to create a new Stream or
// Repository Workspace. The RTC command "create" can be used to create new Repository Workspaces.
// Note however the creation of a new stream from the command line is not supported. 
//
// See the following links for additional information on the RTC "create" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_create.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_create.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_create.html
//

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzBranchCommand
    extends AbstractBranchCommand
{
    /**
     * {@inheritDoc}
     */
    protected ScmResult executeBranchCommand( ScmProviderRepository repo, ScmFileSet fileSet, String branch,
                                              String message )
        throws ScmException
    {
        throw new ScmException( "This provider does not support the branch command." );
    }
}
