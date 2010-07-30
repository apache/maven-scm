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
import java.util.Date;
import java.util.List;

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
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommand;

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

        AccuRev accuRev = repository.getAccuRev();

        File basedir = fileSet.getBasedir();

        AccuRevInfo info = accuRev.info( basedir );

        if ( !info.isWorkSpace() )
        {
            throw new AccuRevException( "No workspace at " + basedir.getAbsolutePath() );
        }

        String startRevision = getStartRevision( repository, parameters, info );

        ScmVersion scmVersion = parameters.getScmVersion( CommandParameter.SCM_VERSION, null );

        String updateTransactionId = null;

        if ( scmVersion != null )
        {
            AccuRevVersion updateVersion = repository.getAccuRevVersion( scmVersion );

            // Reparent if necessary
            String newBasisStream = updateVersion.getBasisStream();
            if ( newBasisStream != null
                && ( !( newBasisStream.equals( info.getWorkSpace() ) || newBasisStream.equals( info.getBasis() ) ) ) )
            {
                getLogger().info( "Reparenting " + info.getWorkSpace() + " to " + newBasisStream );
                accuRev.chws( basedir, info.getWorkSpace(), newBasisStream );
            }

            if ( !updateVersion.isNow() )
            {
                updateTransactionId = updateVersion.getTimeSpec();
            }
        }

        if ( updateTransactionId == null )
        {
            updateTransactionId = repository.getDepotTransactionId( info.getWorkSpace(), "now" );
        }

        String endRevision = repository.getRevision( info.getWorkSpace(), updateTransactionId );

        List<File> updatedFiles = accuRev.update( basedir, updateTransactionId );

        if ( updatedFiles != null )
        {
            return new AccuRevUpdateScmResult( accuRev.getCommandLines(), getScmFiles( updatedFiles,
                                                                                       ScmFileStatus.UPDATED ),
                                               startRevision, endRevision );
        }
        else
        {
            return new AccuRevUpdateScmResult( accuRev.getCommandLines(), "AccuRev error", accuRev.getErrorOutput(),
                                               null, null, false );
        }
    }

    /*
     * If we are not capturing info for a changelog then we don't need a start revision. Start date is used if supplied
     * otherwise get the current high water mark for the workspace as the start version.
     */
    private String getStartRevision( AccuRevScmProviderRepository repository, CommandParameters parameters,
                                     AccuRevInfo info )
        throws ScmException, AccuRevException
    {

        boolean runChangeLog = parameters.getBoolean( CommandParameter.RUN_CHANGELOG_WITH_UPDATE );
        Date startDate = parameters.getDate( CommandParameter.START_DATE, null );
        String workspace = info.getWorkSpace();

        if ( !runChangeLog )
        {
            return null;
        }
        else
        {
            return startDate == null ? repository.getWorkSpaceRevision( workspace )
                            : repository.getRevision( workspace, startDate );
        }

    }

    public UpdateScmResult update( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (UpdateScmResult) execute( repository, fileSet, parameters );
    }

}
