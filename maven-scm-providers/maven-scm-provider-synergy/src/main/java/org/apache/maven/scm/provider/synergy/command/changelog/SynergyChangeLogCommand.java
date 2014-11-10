package org.apache.maven.scm.provider.synergy.command.changelog;

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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
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

/**
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 * @author Olivier Lamy
 *
 */
public class SynergyChangeLogCommand
    extends AbstractChangeLogCommand
    implements SynergyCommand
{

    /** {@inheritDoc} */
    protected ChangeLogScmResult executeChangeLogCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                          Date startDate, Date endDate, ScmBranch branch,
                                                          String datePattern )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing changelog command..." );
        }

        SynergyScmProviderRepository repo = (SynergyScmProviderRepository) repository;

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "basedir: " + fileSet.getBasedir() );
        }

        String ccmAddr = SynergyUtil.start( getLogger(), repo.getUser(), repo.getPassword(), null );

        List<ChangeSet> csList = new ArrayList<ChangeSet>();

        try
        {
            String projectSpec =
                SynergyUtil.getWorkingProject( getLogger(), repo.getProjectSpec(), repo.getUser(), ccmAddr );
            if ( projectSpec == null )
            {
                throw new ScmException( "You should checkout a working project first" );
            }
            List<SynergyTask> tasks =
                SynergyUtil.getCompletedTasks( getLogger(), projectSpec, startDate, endDate, ccmAddr );
            for ( SynergyTask t : tasks )
            {
                ChangeSet cs = new ChangeSet();
                cs.setAuthor( t.getUsername() );
                cs.setComment( "Task " + t.getNumber() + ": " + t.getComment() );
                cs.setDate( t.getModifiedTime() );
                cs.setFiles( SynergyUtil.getModifiedObjects( getLogger(), t.getNumber(), ccmAddr ) );
                csList.add( cs );
            }
        }
        finally
        {
            SynergyUtil.stop( getLogger(), ccmAddr );
        }

        return new ChangeLogScmResult( "ccm query ...", new ChangeLogSet( csList, startDate, endDate ) );
    }

}
