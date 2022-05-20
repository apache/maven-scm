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
import org.apache.maven.scm.util.AbstractConsumer;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Bertrand Paquet
 */
public class GitRemoteInfoConsumer
        extends AbstractConsumer
{

    /**
     * The pattern used to match branches
     */
    private static final Pattern BRANCH_PATTERN = Pattern.compile( "^(.*)\\s+refs/heads/(.*)" );

    /**
     * The pattern used to match tags
     */
    private static final Pattern TAGS_PATTERN = Pattern.compile( "^(.*)\\s+refs/tags/(.*)" );

    private final RemoteInfoScmResult remoteInfoScmResult;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public GitRemoteInfoConsumer( ScmLogger logger, String commandLine )
    {
        super( logger );
        this.remoteInfoScmResult =
            new RemoteInfoScmResult( commandLine, new HashMap<String, String>(), new HashMap<String, String>() );
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
        
        Matcher matcher = BRANCH_PATTERN.matcher( line );
        if ( matcher.matches() )
        {
            remoteInfoScmResult.getBranches().put( matcher.group( 2 ), matcher.group( 1 ) );
        }
        
        matcher = TAGS_PATTERN.matcher( line );
        if ( matcher.matches() )
        {
            remoteInfoScmResult.getTags().put( matcher.group( 2 ), matcher.group( 1 ) );
        }

    }

    public RemoteInfoScmResult getRemoteInfoScmResult()
    {
        return remoteInfoScmResult;
    }

}