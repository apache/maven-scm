package org.apache.maven.scm.provider.synergy.command.checkout;

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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.synergy.command.SynergyCommand;
import org.apache.maven.scm.provider.synergy.repository.SynergyScmProviderRepository;
import org.apache.maven.scm.provider.synergy.util.SynergyUtil;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 *
 */
public class SynergyCheckOutCommand
    extends AbstractCheckOutCommand
    implements SynergyCommand
{

    /** {@inheritDoc} */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                       ScmVersion version, boolean recursive, boolean shallow )
        throws ScmException
    {
        if ( fileSet.getFileList().size() != 0 )
        {
            throw new ScmException( "This provider doesn't support checking out subsets of a project" );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing checkout command..." );
        }

        SynergyScmProviderRepository repo = (SynergyScmProviderRepository) repository;

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( fileSet.toString() );
        }

        String ccmAddr = SynergyUtil.start( getLogger(), repo.getUser(), repo.getPassword(), null );

        File waPath;
        try
        {
            String projectSpec =
                SynergyUtil.getWorkingProject( getLogger(), repo.getProjectSpec(), repo.getUser(), ccmAddr );
            if ( projectSpec != null )
            {
                if ( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "A working project already exists [" + projectSpec + "]." );
                }
                SynergyUtil.synchronize( getLogger(), projectSpec, ccmAddr );
            }
            else
            {
                SynergyUtil.checkoutProject( getLogger(), null, repo.getProjectSpec(), version,
                                             repo.getProjectPurpose(), repo.getProjectRelease(), ccmAddr );
                projectSpec =
                    SynergyUtil.getWorkingProject( getLogger(), repo.getProjectSpec(), repo.getUser(), ccmAddr );
                if ( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "A new working project [" + projectSpec + "] was created." );
                }
            }
            SynergyUtil.reconfigure( getLogger(), projectSpec, ccmAddr );
            waPath = SynergyUtil.getWorkArea( getLogger(), projectSpec, ccmAddr );

        }
        finally
        {
            SynergyUtil.stop( getLogger(), ccmAddr );
        }

        File source = new File( waPath, repo.getProjectName() );

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info(
                              "We will now copy files from Synergy Work Area [" + source
                                  + "] to expected folder [" + fileSet.getBasedir() + "]" );
        }

        // Move files to the expected folder
        try
        {
            FileUtils.copyDirectoryStructure( source, fileSet.getBasedir() );
        }
        catch ( IOException e1 )
        {
            throw new ScmException( "Unable to copy directory structure", e1 );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "We will list content of checkout directory." );
        }

        // We need to list files in the directory
        List<ScmFile> files = new ArrayList<ScmFile>();
        try
        {
            @SuppressWarnings( "unchecked" )
            List<File> realFiles = FileUtils.getFiles( fileSet.getBasedir(), null, "_ccmwaid.inf" );
            for ( File f : realFiles )
            {
                files.add( new ScmFile( f.getPath(), ScmFileStatus.CHECKED_OUT ) );
            }
        }
        catch ( IOException e )
        {
            throw new ScmException( "Unable to list files in checkout directory", e );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "checkout command end successfully ..." );
        }

        return new CheckOutScmResult( files, new ScmResult( "multiple commandline", "OK", "OK", true ) );
    }

}
