package org.apache.maven.scm.provider.synergy.command.add;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
 */

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.synergy.command.SynergyCommand;
import org.apache.maven.scm.provider.synergy.repository.SynergyScmProviderRepository;
import org.apache.maven.scm.provider.synergy.util.SynergyUtil;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyAddCommand extends AbstractAddCommand implements SynergyCommand
{
    protected ScmResult executeAddCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message,
            boolean binary ) throws ScmException
    {
        getLogger().debug( "executing add command..." );

        SynergyScmProviderRepository repo = ( SynergyScmProviderRepository ) repository;
        getLogger().debug( "basedir: " + fileSet.getBasedir() );

        if ( message == null || message.equals( "" ) )
        {
            message = "Maven SCM Synergy provider: adding file(s) to project " + repo.getProjectSpec();
        }

        String CCM_ADDR = SynergyUtil.start( getLogger(), repo.getUser(), repo.getPassword(), null );

        try
        {
            int taskNum = SynergyUtil.createTask( getLogger(), message, repo.getProjectRelease(), true, CCM_ADDR );
            String project_spec = SynergyUtil.getWorkingProject( getLogger(), repo.getProjectSpec(), repo.getUser(),
                    CCM_ADDR );
            if ( project_spec == null )
            {
                throw new ScmException( "You should checkout project first" );
            }
            File WAPath = SynergyUtil.getWorkArea( getLogger(), project_spec, CCM_ADDR );
            File destPath = new File( WAPath, repo.getProjectName() );
            for ( Iterator i = fileSet.getFileList().iterator(); i.hasNext(); )
            {
                File f = ( File ) i.next();
                File source = f;
                File dest = new File( destPath, SynergyUtil.removePrefix( fileSet.getBasedir(), f ) );
                if ( !source.equals( dest ) )
                {
                    getLogger().debug( "Copy file [" + source + "] to Synergy Work Area [" + dest + "]." );
                    try
                    {
                        FileUtils.copyFile( source, dest );
                    }
                    catch ( IOException e )
                    {
                        throw new ScmException( "Unable to copy file in Work Area", e );
                    }
                }
                SynergyUtil.create( getLogger(), dest, message, CCM_ADDR );
            }
            SynergyUtil.checkinTask( getLogger(), taskNum, message, CCM_ADDR );

        }
        finally
        {
            SynergyUtil.stop( getLogger(), CCM_ADDR );
        }

        return new AddScmResult( "", fileSet.getFileList() );
    }

    

}
