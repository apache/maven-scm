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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitStatusConsumer
    implements StreamConsumer
{

    /**
     * The pattern used to match added file lines
     */
    private static final Pattern addedRegexp = Pattern.compile( "^A[ M]* (.*)$" );

    /**
     * The pattern used to match modified file lines
     */
    private static final Pattern modifiedRegexp = Pattern.compile( "^ *M[ M]* (.*)$" );

    /**
     * The pattern used to match deleted file lines
     */
    private Pattern deletedRegexp = Pattern.compile( "^ *D * (.*)$" );

    /**
     * The pattern used to match renamed file lines
     */
    private Pattern renamedRegexp = Pattern.compile( "R (.*) -> (.*)$" );

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
        
        Matcher matcher;
        if ( ( matcher = addedRegexp.matcher( line ) ).find() )
        {
            status = ScmFileStatus.ADDED;
            files.add( matcher.group( 1 ) );
        }
        else if ( ( matcher = modifiedRegexp.matcher( line ) ).find() )
        {
            status = ScmFileStatus.MODIFIED;
            files.add( matcher.group( 1 ) );
        }
        else if ( ( matcher = deletedRegexp.matcher( line ) ) .find() )
        {
            status = ScmFileStatus.DELETED;
            files.add( matcher.group( 1 ) );
        }
        else if ( ( matcher = renamedRegexp.matcher( line ) ).find() )
        {
            status = ScmFileStatus.RENAMED;
            files.add( StringUtils.trim( matcher.group( 1 ) ) );
            files.add( StringUtils.trim( matcher.group( 2 ) ) );
            logger.debug( "RENAMED status for line '" + line + "' files added '" + matcher.group( 1 ) + "' '"
                              + matcher.group( 2 ) );
        }
        else
        {
        	logger.warn( "Ignoring unrecognized line: " +  line );
        	return;
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
                        logger.debug(
                            "file '" + new File( workingDirectory, oldFilePath ).getAbsolutePath() + "' is a file" );
                        return;
                    }
                    else
                    {
                        logger.debug(
                            "file '" + new File( workingDirectory, oldFilePath ).getAbsolutePath() + "' not a file" );
                    }
                    if ( !new File( workingDirectory, newFilePath ).isFile() )
                    {
                        logger.debug(
                            "file '" + new File( workingDirectory, newFilePath ).getAbsolutePath() + "' not a file" );
                        return;
                    }
                    else
                    {
                        logger.debug(
                            "file '" + new File( workingDirectory, newFilePath ).getAbsolutePath() + "' is a file" );
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

            for ( String file : files )
            {
                changedFiles.add( new ScmFile( file, status ) );
            }
        }
    }

    public List<ScmFile> getChangedFiles()
    {
        return changedFiles;
    }
}
