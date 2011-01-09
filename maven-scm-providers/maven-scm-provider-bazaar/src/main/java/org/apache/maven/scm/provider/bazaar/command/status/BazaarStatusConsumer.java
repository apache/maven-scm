package org.apache.maven.scm.provider.bazaar.command.status;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a>
 * @version $Id$
 */
class BazaarStatusConsumer
    extends BazaarConsumer
{
    private final List<ScmFile> repositoryStatus = new ArrayList<ScmFile>();

    private final File workingDir;

    /**
     * State currently consuming (one of the identifieres or null)
     */
    private ScmFileStatus currentState = null;

    BazaarStatusConsumer( ScmLogger logger, File workingDir )
    {
        super( logger );
        this.workingDir = workingDir;
    }

    /** {@inheritDoc} */
    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        if ( status != null )
        {
            currentState = status;
            return;
        }

        if ( currentState == null )
        {
            return;
        }

        //Only include real files (not directories)
        File tmpFile = new File( workingDir, trimmedLine );
        if ( !tmpFile.exists() )
        {
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "Not a file: " + tmpFile + ". Ignoring" );
            }
        }
        else if ( tmpFile.isDirectory() )
        {
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "New directory added: " + tmpFile );
            }
        }
        else
        {
            ScmFile scmFile = new ScmFile( trimmedLine, currentState );
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( scmFile.toString() );
            }
            repositoryStatus.add( scmFile );
        }
    }

    List<ScmFile> getStatus()
    {
        return repositoryStatus;
    }
}
