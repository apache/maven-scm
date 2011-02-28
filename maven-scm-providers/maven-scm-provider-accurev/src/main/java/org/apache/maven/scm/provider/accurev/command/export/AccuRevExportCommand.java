package org.apache.maven.scm.provider.accurev.command.export;

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
import java.util.Collections;
import java.util.List;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.export.ExportScmResultWithRevision;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevCapability;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevVersion;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevExtractSourceCommand;

public class AccuRevExportCommand
    extends AbstractAccuRevExtractSourceCommand
{

    public AccuRevExportCommand( ScmLogger logger )
    {
        super( logger );
    }

    public ExportScmResult export( ScmProviderRepository repository, ScmFileSet scmFileSet, CommandParameters params )
        throws ScmException
    {
        return (ExportScmResult) execute( repository, scmFileSet, params );
    }

    @Override
    protected List<File> extractSource( AccuRevScmProviderRepository repository, File basedir, AccuRevVersion version )
        throws AccuRevException
    {
        AccuRev accuRev = repository.getAccuRev();
        AccuRevInfo info = accuRev.info( basedir );
        String basisStream = version.getBasisStream();
        String transactionId = version.getTimeSpec();

        if ( !AccuRevVersion.isNow( transactionId )
            && !AccuRevCapability.POPULATE_TO_TRANSACTION.isSupported( accuRev.getClientVersion() ) )
        {
            getLogger().warn(
                              String.format( "Ignoring transaction id %s, Export can only extract current sources",
                                             transactionId ) );
            transactionId = "now";
        } else {
            //We might be heading to a transaction id that is not yet available on a replica
            accuRev.syncReplica();            
        }

        boolean removedWorkspace = false;

        // We'll do a pop -V.

        if ( info.isWorkSpace() )
        {

            String stat = accuRev.stat( basedir );

            if ( stat != null )
            {
                throw new AccuRevException(
                                            String.format(
                                                           "Cannot populate %s, as it is a non-ignored subdirectory of workspace %s rooted at %s.",
                                                           basedir.getAbsolutePath(), info.getWorkSpace(),
                                                           info.getTop() ) );
            }

            // ok, the subdirectory must be ignored. temporarily remove the workspace.
            removedWorkspace = accuRev.rmws( info.getWorkSpace() );

        }

        try
        {
            return accuRev.popExternal(
                                        basedir,
                                        basisStream,
                                        transactionId,
                                        Collections.singletonList( new File( repository.getDepotRelativeProjectPath() ) ) );

        }
        finally
        {
            if ( removedWorkspace )
            {
                accuRev.reactivate( info.getWorkSpace() );
            }
        }
    }

    @Override
    protected ScmResult getScmResult( AccuRevScmProviderRepository repository, List<ScmFile> scmFiles,
                                      ScmVersion scmVersion )
    {
        AccuRev accuRev = repository.getAccuRev();
        if ( scmFiles != null )
        {
            if ( scmVersion == null )
            {
                return new ExportScmResult( accuRev.getCommandLines(), scmFiles );
            }
            else
            {
                return new ExportScmResultWithRevision( accuRev.getCommandLines(), scmFiles, scmVersion.toString() );
            }
        }
        else
        {
            return new ExportScmResult( accuRev.getCommandLines(), "AccuRev Error", accuRev.getErrorOutput(), false );
        }
    }

}
