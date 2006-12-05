package org.apache.maven.scm.provider.synergy.command.changelog;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.AbstractChangeLogCommand;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.synergy.command.SynergyCommand;
import org.apache.maven.scm.provider.synergy.repository.SynergyScmProviderRepository;
import org.apache.maven.scm.provider.synergy.util.SynergyTask;
import org.apache.maven.scm.provider.synergy.util.SynergyUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyChangeLogCommand extends AbstractChangeLogCommand implements SynergyCommand
{

    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repository, ScmFileSet fileSet,
            Date startDate, Date endDate, String branch, String datePattern ) throws ScmException
    {
        getLogger().debug( "executing changelog command..." );
        SynergyScmProviderRepository repo = ( SynergyScmProviderRepository ) repository;
        getLogger().debug( "basedir: " + fileSet.getBasedir() );

        String CCM_ADDR = SynergyUtil.start( getLogger(), repo.getUser(), repo.getPassword(), null );

        List csList = new ArrayList();

        try
        {
            String project_spec = SynergyUtil.getWorkingProject( getLogger(), repo.getProjectSpec(), repo.getUser(),
                    CCM_ADDR );
            if ( project_spec == null )
            {
                throw new ScmException( "You should checkout project first" );
            }
            List tasks = SynergyUtil.getCompletedTasks( getLogger(), project_spec, startDate, endDate, CCM_ADDR );
            for ( Iterator i = tasks.iterator(); i.hasNext(); )
            {
                ChangeSet cs = new ChangeSet();
                SynergyTask t = ( ( SynergyTask ) i.next() );
                cs.setAuthor( t.getUsername() );
                cs.setComment( "Task " + t.getNumber() + ": " + t.getComment() );
                cs.setDate( t.getModifiedTime() );
                cs.setFiles( SynergyUtil.getModifiedObjects( getLogger(), t.getNumber(), CCM_ADDR ) );
                csList.add( cs );
            }
        }
        finally
        {
            SynergyUtil.stop( getLogger(), CCM_ADDR );
        }

        return new ChangeLogScmResult( "ccm query ...", new ChangeLogSet( csList, startDate, endDate ) );
    }

}
