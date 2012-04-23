package org.apache.maven.scm.provider.jazz.command.add;

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
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.consumer.AbstractRepositoryConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Consume the output of the scm command for the "add" operation.
 *
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzAddConsumer
    extends AbstractRepositoryConsumer
{
    // A flag to indicate that we have seen the "Changes:" line in the output.
    // After that, we have the files themselves.
    private boolean haveSeenChanges = false;

    protected String fCurrentDir = "";

    // The list of files that we have checked in.
    private List<ScmFile> fCheckedInFiles = new ArrayList<ScmFile>();

    /**
     * Construct the JazzAddCommand consumer.
     *
     * @param repository The repository we are working with.
     * @param logger     The logger to use.
     */
    public JazzAddConsumer( ScmProviderRepository repository, ScmLogger logger )
    {
        super( repository, logger );
    }

    /**
     * Process one line of output from the execution of the "scm xxxx" command.
     *
     * @param line The line of output from the external command that has been pumped to us.
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        super.consumeLine( line );
        // The Jazz SCM "checkin" command does not output a list of each file that was checked in.
        // An example output is shown below, perhaps in the future we may need to
        // consume the "Workspace", "Component", Stream or "Change sets"

        /*
            Committing...
            Workspace: (1004) "Release Repository Workspace" <-> (1005) "Maven Release Plugin Stream"
              Component: (1006) "Release Component"
                Outgoing:
                  Change sets:
                    (1008) --@ <No comment>
                    
        Or:
        
            Committing...
            Workspace: (1903) "MavenSCMTestWorkspace_1332908068770" <-> (1903) "MavenSCMTestWorkspace_1332908068770"
              Component: (1768) "MavenSCMTestComponent"
                Outgoing:
                  Change sets:
                    (1907)  *--@  "Commit message"
                      Changes:
                        --a-- \src\main\java\Me.java
                        --a-- \src\main\java\Me1.java
                        --a-- \src\main\java\Me2.java        
        */

        if ( haveSeenChanges )
        {
            // We have already seen the "Changes:" line, so we must now be processing files.
            String trimmed = line.trim();
            int spacePos = trimmed.indexOf( " " );
            // NOTE: The second + 1 is to remove the leading slash
            String path = trimmed.substring( spacePos + 1 + 1 );
            fCheckedInFiles.add( new ScmFile( path, ScmFileStatus.CHECKED_OUT ) );
        }
        else
        {
            if ( "Changes:".equals( line.trim() ) )
            {
                haveSeenChanges = true;
            }
        }
    }

    protected ScmFile getScmFile( String filename )
    {
        return new ScmFile( new File( fCurrentDir, filename ).getAbsolutePath(), ScmFileStatus.CHECKED_OUT );
    }

    public List<ScmFile> getFiles()
    {
        return fCheckedInFiles;
    }
}