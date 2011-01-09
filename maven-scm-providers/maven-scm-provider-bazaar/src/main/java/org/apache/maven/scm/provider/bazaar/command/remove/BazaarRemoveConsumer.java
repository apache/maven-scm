package org.apache.maven.scm.provider.bazaar.command.remove;

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
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjorn Eikli Smorgrav</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class BazaarRemoveConsumer
    extends BazaarConsumer
{
    private final File workingDir;

    private final List<ScmFile> removedFiles = new ArrayList<ScmFile>();

    public BazaarRemoveConsumer( ScmLogger logger, File workingDir )
    {
        super( logger );
        this.workingDir = workingDir;
    }

    /** {@inheritDoc} */
    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        if ( status != null && status == ScmFileStatus.DELETED )
        {
            //Only include real files (not directories)
            File tmpFile = new File( workingDir, trimmedLine );
            if ( !tmpFile.exists() )
            {
                if ( getLogger().isWarnEnabled() )
                {
                    getLogger().warn( "Not a file: " + tmpFile + ". Ignored" );
                }
            }
            else
            {
                ScmFile scmFile = new ScmFile( trimmedLine, ScmFileStatus.DELETED );
                if ( getLogger().isInfoEnabled() )
                {
                    getLogger().info( scmFile.toString() );
                }
                removedFiles.add( scmFile );
            }
        }
    }

    public List<ScmFile> getRemovedFiles()
    {
        return removedFiles;
    }
}