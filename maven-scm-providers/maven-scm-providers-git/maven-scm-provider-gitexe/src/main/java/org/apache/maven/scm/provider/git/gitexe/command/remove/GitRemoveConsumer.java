package org.apache.maven.scm.provider.git.gitexe.command.remove;

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
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitRemoveConsumer implements StreamConsumer
{
    /**
     * The pattern used to match deleted file lines
     */
    private static final String REMOVED_PATTERN = "^rm\\s'(.*)'";

    private ScmLogger logger;

    private List removedFiles = new ArrayList();

    /**
     * @see #REMOVED_PATTERN
     */
    private RE removedRegexp;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public GitRemoveConsumer( ScmLogger logger )
    {
        this.logger = logger;
        try
        {
            removedRegexp = new RE( REMOVED_PATTERN );
        }
        catch ( RESyntaxException ex )
        {
            throw new RuntimeException(
                "INTERNAL ERROR: Could not create regexp to parse git log file. This shouldn't happen. Something is probably wrong with the oro installation.",
                ex );
        }        
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    public void consumeLine( String line )
    {
        if ( line.length() <= 2 )
        {
            return;
        }

        if ( removedRegexp.match( line ) ) 
        {
        	String file = removedRegexp.getParen( 1 );
            removedFiles.add( new ScmFile( file, ScmFileStatus.DELETED ) );
        }
        else
        {
            logger.info( "could not parse line: " + line );

            return;
        }
    }

    public List getRemovedFiles()
    {
        return removedFiles;
    }

}
