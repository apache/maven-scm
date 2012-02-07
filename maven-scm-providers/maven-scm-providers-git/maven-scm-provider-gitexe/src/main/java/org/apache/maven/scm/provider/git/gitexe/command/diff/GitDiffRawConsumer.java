package org.apache.maven.scm.provider.git.gitexe.command.diff;

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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id$
 */
public class GitDiffRawConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private List<ScmFile> changedFiles = new ArrayList<ScmFile>();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public GitDiffRawConsumer(ScmLogger logger)
    {
        this.logger = logger;
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
        if ( StringUtils.isEmpty( line ) )
        {
            return;
        }

        ScmFileStatus status = null;

        String[] parts = line.split( "\\s", 6 );
        if ( parts.length != 6 )
        {
            logger.warn( "Skipping line because it doesn't contain the right status parameters: " + line );
            return;
        }
        
        String modus = parts[4];
        String file = parts[5];

        if ( "A".equals( modus ) )
        {
            status = ScmFileStatus.ADDED;
        }
        else if ( "M".equals( modus ) )
        {
            // attention! 'M' is 'updated', and _not_ ScmFileStatus.MODIFIED (which is for 'modified locally')
            status = ScmFileStatus.UPDATED;
        }
        else if ( "D".equals( modus ) )
        {
            status = ScmFileStatus.DELETED;
        }
        else
        {
            logger.warn( "unknown status detected in line: " + line );
            return;
        }

        changedFiles.add( new ScmFile( file, status ) );
    }

    public List<ScmFile> getChangedFiles()
    {
        return changedFiles;
    }
}
