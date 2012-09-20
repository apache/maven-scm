package org.apache.maven.scm.provider.synergy.command.unedit;

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
import org.apache.maven.scm.command.unedit.AbstractUnEditCommand;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.synergy.command.SynergyCommand;
import org.apache.maven.scm.provider.synergy.repository.SynergyScmProviderRepository;
import org.apache.maven.scm.provider.synergy.util.SynergyUtil;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 *
 */
public class SynergyUnEditCommand
    extends AbstractUnEditCommand
    implements SynergyCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeUnEditCommand( ScmProviderRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing unedit command..." );
        }

        SynergyScmProviderRepository repo = (SynergyScmProviderRepository) repository;

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "basedir: " + fileSet.getBasedir() );
        }

        String ccmAddr = SynergyUtil.start( getLogger(), repo.getUser(), repo.getPassword(), null );

        try
        {
            String projectSpec =
                SynergyUtil.getWorkingProject( getLogger(), repo.getProjectSpec(), repo.getUser(), ccmAddr );
            if ( projectSpec == null )
            {
                throw new ScmException( "You should checkout a working project first" );
            }
            File waPath = SynergyUtil.getWorkArea( getLogger(), projectSpec, ccmAddr );
            File destPath = new File( waPath, repo.getProjectName() );
            for ( File f : fileSet.getFileList() )
            {
                File source = new File( fileSet.getBasedir(), f.getPath() );
                File dest = new File( destPath, f.getPath() );
                SynergyUtil.delete( getLogger(), dest, ccmAddr, true );
                if ( !source.equals( dest ) )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Copy file [" + dest + "] to [" + source + "]." );
                    }
                    try
                    {
                        FileUtils.copyFile( dest, source );
                    }
                    catch ( IOException e )
                    {
                        throw new ScmException( "Unable to restore file in output folder", e );
                    }
                }
            }
        }
        finally
        {
            SynergyUtil.stop( getLogger(), ccmAddr );
        }
        List<ScmFile> files = new ArrayList<ScmFile>();
        for ( File f : fileSet.getFileList() )
        {
            files.add( new ScmFile( f.getPath(), ScmFileStatus.UNKNOWN ) );
        }
        return new UnEditScmResult( "", files );
    }

}
