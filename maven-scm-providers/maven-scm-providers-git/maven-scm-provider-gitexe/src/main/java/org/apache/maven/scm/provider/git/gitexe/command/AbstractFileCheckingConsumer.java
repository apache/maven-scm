package org.apache.maven.scm.provider.git.gitexe.command;

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
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 */
public abstract class AbstractFileCheckingConsumer
    implements StreamConsumer
{
    protected ScmLogger logger;

    protected File workingDirectory;

    private List files = new ArrayList();

    protected int revision;

    private boolean filtered;

    public AbstractFileCheckingConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;
        this.workingDirectory = workingDirectory;
    }

    public final void consumeLine( String line )
    {
        if ( line.length() <= 3 )
        {
            return;
        }

        logger.debug( line );

        parseLine( line );
    }

    protected abstract void parseLine( String line );

    protected List getFiles()
    {
        if ( !filtered )
        {
            for ( Iterator it = files.iterator(); it.hasNext(); )
            {
                if ( !new File( workingDirectory, ( (ScmFile) it.next() ).getPath() ).isFile() )
                {
                    it.remove();
                }
            }

            filtered = true;
        }

        return files;
    }

    protected final int parseInt( String revisionString )
    {
        try
        {
            return Integer.parseInt( revisionString );
        }
        catch ( NumberFormatException ex )
        {
            return 0;
        }
    }

    protected void addFile( ScmFile file )
    {
        files.add( file );
    }

    public final int getRevision()
    {
        return revision;
    }

}
