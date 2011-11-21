package org.apache.maven.scm.provider.git.gitexe.command.remoteinfo;

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

import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.HashMap;

/**
 * @author Bertrand Paquet
 */
public class GitRemoteInfoConsumer
    implements StreamConsumer
{

    /**
     * The pattern used to match branches
     */
    private static final String BRANCH_PATTERN = "^(.*)\\s+refs/heads/(.*)";

    /**
     * The pattern used to match tags
     */
    private static final String TAGS_PATTERN = "^(.*)\\s+refs/tags/(.*)";

    private ScmLogger logger;

    private RemoteInfoScmResult remoteInfoScmResult;

    private RE branchRegexp;

    private RE tagRegexp;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public GitRemoteInfoConsumer( ScmLogger logger, String commandLine )
    {
        this.logger = logger;
        this.remoteInfoScmResult =
            new RemoteInfoScmResult( commandLine, new HashMap<String, String>(), new HashMap<String, String>() );

        try
        {
            this.branchRegexp = new RE( BRANCH_PATTERN );
            this.tagRegexp = new RE( TAGS_PATTERN );
        }
        catch ( RESyntaxException ex )
        {
            throw new RuntimeException(
                "INTERNAL ERROR: Could not create regexp to parse git ls-remote file. This shouldn't happen. Something is probably wrong with the oro installation.",
                ex );
        }
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public void consumeLine( String line )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( line );
        }
        if ( branchRegexp.match( line ) )
        {
            remoteInfoScmResult.getBranches().put( branchRegexp.getParen( 2 ), branchRegexp.getParen( 1 ) );
        }
        if ( tagRegexp.match( line ) )
        {
            remoteInfoScmResult.getTags().put( tagRegexp.getParen( 2 ), tagRegexp.getParen( 1 ) );
        }

    }

    public RemoteInfoScmResult getRemoteInfoScmResult()
    {
        return remoteInfoScmResult;
    }

}