package org.apache.maven.scm.provider.git.gitexe.command.status;

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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @version $Id$
 */
public class GitStatusConsumer
    implements StreamConsumer
{
    /**
     * The pattern used to match added file lines
     */
    private static final String ADDED_PATTERN = "^A[ M]* (.*)$";

    /**
     * The pattern used to match modified file lines
     */
    private static final String MODIFIED_PATTERN = "^ *M[ M]* (.*)$";

    /**
     * The pattern used to match deleted file lines
     */
    private static final String DELETED_PATTERN = "^ *D * (.*)$";

    /**
     * The pattern used to match renamed file lines
     */
    private static final String RENAMED_PATTERN = "R (.*) -> (.*)$";

    /**
     * @see #ADDED_PATTERN
     */
    private RE addedRegexp;

    /**
     * @see #MODIFIED_PATTERN
     */
    private RE modifiedRegexp;

    /**
     * @see #DELETED_PATTERN
     */
    private RE deletedRegexp;

    /**
     * @see #RENAMED_PATTERN
     */
    private RE renamedRegexp;

    private ScmLogger logger;

    private File workingDirectory;

    private List<ScmFile> changedFiles = new ArrayList<ScmFile>();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public GitStatusConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;
        this.workingDirectory = workingDirectory;

        try
        {
            addedRegexp = new RE( ADDED_PATTERN );
            modifiedRegexp = new RE( MODIFIED_PATTERN );
            deletedRegexp = new RE( DELETED_PATTERN );
            renamedRegexp = new RE( RENAMED_PATTERN );
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

        List<String> files = new ArrayList<String>();

        if ( addedRegexp.match( line ) )
        {
            status = ScmFileStatus.ADDED;
            files.add(addedRegexp.getParen(1));
        }
        else if ( modifiedRegexp.match( line ) )
        {
            status = ScmFileStatus.MODIFIED;
            files.add( modifiedRegexp.getParen( 1 ) );
        }
        else if ( deletedRegexp.match( line ) )
        {
            status = ScmFileStatus.DELETED;
            files.add( deletedRegexp.getParen( 1 ) );
        }
        else if ( renamedRegexp.match( line ) )
        {
            status = ScmFileStatus.RENAMED;
            files.add( renamedRegexp.getParen( 1 ) );
            files.add( renamedRegexp.getParen( 2 ) );
        }

        // If the file isn't a file; don't add it.
        if ( !files.isEmpty() && status != null )
        {
            if ( workingDirectory != null )
            {
                if ( status == ScmFileStatus.RENAMED )
                {
                    String oldFilePath = files.get( 0 );
                    String newFilePath = files.get( 1 );
                    if ( new File( workingDirectory, oldFilePath ).isFile() )
                    {
                        return;
                    }
                    if ( !new File( workingDirectory, newFilePath ).isFile() )
                    {
                        return;
                    }
                }
                else if ( status == ScmFileStatus.DELETED )
                {
                    if ( new File( workingDirectory, files.get( 0 ) ).isFile() )
                    {
                        return;
                    }
                }
                else
                {
                    if ( !new File( workingDirectory, files.get( 0 ) ).isFile() )
                    {
                        return;
                    }
                }
            }

            for(String file : files){
                changedFiles.add( new ScmFile( file, status ) );
            }
        }
    }

    public List<ScmFile> getChangedFiles()
    {
        return changedFiles;
    }
}
