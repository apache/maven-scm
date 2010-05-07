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
import java.util.Iterator;
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

        boolean success = true;
        AccuRev accuRev = repository.getAccuRev();

        File basedir = fileSet.getBasedir();
        @SuppressWarnings("unchecked")
        List<File> elements = fileSet.getFileList();

        List<File> defunctElements = new ArrayList<File>();
        List<File> modOrAddedElements = new ArrayList<File>();
        List<File> modifiedElements = new ArrayList<File>();
        List<File> addedElements = new ArrayList<File>();
        List<File> missingElements = new ArrayList<File>();
        List<File> externalElements = new ArrayList<File>();

        success = success && accuRev.stat( basedir, elements, AccuRevStat.DEFUNCT, defunctElements );

        success = success && accuRev.stat( basedir, elements, AccuRevStat.KEPT, modOrAddedElements );

        // Defunct elements are also listed as kept (AccuRev 4.7.1), exclude those here.
        Iterator<File> iter = modOrAddedElements.iterator();
        while ( iter.hasNext() )
        {
            if ( defunctElements.contains( iter.next() ) )
            {
                iter.remove();
            }
        }

        success = success && accuRev.stat( basedir, elements, AccuRevStat.MODIFIED, modOrAddedElements );
        success = success && accuRev.statBackingStream( basedir, modOrAddedElements, modifiedElements, addedElements );

        success = success && accuRev.stat( basedir, elements, AccuRevStat.MISSING, missingElements );
        success = success && accuRev.stat( basedir, elements, AccuRevStat.EXTERNAL, externalElements );

        List<ScmFile> resultFiles = getScmFiles( defunctElements, ScmFileStatus.DELETED );
        resultFiles.addAll( getScmFiles( modifiedElements, ScmFileStatus.MODIFIED ) );
        resultFiles.addAll( getScmFiles( addedElements, ScmFileStatus.ADDED ) );
        resultFiles.addAll( getScmFiles( missingElements, ScmFileStatus.MISSING ) );
        resultFiles.addAll( getScmFiles( externalElements, ScmFileStatus.UNKNOWN ) );

        if ( success )
        {
            return new StatusScmResult( accuRev.getCommandLines(), resultFiles );
        }
        else
        {
            return new StatusScmResult( accuRev.getCommandLines(), "AccuRev Error", accuRev.getErrorOutput(), false );
        }

    }

    public StatusScmResult status( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (StatusScmResult) execute( repository, fileSet, parameters );
    }

}
