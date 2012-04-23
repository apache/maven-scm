package org.apache.maven.scm.provider.jazz.command.blame;

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
import org.apache.maven.scm.command.blame.AbstractBlameCommand;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.JazzConstants;
import org.apache.maven.scm.provider.jazz.command.JazzScmCommand;
import org.apache.maven.scm.provider.jazz.command.consumer.ErrorConsumer;

// The Maven SCM plugin "blame" goal is equivalent to the RTC "annotate" command.
//
// See the following links for additional information on the RTC "annotate" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_annotate.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_annotate.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_annotate.html
//
// The RTC "history" command also provides similar information that might be needed for the "blame" goal.
// See the following links for additional information on the RTC "history" command:
// RTC 2.0.0.2:
// http://publib.boulder.ibm.com/infocenter/rtc/v2r0m0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_history.html
// RTC 3.0:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0/topic/com.ibm.team.scm.doc/topics/r_scm_cli_history.html
// RTC 3.0.1:
// http://publib.boulder.ibm.com/infocenter/clmhelp/v3r0m1/topic/com.ibm.team.scm.doc/topics/r_scm_cli_history.html
//

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzBlameCommand
    extends AbstractBlameCommand
{
    /**
     * {@inheritDoc}
     */
    public BlameScmResult executeBlameCommand( ScmProviderRepository repo, ScmFileSet fileSet, String filename )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Executing blame command..." );
        }

        JazzScmCommand blameCmd = createBlameCommand( repo, fileSet, filename );

        JazzBlameConsumer blameConsumer = new JazzBlameConsumer(repo, getLogger());
        ErrorConsumer errConsumer = new ErrorConsumer( getLogger() );

        int status = blameCmd.execute( blameConsumer, errConsumer );
        if ( status != 0 || errConsumer.hasBeenFed() )
        {
            return new BlameScmResult( blameCmd.getCommandString(),
                                       "Error code for Jazz SCM blame command - " + status,
                                       errConsumer.getOutput(), false );
        }

        return new BlameScmResult( blameCmd.getCommandString(), blameConsumer.getLines() );
    }

    public JazzScmCommand createBlameCommand( ScmProviderRepository repo, ScmFileSet fileSet, String filename )
    {
        JazzScmCommand command =
            new JazzScmCommand( JazzConstants.CMD_ANNOTATE, null, repo, false, fileSet, getLogger() );
        command.addArgument( filename );

        return command;
    }

}
