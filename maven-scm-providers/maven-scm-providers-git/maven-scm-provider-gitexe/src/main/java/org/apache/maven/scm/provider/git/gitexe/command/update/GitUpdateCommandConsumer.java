package org.apache.maven.scm.provider.git.gitexe.command.update;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;
import org.codehaus.plexus.util.StringUtils;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 10 août 2008
 * @version $Id$
 */
public class GitUpdateCommandConsumer
    extends AbstractConsumer
{
    
    private boolean updatingFound;
    
    private boolean summaryFound;

    private List scmFiles = new ArrayList();

    public GitUpdateCommandConsumer( ScmLogger logger, File workingDirectory )
    {
        super( logger );
    }

    public void consumeLine( String line )
    {
        getLogger().debug( "GitUpdateCommandConsumer consumeLine : " + line );
        if ( line == null || StringUtils.isEmpty( line ) )
        {
            return;
        }
        if ( !updatingFound )
        {
            if ( line.startsWith( "Updating" ) )
            {
                updatingFound = true;
                return;
            }
        }
        // skip summary line
        //1 files changed, 1 insertions(+), 1 deletions(-)←[m
        if ( line.indexOf( "files changed" ) >= 0 )
        {
            summaryFound = true;

        }
        if ( updatingFound && !summaryFound )
        {
            // test format : pom.xml←[m |    1 ←[32m+←[m
            int index = line.indexOf( "←[" );
           
            if ( index >= 0 )
            {
                String fileName = StringUtils.trim( line.substring( 0, index ) );
                scmFiles.add( new ScmFile( fileName, ScmFileStatus.UPDATED ) );
            }
            else
            {
                // test other format : pom.xml |    3 +--
                index = line.indexOf( "|" );
                if ( index >= 0 )
                {
                    String fileName = StringUtils.trim( line.substring( 0, index ) );
                    scmFiles.add( new ScmFile( fileName, ScmFileStatus.UPDATED ) );
                }
            }
        }

        // here we have status/name of added/remove and we update if create or remove
        //3 files changed, 1 insertions(+), 3 deletions(-)←[m
        // delete mode 100644 README
        //create mode 100644 test.txt
    }

    public List getUpdatedFiles()
    {
        getLogger().debug( " updatedFiles size " + scmFiles.size() );
        return scmFiles;
    }    
}
