package org.apache.maven.scm.provider.accurev.command.checkout;

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
import java.util.List;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevExtractSourceCommand;

public class AccuRevCheckOutCommand
    extends AbstractAccuRevExtractSourceCommand
{

    public AccuRevCheckOutCommand( ScmLogger logger )
    {
        super( logger );
    }

    public CheckOutScmResult checkout( ScmProviderRepository repository, ScmFileSet fileSet,
                                       CommandParameters parameters )
        throws ScmException
    {

        return (CheckOutScmResult) execute( repository, fileSet, parameters );
    }

    @Override
    protected boolean extractSource( AccuRevScmProviderRepository repository, File basedir, String basisStream,
                                     String transactionId, List<File> checkedOutFiles )
        throws AccuRevException
    {
        AccuRev accuRev = repository.getAccuRev();

        AccuRevInfo info = accuRev.info( basedir );

        boolean success = true;
        if ( info.isWorkSpace() )
        {

            if ( !repository.isWorkSpaceTop( info ) )
            {
                throw new AccuRevException( String
                    .format( "Can't checkout to %s, a subdirectory of existing workspace %s", basedir, info
                        .getWorkSpace() ) );
            }
            // workspace exists at this basedir already.
            if ( !basisStream.equals( info.getBasis() ) )
            {
                // different basis, reparent.
                success = accuRev.chws( basedir, info.getWorkSpace(), basisStream );
            }

            if ( success )
            {
                // repopulate everything in the workspace.
                success = accuRev.pop( basedir, null, checkedOutFiles );

            }

        }
        else
        {
            // not a workspace, make one...
            // TODO set incl rules to only include the projectPath

            String workSpaceName = getWorkSpaceName( basedir, basisStream );

            success = accuRev.mkws( basisStream, workSpaceName, basedir );

            if ( success )
            {
                getLogger().info( "Created workspace " + workSpaceName );
            }
        }

        if ( success )
        {
            success = accuRev.update( basedir, transactionId, checkedOutFiles );
        }
        return success;
    }

    @Override
    protected ScmResult getScmResult( AccuRevScmProviderRepository repository, List<ScmFile> scmFiles, boolean success )
    {

        AccuRev accuRev = repository.getAccuRev();
        if ( success )
        {
            return new CheckOutScmResult( accuRev.getCommandLines(), scmFiles, repository.getProjectPath() );
        }
        else
        {
            return new CheckOutScmResult( accuRev.getCommandLines(), "AccuRev Error", accuRev.getErrorOutput(), false );
        }
    }

    public static String getWorkSpaceName( File basedir, String basisStream )
    {
        String workSpaceName;
        String baseName = basedir.getName();
        if ( baseName.contains( basisStream ) )
        {
            workSpaceName = baseName;
        }
        else if ( basisStream.contains( baseName ) )
        {
            workSpaceName = basisStream;
        }
        else
        {
            workSpaceName = basisStream + "_" + baseName;
        }
        return workSpaceName;
    }

}
