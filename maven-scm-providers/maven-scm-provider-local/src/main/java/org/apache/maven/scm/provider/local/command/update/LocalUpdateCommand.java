package org.apache.maven.scm.provider.local.command.update;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.LocalScmProvider;
import org.apache.maven.scm.provider.local.command.LocalCommand;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LocalUpdateCommand
    extends AbstractUpdateCommand
    implements LocalCommand
{
    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, File workingDirectory, String tag )
        throws ScmException
    {
        LocalScmProviderRepository repository = (LocalScmProviderRepository) repo;

        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "The local scm doesn't support tags." );
        }

        File root = new File( repository.getRoot() );

        String module = repository.getModule();

        File source = new File( root, module );

        File baseDestination = new File( workingDirectory, module );

        if ( !workingDirectory.exists() )
        {
            throw new ScmException( "The working directory doesn't exist (" + workingDirectory.getAbsolutePath() + ")." );
        }

        if ( !root.exists() )
        {
            throw new ScmException( "The base directory doesn't exist (" + root.getAbsolutePath() + ")." );
        }

        if ( !source.exists() )
        {
            throw new ScmException( "The module directory doesn't exist (" + source.getAbsolutePath() + ")." );
        }

    	if ( !baseDestination.exists() && !baseDestination.isDirectory() )
    	{
    	    throw new ScmException( "The destination directory isn't a directory or doesn't exist (" + baseDestination.getAbsolutePath() + ")." );
    	}

    	List updatedFiles;

    	try
        {
        	System.err.println( "Updating '" + baseDestination.getAbsolutePath() + "' from '" + source.getAbsolutePath() + "'." );

        	List fileList = FileUtils.getFiles( source.getAbsoluteFile(), "**", null );

        	updatedFiles = update( source, baseDestination, fileList, LocalScmProvider.fixModuleName( repository.getModule() ) );
        }
        catch( IOException ex )
        {
            throw new ScmException( "Error while checking out the files.", ex );
        }

        return new LocalUpdateScmResult( updatedFiles );
    }

    private List update( File source, File baseDestination, List files, String module )
    	throws ScmException, IOException
    {
        String sourcePath = source.getAbsolutePath();

        List updatedFiles = new ArrayList();

        for ( Iterator i = files.iterator(); i.hasNext(); )
        {
            File repositoryFile = (File) i.next();

            File repositoryDirectory = repositoryFile.getParentFile();

            // TODO: Add more excludes here
            if ( repositoryDirectory != null && repositoryDirectory.getName().equals( "CVS" ) )
            {
                continue;
            }

            String dest = repositoryFile.getAbsolutePath().substring( sourcePath.length() + 1 );

            File destinationFile = new File( baseDestination, dest );

            String repositoryFileContents = FileUtils.fileRead( repositoryFile );

            if ( destinationFile.exists() )
            {
                String destionationFileContents = FileUtils.fileRead( destinationFile );

                if ( repositoryFileContents.equals( destionationFileContents ) )
                {
                    continue;
                }
            }

            File destinationDirectory = destinationFile.getParentFile();

            if ( !destinationDirectory.exists() && !destinationDirectory.mkdirs() )
            {
                throw new ScmException( "Could not create destination directory '" + destinationDirectory.getAbsolutePath() + "'." );
            }

            ScmFileStatus status;

            if ( destinationFile.exists() )
            {
                status = ScmFileStatus.UPDATED;
            }
            else
            {
                status = ScmFileStatus.ADDED;
            }

            FileUtils.copyFileToDirectory( repositoryFile, destinationDirectory );

            int chop = baseDestination.getAbsolutePath().length();

            String fileName = "/" + module + "/" + destinationFile.getAbsolutePath().substring( chop + 1 );

            updatedFiles.add( new ScmFile( fileName, status ) );
        }

        return updatedFiles;
    }
}
