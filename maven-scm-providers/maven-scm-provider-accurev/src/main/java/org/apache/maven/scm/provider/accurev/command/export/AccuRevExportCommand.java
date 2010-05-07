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
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevExtractSourceCommand;
import org.codehaus.plexus.util.StringUtils;

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
    protected boolean extractSource( AccuRevScmProviderRepository repository, File basedir, String basisStream,
                                     String transactionId, List<File> checkedOutFiles )
        throws AccuRevException
    {
        AccuRev accuRev = repository.getAccuRev();
        AccuRevInfo info = accuRev.info( basedir );

        validateTransactionId( transactionId );

        boolean removedWorkspace = false;

        boolean success;
        // We'll do a pop -V.

        if ( info.isWorkSpace() )
        {

            String stat = accuRev.stat( basedir );

            if ( stat != null )
            {
                throw new AccuRevException( String
                    .format( "Cannot populate %s, as it is a non-ignored subdirectory of workspace %s rooted at %s.",
                             basedir.getAbsolutePath(), info.getWorkSpace(), info.getTop() ) );
            }

            // ok, the subdirectory must be ignored. temporarily remove the workspace.
            removedWorkspace = accuRev.rmws( info.getWorkSpace() );

        }

        try
        {
            success = accuRev.pop( basedir, basisStream, Collections.singletonList( new File( repository
                .getDepotRelativeProjectPath() ) ), checkedOutFiles );

        }
        finally
        {
            if ( removedWorkspace )
            {
                accuRev.reactivate( info.getWorkSpace() );
            }
        }
        return success;
    }

    private void validateTransactionId( String transactionId )
        throws AccuRevException
    {
        if ( StringUtils.isBlank( transactionId ) )
        {
            return;
        }

        transactionId = transactionId.trim();

        if ( "highest".equals( transactionId ) || "now".equals( transactionId ) )
        {
            return;
        }

        throw new AccuRevException( "Transaction id " + transactionId
            + " out of range. Export can only extract current sources" );
    }

    @Override
    protected ScmResult getScmResult( AccuRevScmProviderRepository repository, List<ScmFile> scmFiles, boolean success )
    {
        AccuRev accuRev = repository.getAccuRev();
        if ( success )
        {
            return new ExportScmResult( accuRev.getCommandLines(), scmFiles );
        }
        else
        {
            return new ExportScmResult( accuRev.getCommandLines(), "AccuRev Error", accuRev.getErrorOutput(), false );
        }
    }

}
