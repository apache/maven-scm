package org.apache.maven.scm.provider.synergy.command.edit;

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
import java.util.LinkedList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.edit.AbstractEditCommand;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.synergy.command.SynergyCommand;
import org.apache.maven.scm.provider.synergy.repository.SynergyScmProviderRepository;
import org.apache.maven.scm.provider.synergy.util.SynergyUtil;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 *
 */
public class SynergyEditCommand
    extends AbstractEditCommand
    implements SynergyCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeEditCommand( ScmProviderRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing edit command..." );
        }

        SynergyScmProviderRepository repo = (SynergyScmProviderRepository) repository;

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( fileSet.toString() );
        }

        String ccmAddr = SynergyUtil.start( getLogger(), repo.getUser(), repo.getPassword(), null );

        try
        {
            String projectSpec =
                SynergyUtil.getWorkingProject( getLogger(), repo.getProjectSpec(), repo.getUser(), ccmAddr );
            File waPath = SynergyUtil.getWorkArea( getLogger(), projectSpec, ccmAddr );
            File sourcePath = new File( waPath, repo.getProjectName() );
            if ( projectSpec == null )
            {
                throw new ScmException( "You should checkout project first" );
            }
            int taskNum = SynergyUtil.createTask( getLogger(), "Maven SCM Synergy provider: edit command for project "
                + repo.getProjectSpec(), repo.getProjectRelease(), true, ccmAddr );
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "Task " + taskNum + " was created to perform checkout." );
            }
            for ( File dest : fileSet.getFileList() )
            {
                File source = new File( sourcePath, SynergyUtil.removePrefix( fileSet.getBasedir(), dest ) );
                List<File> list = new LinkedList<File>();
                list.add( source );
                SynergyUtil.checkoutFiles( getLogger(), list, ccmAddr );
                if ( !source.equals( dest ) )
                {
                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Copy file [" + source + "] to expected directory [" + dest + "]." );
                    }
                    try
                    {
                        FileUtils.copyFile( source, dest );
                    }
                    catch ( IOException e )
                    {
                        throw new ScmException( "Unable to copy file from Work Area", e );
                    }
                }
            }
        }
        finally
        {
            SynergyUtil.stop( getLogger(), ccmAddr );
        }
        List<ScmFile> scmFiles = new ArrayList<ScmFile>( fileSet.getFileList().size() );
        for ( File dest : fileSet.getFileList() )
        {
            scmFiles.add( new ScmFile( dest.getPath(), ScmFileStatus.EDITED ) );
        }
        return new EditScmResult( "", scmFiles );
    }

}
