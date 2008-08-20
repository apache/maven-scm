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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.edit.AbstractEditCommand;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.synergy.command.SynergyCommand;
import org.apache.maven.scm.provider.synergy.repository.SynergyScmProviderRepository;
import org.apache.maven.scm.provider.synergy.util.SynergyUtil;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 * @version $Id$
 */
public class SynergyEditCommand
    extends AbstractEditCommand
    implements SynergyCommand
{
    /** {@inheritDoc} */
    protected ScmResult executeEditCommand( ScmProviderRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        getLogger().debug( "executing edit command..." );

        SynergyScmProviderRepository repo = (SynergyScmProviderRepository) repository;
        getLogger().debug( "basedir: " + fileSet.getBasedir() );

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
            getLogger().info( "Task " + taskNum + " was created to perform checkout." );
            for ( Iterator i = fileSet.getFileList().iterator(); i.hasNext(); )
            {
                File f = (File) i.next();
                File dest = f;
                File source = new File( sourcePath, SynergyUtil.removePrefix( fileSet.getBasedir(), f ) );
                List list = new LinkedList();
                list.add( source );
                SynergyUtil.checkoutFiles( getLogger(), list, ccmAddr );
                if ( !source.equals( dest ) )
                {
                    getLogger().debug( "Copy file [" + source + "] to expected folder [" + dest + "]." );
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

        return new EditScmResult( "", fileSet.getFileList() );
    }

}
