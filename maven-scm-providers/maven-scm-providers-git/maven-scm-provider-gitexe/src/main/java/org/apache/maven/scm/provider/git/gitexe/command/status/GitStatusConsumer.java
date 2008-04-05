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
 */
public class GitStatusConsumer
    implements StreamConsumer
{
    /**
     * The pattern used to match added file lines
     */
    private static final String ADDED_PATTERN = "^#\\s*new file:\\s*(.*)";

    /**
     * The pattern used to match modified file lines
     */
    private static final String MODIFIED_PATTERN = "^#\\s*modified:\\s*(.*)";
    
    /**
     * The pattern used to match deleted file lines
     */
    private static final String DELETED_PATTERN = "^#\\s*deleted:\\s*(.*)";
    
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
    
    private ScmLogger logger;

    private File workingDirectory;

    private List changedFiles = new ArrayList();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public GitStatusConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;
        this.workingDirectory = workingDirectory;
        
        try
        {
            addedRegexp    = new RE( ADDED_PATTERN    );
            modifiedRegexp = new RE( MODIFIED_PATTERN );
            deletedRegexp  = new RE( DELETED_PATTERN  );
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
        logger.debug( line );
        if ( StringUtils.isEmpty( line ) )
        {
            return;
        }
        
        ScmFileStatus status = null;
        
        String file = null;

        if ( addedRegexp.match( line ) ) 
        {
            status = ScmFileStatus.ADDED;
            file = addedRegexp.getParen( 1 );
        } 
        else if ( modifiedRegexp.match( line ) ) 
        {
            status = ScmFileStatus.MODIFIED;
            file = modifiedRegexp.getParen( 1 );
        }
        else if ( deletedRegexp.match( line ) ) 
        {
            status = ScmFileStatus.DELETED;
            file = deletedRegexp.getParen( 1 );
        }
        
        // If the file isn't a file; don't add it.
        if ( file != null )
        {
            if ( workingDirectory != null && !new File( workingDirectory, file ).isFile() )
            {
                return;
            }
            
            changedFiles.add( new ScmFile( file, status ) );
        }

        
    }

    public List getChangedFiles()
    {
        return changedFiles;
    }
}
