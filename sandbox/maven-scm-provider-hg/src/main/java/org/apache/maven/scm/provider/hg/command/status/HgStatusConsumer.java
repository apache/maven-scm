package org.apache.maven.scm.provider.hg.command.status;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.command.HgConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
class HgStatusConsumer
    extends HgConsumer
{
    private final List repositoryStatus = new ArrayList();

    private final File workingDir;


    HgStatusConsumer( ScmLogger logger, File workingDir )
    {
        super( logger );
        this.workingDir = workingDir;
    }

    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
         //Only include real files (not directories)
        File tmpFile = new File( workingDir, trimmedLine );
        if ( !tmpFile.exists() )
        {
            getLogger().info( "Not a file: " + tmpFile + ". Ignoring" );
        }
        else if ( tmpFile.isDirectory() )
        {
            getLogger().info( "New directory added: " + tmpFile );
        }
        else
        {
            ScmFile scmFile = new ScmFile( trimmedLine, status );
            getLogger().info( scmFile.toString() );
            repositoryStatus.add( scmFile );
        }
    }

    List getStatus()
    {
        return repositoryStatus;
    }
}
