package org.apache.maven.scm.provider.jazz.command.list;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.consumer.AbstractRepositoryConsumer;

/**
 * Consume the output of the scm command for the "list" operation.
 * 
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzListConsumer
    extends AbstractRepositoryConsumer
{
    private List<ScmFile> files = new ArrayList<ScmFile>();

    /**
     * Construct the JazzListCommand consumer.
     * @param repository The repository we are working with.
     * @param logger The logger to use.
     */
    public JazzListConsumer( ScmProviderRepository repository, ScmLogger logger )
    {
        super( repository, logger );
    }

    /**
     * Process one line of output from the execution of the "scm list" command.
     * @param line The line of output from the external command that has been pumped to us.
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        super.consumeLine( line );
        files.add( new ScmFile( line, ScmFileStatus.CHECKED_IN ) );
    }

    public List<ScmFile> getFiles()
    {
        return files;
    }
}
