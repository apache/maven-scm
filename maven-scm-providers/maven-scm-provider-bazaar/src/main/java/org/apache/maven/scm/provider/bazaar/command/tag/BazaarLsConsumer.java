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

package org.apache.maven.scm.provider.bazaar.command.tag;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

/**
 * Parse output from "bzr ls".
 * @author <a href="mailto:johan.walles@gmail.com">Johan Walles</a>
 * @author Olivier Lamy
 *
 */
class BazaarLsConsumer extends BazaarConsumer {
    /**
     * The root directory of this bazaar repository.
     */
    private File repositoryRoot;
    
    /**
     * A list of the files found by ls.
     */
    private List<ScmFile> files;
    
    /**
     * Create a new "bzr ls" consumer.
     * @param repositoryRoot The root directory of this bazaar repository.
     * @param wantedStatus The status we'll report for the files listed.
     */
    public BazaarLsConsumer(ScmLogger logger,
            File repositoryRoot,
            ScmFileStatus wantedStatus) 
    {
        super( logger );
        files = new LinkedList<ScmFile>();
    }
    
    public void doConsume( ScmFileStatus status, String trimmedLine ) {
        if ( trimmedLine.endsWith( File.separator ) ) {
            // Don't report directories
            return;
        }
        
        String path = new File( repositoryRoot, trimmedLine ).toString();
        files.add( new ScmFile( path, ScmFileStatus.TAGGED ) );
    }
    
    /**
     * Answer what files were listed by bzr ls.
     * @return A list of files listed by bzr ls.
     */
    public List<ScmFile> getListedFiles() {
        return files;
    }
}
