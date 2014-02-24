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
import java.net.URI;
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
    private static final Pattern ADDED_PATTERN = Pattern.compile( "^A[ M]* (.*)$" );

    /**
     * The pattern used to match modified file lines
     */
    private static final Pattern MODIFIED_PATTERN = Pattern.compile( "^ *M[ M]* (.*)$" );

    /**
     * The pattern used to match deleted file lines
     */
    private static final Pattern DELETED_PATTERN = Pattern.compile( "^ *D * (.*)$" );

    /**
     * The pattern used to match renamed file lines
     */
    private static final Pattern RENAMED_PATTERN = Pattern.compile( "^R  (.*) -> (.*)$" );

    private ScmLogger logger;

    private File workingDirectory;

    /**
     * Entries are relative to working directory, not to the repositoryroot
     */
    private List<ScmFile> changedFiles = new ArrayList<ScmFile>();

    private URI relativeRepositoryPath;
    
    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Consumer when workingDirectory and repositoryRootDirectory are the same
     * 
     * @param logger the logger
     * @param workingDirectory the working directory
     */
    public GitStatusConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;
        this.workingDirectory = workingDirectory;
    }

    /**
     * Assuming that you have to discover the repositoryRoot, this is how you can get the <code>relativeRepositoryPath</code>
     * <pre>
     * URI.create( repositoryRoot ).relativize( fileSet.getBasedir().toURI() )
     * </pre>
     * 
     * @param logger the logger
     * @param workingDirectory the working directory
     * @param relativeRepositoryPath the working directory relative to the repository root
     * @since 1.9
     * @see GitStatusCommand#createRevparseShowToplevelCommand(org.apache.maven.scm.ScmFileSet)
     */
    public GitStatusConsumer( ScmLogger logger, File workingDirectory, URI relativeRepositoryPath )
    {
        this( logger, workingDirectory );
        this.relativeRepositoryPath = relativeRepositoryPath;
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
        if ( ( matcher = ADDED_PATTERN.matcher( line ) ).find() )
        {
            status = ScmFileStatus.ADDED;
            files.add(resolvePath(matcher.group(1), relativeRepositoryPath));
        }
        else if ( ( matcher = MODIFIED_PATTERN.matcher( line ) ).find() )
        {
            status = ScmFileStatus.MODIFIED;
            files.add(resolvePath(matcher.group(1), relativeRepositoryPath));
        }
        else if ( ( matcher = DELETED_PATTERN.matcher( line ) ) .find() )
        {
            status = ScmFileStatus.DELETED;
            files.add(resolvePath(matcher.group(1), relativeRepositoryPath));
        }
        else if ( ( matcher = RENAMED_PATTERN.matcher( line ) ).find() )
        {
            status = ScmFileStatus.RENAMED;
            files.add( resolvePath( matcher.group( 1 ), relativeRepositoryPath ) );
            files.add( resolvePath( matcher.group( 2 ), relativeRepositoryPath ) );
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
                    if ( isFile( oldFilePath ) )
                    {
                        logger.debug(
                            "file '" + oldFilePath + "' is a file" );
                        return;
                    }
                    else
                    {
                        logger.debug(
                            "file '" + oldFilePath + "' not a file" );
                    }
                    if ( !isFile( newFilePath ) )
                    {
                        logger.debug(
                            "file '" + newFilePath + "' not a file" );
                        return;
                    }
                    else
                    {
                        logger.debug(
                            "file '" + newFilePath + "' is a file" );
                    }
                }
                else if ( status == ScmFileStatus.DELETED )
                {
                    if ( isFile( files.get( 0 ) ) )
                    {
                        return;
                    }
                }
                else
                {
                    if ( !isFile( files.get( 0 ) ) )
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

    private boolean isFile( String file )
    {
        return new File( workingDirectory, file ).isFile();
    }

    protected static String resolvePath( String fileEntry, URI path )
    {
        if ( path != null )
        {
            return resolveURI( fileEntry, path ).getPath();
        }
        else
        {
            return fileEntry;
        }
    }

    /**
     * 
     * @param fileEntry the fileEntry, must not be {@code null}
     * @param path the path, must not be {@code null}
     * @return
     */
    public static URI resolveURI( String fileEntry, URI path )
    {
        // When using URI.create, spaces need to be escaped but not the slashes, so we can't use URLEncoder.encode( String, String )
        // new File( String ).toURI() results in an absolute URI while path is relative, so that can't be used either.
        String str = fileEntry.replace( " ", "%20" );
        return path.relativize( URI.create( str ) );
    }


    public List<ScmFile> getChangedFiles()
    {
        return changedFiles;
    }
}
