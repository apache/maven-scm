package org.apache.maven.scm.provider.accurev.command.status;

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
import java.util.List;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevStat;
import org.apache.maven.scm.provider.accurev.CategorisedElements;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommand;

public class AccuRevStatusCommand
    extends AbstractAccuRevCommand
{

    public AccuRevStatusCommand( ScmLogger logger )
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
        @SuppressWarnings( "unchecked" )
        List<File> elements = fileSet.getFileList();

        List<File> defunctElements = accuRev.stat( basedir, elements, AccuRevStat.DEFUNCT );

        if ( defunctElements == null )
        {
            return error( accuRev, "Failed retrieving defunct elements" );
        }

        List<File> keptElements = accuRev.stat( basedir, elements, AccuRevStat.KEPT );

        // Defunct elements are also listed as kept (AccuRev 4.7.1), exclude those here.
        if ( keptElements == null )
        {
            return error( accuRev, "Failed retrieving kept elements" );
        }

        List<File> modOrAddedElements = new ArrayList<File>();

        for ( File file : keptElements )
        {
            if ( !defunctElements.contains( file ) )
            {
                modOrAddedElements.add( file );
            }
        }

        List<File> modifiedElements = accuRev.stat( basedir, elements, AccuRevStat.MODIFIED );

        if ( modifiedElements == null )
        {
            return error( accuRev, "Failed retrieving modified elements" );
        }

        modOrAddedElements.addAll( modifiedElements );

        CategorisedElements catElems = accuRev.statBackingStream( basedir, modOrAddedElements );

        if ( catElems == null )
        {
            return error( accuRev, "Failed stat backing stream to split modified and added elements" );
        }

        List<File> addedElements = catElems.getNonMemberElements();
        modifiedElements = catElems.getMemberElements();

        List<File> missingElements = accuRev.stat( basedir, elements, AccuRevStat.MISSING );

        if ( missingElements == null )
        {
            return error( accuRev, "Failed retrieving missing elements" );
        }

        List<File> externalElements = accuRev.stat( basedir, elements, AccuRevStat.EXTERNAL );

        if ( externalElements == null )
        {
            return error( accuRev, "Failed retrieving external elements" );
        }

        List<ScmFile> resultFiles = getScmFiles( defunctElements, ScmFileStatus.DELETED );
        resultFiles.addAll( getScmFiles( modifiedElements, ScmFileStatus.MODIFIED ) );
        resultFiles.addAll( getScmFiles( addedElements, ScmFileStatus.ADDED ) );
        resultFiles.addAll( getScmFiles( missingElements, ScmFileStatus.MISSING ) );
        resultFiles.addAll( getScmFiles( externalElements, ScmFileStatus.UNKNOWN ) );

        return new StatusScmResult( accuRev.getCommandLines(), resultFiles );

    }

    private ScmResult error( AccuRev accuRev, String message )
    {
        return new StatusScmResult( accuRev.getCommandLines(), "AccuRev " + message, accuRev.getErrorOutput(), false );
    }

    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (StatusScmResult) execute( repository, fileSet, parameters );
    }

}
