package org.apache.maven.scm.provider.local.command.checkout;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.LocalCommand;
import org.apache.maven.scm.provider.local.metadata.LocalScmMetadataUtils;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class LocalCheckOutCommand
    extends AbstractCheckOutCommand
    implements LocalCommand
{
    /** {@inheritDoc} */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                       ScmVersion version, boolean recursive, boolean shallow )
        throws ScmException
    {
        LocalScmProviderRepository repository = (LocalScmProviderRepository) repo;

        if ( version != null )
        {
            throw new ScmException( "The local scm doesn't support tags." );
        }

        File root = new File( repository.getRoot() );

        String module = repository.getModule();

        File source = new File( root, module );

        File baseDestination = fileSet.getBasedir();

        if ( !root.exists() )
        {
            throw new ScmException( "The base directory doesn't exist (" + root.getAbsolutePath() + ")." );
        }

        if ( !source.exists() )
        {
            throw new ScmException( "The module directory doesn't exist (" + source.getAbsolutePath() + ")." );
        }

        List<ScmFile> checkedOutFiles;

        try
        {
            if ( baseDestination.exists() )
            {
                FileUtils.deleteDirectory( baseDestination );
            }

            if ( !baseDestination.mkdirs() )
            {
                throw new ScmException(
                    "Could not create destination directory '" + baseDestination.getAbsolutePath() + "'." );
            }

            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info(
                                  "Checking out '" + source.getAbsolutePath() + "' to '"
                                      + baseDestination.getAbsolutePath() + "'." );
            }

            List<File> fileList;

            if ( fileSet.getFileList().isEmpty() )
            {
                @SuppressWarnings( "unchecked" )
                List<File> files = FileUtils.getFiles( source.getAbsoluteFile(), "**", null ); 
                fileList = files;
            }
            else
            {
                fileList = fileSet.getFileList();
            }

            checkedOutFiles = checkOut( source, baseDestination, fileList, repository.getModule() );

            // write metadata file
            LocalScmMetadataUtils metadataUtils = new LocalScmMetadataUtils( getLogger() );
            metadataUtils.writeMetadata( baseDestination, metadataUtils.buildMetadata( source ) );
        }
        catch ( IOException ex )
        {
            throw new ScmException( "Error while checking out the files.", ex );
        }

        return new LocalCheckOutScmResult( null, checkedOutFiles );
    }

    private List<ScmFile> checkOut( File source, File baseDestination, List<File> files, String module )
        throws ScmException, IOException
    {
        String sourcePath = source.getAbsolutePath();

        List<ScmFile> checkedOutFiles = new ArrayList<ScmFile>();

        for ( File file : files )
        {
            String dest = file.getAbsolutePath();

            dest = dest.substring( sourcePath.length() + 1 );

            File destination = new File( baseDestination, dest );

            destination = destination.getParentFile();

            if ( !destination.exists() && !destination.mkdirs() )
            {
                throw new ScmException(
                    "Could not create destination directory '" + destination.getAbsolutePath() + "'." );
            }

            FileUtils.copyFileToDirectory( file, destination );

            File parent = file.getParentFile();

            // TODO: Add more excludes here
            if ( parent != null && parent.getName().equals( "CVS" ) )
            {
                continue;
            }

            String fileName = "/" + module + "/" + dest;

            checkedOutFiles.add( new ScmFile( fileName, ScmFileStatus.CHECKED_OUT ) );
        }

        return checkedOutFiles;
    }


}
