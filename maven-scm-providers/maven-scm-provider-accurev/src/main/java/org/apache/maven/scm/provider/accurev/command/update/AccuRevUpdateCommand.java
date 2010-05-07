package org.apache.maven.scm.provider.accurev.command.update;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevVersion;
import org.apache.maven.scm.provider.accurev.WorkSpace;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommand;
import org.codehaus.plexus.util.StringUtils;

public class AccuRevUpdateCommand
    extends AbstractAccuRevCommand
{

    public AccuRevUpdateCommand( ScmLogger logger )
    {
        super( logger );
    }

    @Override
    protected ScmResult executeAccurevCommand( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                               CommandParameters parameters )
        throws ScmException, AccuRevException
    {

        ArrayList<File> updatedFiles = new ArrayList<File>();

        AccuRev accuRev = repository.getAccuRev();

        File basedir = fileSet.getBasedir();

        AccuRevInfo info = accuRev.info( basedir );

        if ( !info.isWorkSpace() )
        {
            throw new AccuRevException( "No workspace at " + basedir.getAbsolutePath() );
        }

        boolean success = true;

        AccuRevVersion startVersion = getStartVersion( repository, parameters, info );

        AccuRevVersion endVersion = getEndVersion( repository, parameters, info );

        String newBasisStream = endVersion.getBasisStream();
        if ( newBasisStream != null && !newBasisStream.equals( info.getBasis() ) )
        {
            getLogger().info( "Reparenting " + info.getWorkSpace() + " to " + newBasisStream );
            success = accuRev.chws( basedir, info.getWorkSpace(), newBasisStream );
        }

        success = success && accuRev.update( basedir, endVersion.getTimeSpec(), updatedFiles );

        if ( success )
        {
            return new AccuRevUpdateScmResult( startVersion, endVersion, accuRev.getCommandLines(),
                                               getScmFiles( updatedFiles, ScmFileStatus.UPDATED ) );
        }
        else
        {
            return new UpdateScmResult( accuRev.getCommandLines(), "AccuRev error", accuRev.getErrorOutput(), false );
        }
    }

    /*
     * If we are not capturing info for a changelog, return null. If start date is supplied then
     * start version is the current workspace basis stream / start_date Otherwise get the current
     * high water mark for the workspace as the start version.
     */
    private AccuRevVersion getStartVersion( AccuRevScmProviderRepository repository, CommandParameters parameters,
                                            AccuRevInfo info )
        throws ScmException, AccuRevException
    {
        AccuRevVersion startVersion = null;

        AccuRev accuRev = repository.getAccuRev();

        boolean runChangeLog = parameters.getBoolean( CommandParameter.RUN_CHANGELOG_WITH_UPDATE );
        Date startDate = parameters.getDate( CommandParameter.START_DATE, null );

        if ( runChangeLog )
        {
            if ( startDate == null )
            {
                // Get tran id before the update, and add one.
                startVersion = new AccuRevVersion( info.getBasis(), 1 + getCurrentTransactionId( info.getWorkSpace(),
                                                                                                 accuRev ) );
            }
            else
            {
                // Use the supplied date (assume same basis, TODO not strictly correct)
                startVersion = new AccuRevVersion( info.getBasis(), startDate );
            }
        }
        return startVersion;
    }

    /*
     * End version timespec is used as the -t parameter to update. If a version is specified in
     * parameters then we use that. If "now" or "highest" is specified as the timespec it is
     * replaced with the "now" as a date, so that we can be 100% accurate in terms of the changelog.
     * If no version is specified then we use the current workspace basis stream and "now" as a
     * date.
     */
    private AccuRevVersion getEndVersion( AccuRevScmProviderRepository repository, CommandParameters parameters,
                                          AccuRevInfo info )
        throws ScmException
    {
        AccuRevVersion endVersion = null;
        ScmVersion scmVersion = parameters.getScmVersion( CommandParameter.SCM_VERSION, null );

        if ( scmVersion != null )
        {
            endVersion = repository.getAccuRevVersion( scmVersion );
        }
        else
        {
            endVersion = new AccuRevVersion( info.getBasis(), (String) null );
        }
        return endVersion;
    }

    private long getCurrentTransactionId( String workSpaceName, AccuRev accuRev )
        throws AccuRevException
    {
        // AccuRev does not have a way to get at this workspace info by name.
        // So we have to do it the hard way...

        Map<String, WorkSpace> workSpaces = new HashMap<String, WorkSpace>();

        accuRev.showWorkSpaces( workSpaces );

        WorkSpace workspace = workSpaces.get( workSpaceName );

        if ( workspace == null )
        {
            // Must be a reftree
            accuRev.showRefTrees( workSpaces );
            workspace = workSpaces.get( workSpaceName );
        }

        if ( workspace == null )
        {
            getLogger().warn( "Can't find workspace " + workSpaceName );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().warn( StringUtils.join( workSpaces.values().iterator(), "\n" ) );
            }
            return 0;
        }
        return workspace.getTransactionId();
    }

    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (UpdateScmResult) execute( repository, fileSet, parameters );
    }

}
