package org.apache.maven.scm.provider.synergy.command.update;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.synergy.command.SynergyCommand;
import org.apache.maven.scm.provider.synergy.command.changelog.SynergyChangeLogCommand;
import org.apache.maven.scm.provider.synergy.repository.SynergyScmProviderRepository;
import org.apache.maven.scm.provider.synergy.util.SynergyUtil;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 *
 */
public class SynergyUpdateCommand
    extends AbstractUpdateCommand
    implements SynergyCommand
{
    /** {@inheritDoc} */
    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                    ScmVersion version )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing update command..." );
        }
        SynergyScmProviderRepository repo = (SynergyScmProviderRepository) repository;

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "basedir: " + fileSet.getBasedir() );
        }

        String ccmAddr = SynergyUtil.start( getLogger(), repo.getUser(), repo.getPassword(), null );

        File waPath;
        try
        {
            String projectSpec =
                SynergyUtil.getWorkingProject( getLogger(), repo.getProjectSpec(), repo.getUser(), ccmAddr );
            SynergyUtil.reconfigureProperties( getLogger(), projectSpec, ccmAddr );
            SynergyUtil.reconfigure( getLogger(), projectSpec, ccmAddr );
            // We need to get WA path
            waPath = SynergyUtil.getWorkArea( getLogger(), projectSpec, ccmAddr );
        }
        finally
        {
            SynergyUtil.stop( getLogger(), ccmAddr );
        }

        File source = new File( waPath, repo.getProjectName() );

        // Move file from work area to expected dir if not the same
        List<ScmFile> modifications = new ArrayList<ScmFile>();
        if ( !source.equals( fileSet.getBasedir() ) )
        {
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "We will copy modified files from Synergy Work Area [" + source
                                  + "] to expected folder [" + fileSet.getBasedir() + "]" );
            }
            try
            {
                copyDirectoryStructure( source, fileSet.getBasedir(), modifications );
            }
            catch ( IOException e1 )
            {
                throw new ScmException( "Unable to copy directory structure", e1 );
            }
        }

        return new UpdateScmResult( "ccm reconcile -uwa ...", modifications );
    }

    protected ChangeLogCommand getChangeLogCommand()
    {
        SynergyChangeLogCommand changeLogCmd = new SynergyChangeLogCommand();

        changeLogCmd.setLogger( getLogger() );

        return changeLogCmd;
    }

    /**
     * Copies a entire directory structure and collect modifications.
     * <p>
     * Note:
     * <ul>
     * <li>It will include empty directories.
     * <li>The <code>sourceDirectory</code> must exists.
     * </ul>
     *
     * @param sourceDirectory
     * @param destinationDirectory
     * @throws IOException
     */
    public static void copyDirectoryStructure( File sourceDirectory, File destinationDirectory,
                                               List<ScmFile> modifications )
        throws IOException
    {
        if ( !sourceDirectory.exists() )
        {
            throw new IOException( "Source directory doesn't exists (" + sourceDirectory.getAbsolutePath() + ")." );
        }

        File[] files = sourceDirectory.listFiles();

        String sourcePath = sourceDirectory.getAbsolutePath();

        for ( File file : files )
        {
            String dest = file.getAbsolutePath();

            dest = dest.substring( sourcePath.length() + 1 );

            File destination = new File( destinationDirectory, dest );

            if ( file.isFile() )
            {

                if ( file.lastModified() != destination.lastModified() )
                {

                    destination = destination.getParentFile();

                    FileUtils.copyFileToDirectory( file, destination );

                    modifications.add( new ScmFile( file.getAbsolutePath(), ScmFileStatus.UPDATED ) );

                }
            }
            else if ( file.isDirectory() )
            {
                if ( !destination.exists() && !destination.mkdirs() )
                {
                    throw new IOException(
                        "Could not create destination directory '" + destination.getAbsolutePath() + "'." );
                }

                copyDirectoryStructure( file, destination, modifications );
            }
            else
            {
                throw new IOException( "Unknown file type: " + file.getAbsolutePath() );
            }
        }
    }

}
