package org.apache.maven.scm.provider.svn.svnexe.command.status;

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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class SvnStatusConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private File workingDirectory;

    private List<ScmFile> changedFiles = new ArrayList<ScmFile>();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public SvnStatusConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;

        this.workingDirectory = workingDirectory;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( line );
        }
        if ( StringUtils.isEmpty( line.trim() ) )
        {
            return;
        }

        if ( line.length() <= 7 )
        {
            if ( logger.isWarnEnabled() )
            {
                logger.warn( "Unexpected input, the line must be at least seven characters long. Line: '"
                             + line + "'." );
            }

            return;
        }

        String statusString = line.substring( 0, 1 );

        String file = line.substring( 7 ).trim();

        ScmFileStatus status;

        //  The first six columns in the output are each one character wide:
        //    First column: Says if item was added, deleted, or otherwise changed
        //      ' ' no modifications
        //      'A' Added
        //      'C' Conflicted
        //      'D' Deleted
        //      'I' Ignored
        //      'M' Modified
        //      'R' Replaced
        //      'X' item is unversioned, but is used by an externals definition
        //      '?' item is not under version control
        //      '!' item is missing (removed by non-svn command) or incomplete
        //      '~' versioned item obstructed by some item of a different kind
        //    Second column: Modifications of a file's or directory's properties
        //      ' ' no modifications
        //      'C' Conflicted
        //      'M' Modified
        //    Third column: Whether the working copy directory is locked
        //      ' ' not locked
        //      'L' locked
        //    Fourth column: Scheduled commit will contain addition-with-history
        //      ' ' no history scheduled with commit
        //      '+' history scheduled with commit
        //    Fifth column: Whether the item is switched relative to its parent
        //      ' ' normal
        //      'S' switched
        //    Sixth column: Repository lock token
        //      (without -u)
        //      ' ' no lock token
        //      'K' lock token present
        //      (with -u)
        //      ' ' not locked in repository, no lock token
        //      'K' locked in repository, lock toKen present
        //      'O' locked in repository, lock token in some Other working copy
        //      'T' locked in repository, lock token present but sTolen
        //      'B' not locked in repository, lock token present but Broken
        //
        //  The out-of-date information appears in the eighth column (with -u):
        //      '*' a newer revision exists on the server
        //      ' ' the working copy is up to date
        if ( statusString.equals( "A" ) )
        {
            status = ScmFileStatus.ADDED;
        }
        else if ( statusString.equals( "M" ) || statusString.equals( "R" ) || statusString.equals( "~" ) )
        {
            status = ScmFileStatus.MODIFIED;
        }
        else if ( statusString.equals( "D" ) )
        {
            status = ScmFileStatus.DELETED;
        }
        else if ( statusString.equals( "?" ) )
        {
            status = ScmFileStatus.UNKNOWN;
        }
        else if ( statusString.equals( "!" ) )
        {
            status = ScmFileStatus.MISSING;
        }
        else if ( statusString.equals( "C" ) )
        {
            status = ScmFileStatus.CONFLICT;
        }
        else if ( statusString.equals( "L" ) )
        {
            status = ScmFileStatus.LOCKED;
        }
        else if ( statusString.equals( "X" ) )
        {
            //skip svn:external entries
            return;
        }
        else if ( statusString.equals( "I" ) )
        {
            //skip svn:external entries
            return;
        }
        else
        {
            //Parse the second column
            statusString = line.substring( 1, 1 );

            if ( statusString.equals( "M" ) )
            {
                status = ScmFileStatus.MODIFIED;
            }
            else if ( statusString.equals( "C" ) )
            {
                status = ScmFileStatus.CONFLICT;
            }
            else
            {
                //The line isn't a status line, ie something like 'Performing status on external item at...'
                //or a status defined in next columns
                return;
            }
        }

        // If the file isn't a file; don't add it.
        if ( !status.equals( ScmFileStatus.DELETED ) && !new File( workingDirectory, file ).isFile() )
        {
            return;
        }

        changedFiles.add( new ScmFile( file, status ) );
    }

    public List<ScmFile> getChangedFiles()
    {
        return changedFiles;
    }
}
