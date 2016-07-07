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
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmFileSet;
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

    private ScmFileSet scmFileSet;

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
     * Assuming that you have to discover the repositoryRoot, this is how you can get the
     * <code>relativeRepositoryPath</code>
     * <pre>
     * URI.create( repositoryRoot ).relativize( fileSet.getBasedir().toURI() )
     * </pre>
     * 
     * @param logger the logger
     * @param workingDirectory the working directory
     * @param relativeRepositoryPath the working directory relative to the repository root
     * @since 1.9
     * @see GitStatusCommand#createRevparseShowPrefix(org.apache.maven.scm.ScmFileSet)
     */
    public GitStatusConsumer( ScmLogger logger, File workingDirectory, URI relativeRepositoryPath )
    {
        this( logger, workingDirectory );
        this.relativeRepositoryPath = relativeRepositoryPath;
    }

    /**
     * Assuming that you have to discover the repositoryRoot, this is how you can get the
     * <code>relativeRepositoryPath</code>
     * <pre>
     * URI.create( repositoryRoot ).relativize( fileSet.getBasedir().toURI() )
     * </pre>
     *
     * @param logger the logger
     * @param workingDirectory the working directory
     * @param scmFileSet fileset with includes and excludes
     * @since 1.9
     * @see GitStatusCommand#createRevparseShowToplevelCommand(org.apache.maven.scm.ScmFileSet)
     */
    public GitStatusConsumer( ScmLogger logger, File workingDirectory, ScmFileSet scmFileSet )
    {
        this( logger, workingDirectory );
        this.scmFileSet = scmFileSet;
    }

    /**
     * Assuming that you have to discover the repositoryRoot, this is how you can get the
     * <code>relativeRepositoryPath</code>
     * <pre>
     * URI.create( repositoryRoot ).relativize( fileSet.getBasedir().toURI() )
     * </pre>
     *
     * @param logger the logger
     * @param workingDirectory the working directory
     * @param relativeRepositoryPath the working directory relative to the repository root
     * @param scmFileSet fileset with includes and excludes
     * @since 1.9
     * @see GitStatusCommand#createRevparseShowToplevelCommand(org.apache.maven.scm.ScmFileSet)
     */
    public GitStatusConsumer( ScmLogger logger, File workingDirectory, URI relativeRepositoryPath, ScmFileSet scmFileSet )
    {
        this( logger, workingDirectory, scmFileSet );
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
            files.add( resolvePath( matcher.group( 1 ), relativeRepositoryPath ) );
        }
        else if ( ( matcher = MODIFIED_PATTERN.matcher( line ) ).find() )
        {
            status = ScmFileStatus.MODIFIED;
            files.add( resolvePath( matcher.group( 1 ), relativeRepositoryPath ) );
        }
        else if ( ( matcher = DELETED_PATTERN.matcher( line ) ).find() )
        {
            status = ScmFileStatus.DELETED;
            files.add( resolvePath( matcher.group( 1 ), relativeRepositoryPath ) );
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
            logger.warn( "Ignoring unrecognized line: " + line );
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
                        logger.debug( "file '" + oldFilePath + "' is a file" );
                        return;
                    }
                    else
                    {
                        logger.debug( "file '" + oldFilePath + "' not a file" );
                    }
                    if ( !isFile( newFilePath ) )
                    {
                        logger.debug( "file '" + newFilePath + "' not a file" );
                        return;
                    }
                    else
                    {
                        logger.debug( "file '" + newFilePath + "' is a file" );
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
                if ( this.scmFileSet != null && !isFileNameInFileList( this.scmFileSet.getFileList(), file ) )
                {
                    // skip adding this file
                }
                else
                {
                    changedFiles.add( new ScmFile( file, status ) );
                }
            }
        }
    }

    private boolean isFileNameInFileList( List<File> fileList, String fileName )
    {
        if ( relativeRepositoryPath == null )
        {
          return fileList.contains( new File( fileName ) );
        }
        else
        {
            for ( File f : fileList )
            {
                File file = new File( relativeRepositoryPath.getPath(), fileName );
                if ( file.getPath().endsWith( f.getName() ) )
                {
                    return true;
                }
            }
            return fileList.isEmpty();
        }

    }

    private boolean isFile( String file )
    {
        File targetFile = new File( workingDirectory, file );
        return targetFile.isFile();
    }

    protected static String resolvePath( String fileEntry, URI path )
    {
        /* Quotes may be included (from the git status line) when an fileEntry includes spaces */
        String cleanedEntry = stripQuotes( fileEntry );
        if ( path != null )
        {
            return resolveURI( cleanedEntry, path ).getPath();
        }
        else
        {
            return cleanedEntry;
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
        // When using URI.create, spaces need to be escaped but not the slashes, so we can't use
        // URLEncoder.encode( String, String )
        // new File( String ).toURI() results in an absolute URI while path is relative, so that can't be used either.
        return path.relativize( uriFromPath( stripQuotes ( fileEntry ) ) );
    }

    /**
     * Create an URI whose getPath() returns the given path and getScheme() returns null. The path may contain spaces,
     * colons, and other special characters.
     * 
     * @param path the path.
     * @return the new URI
     */
    public static URI uriFromPath( String path )
    {
        try
        {
            if ( path != null && path.indexOf( ':' ) != -1 )
            {
                // prefixing the path so the part preceding the colon does not become the scheme
                String tmp = new URI( null, null, "/x" + path, null ).toString().substring( 2 );
                // the colon is not escaped by default
                return new URI( tmp.replace( ":", "%3A" ) );
            }
            else
            {
                return new URI( null, null, path, null );
            }
        }
        catch ( URISyntaxException x )
        {
            throw new IllegalArgumentException( x.getMessage(), x );
        }
    }

    public List<ScmFile> getChangedFiles()
    {
        return changedFiles;
    }

    /**
     * @param str the (potentially quoted) string, must not be {@code null}
     * @return the string with a pair of double quotes removed (if they existed)
     */
    private static String stripQuotes( String str )
    {
        int strLen = str.length();
        return ( strLen > 0 && str.startsWith( "\"" ) && str.endsWith( "\"" ) )
                        ? unescape( str.substring( 1, strLen - 1 ) )
                        : str;
    }
    
    /**
     * Dequote a quoted string generated by git status --porcelain.
     * The leading and trailing quotes have already been removed. 
     * @param fileEntry
     * @return
     */
    private static String unescape( String fileEntry )
    {
        // If there are no escaped characters, just return the input argument
        int pos = fileEntry.indexOf( '\\' );
        if ( pos == -1 )
        {
            return fileEntry;
        }
        
        // We have escaped characters
        byte[] inba = fileEntry.getBytes();
        int inSub = 0;      // Input subscript into fileEntry
        byte[] outba = new byte[fileEntry.length()];
        int outSub = 0;     // Output subscript into outba
        
        while ( true )
        {
            System.arraycopy( inba,  inSub,  outba, outSub, pos - inSub );
            outSub += pos - inSub;
            inSub = pos + 1;
            switch ( (char) inba[inSub++] )
            {
                case '"':
                    outba[outSub++] = '"';
                    break;
                    
                case 'a':
                    outba[outSub++] = 7;        // Bell
                    break;
                    
                case 'b':
                    outba[outSub++] = '\b';
                    break;
                    
                case 't':
                    outba[outSub++] = '\t';
                    break;
                    
                case 'n':
                    outba[outSub++] = '\n';
                    break;
                    
                case 'v':
                    outba[outSub++] = 11;       // Vertical tab
                    break;
                    
                case 'f':
                    outba[outSub++] = '\f';
                    break;
                    
                case 'r':
                    outba[outSub++] = '\f';
                    break;
                    
                case '\\':
                    outba[outSub++] = '\\';
                    break;
                    
                case '0':
                case '1':
                case '2':
                case '3':
                    // This assumes that the octal escape here is valid.
                    byte b = (byte) ( ( inba[inSub - 1] - '0' ) << 6 );
                    b |= (byte) ( ( inba[inSub++] - '0' ) << 3 );
                    b |= (byte) ( inba[inSub++] - '0' );
                    outba[outSub++] = b;
                    break;
                    
                default:
                    //This is an invalid escape in a string.  Just copy it.
                    outba[outSub++] = '\\';
                    inSub--;
                    break;
            }
            pos = fileEntry.indexOf( '\\', inSub );
            if ( pos == -1 )        // No more backslashes; we're done
            {
                System.arraycopy( inba, inSub, outba, outSub, inba.length - inSub );
                outSub += inba.length - inSub;
                break;
            }
        }
        try
        {
            // explicit say UTF-8, otherwise it'll fail at least on Windows cmdline
            return new String( outba, 0, outSub, "UTF-8" );
        }
        catch ( UnsupportedEncodingException e )
        {
          throw new RuntimeException( e );    
        }
    }
}
