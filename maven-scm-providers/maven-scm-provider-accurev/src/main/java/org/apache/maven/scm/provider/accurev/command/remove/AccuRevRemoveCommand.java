package org.apache.maven.scm.provider.accurev.command.remove;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommand;

public class AccuRevRemoveCommand
    extends AbstractAccuRevCommand
{

    public AccuRevRemoveCommand( ScmLogger logger )
    {
        super( logger );
    }

    @Override
    protected ScmResult executeAccurevCommand( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                               CommandParameters parameters )
        throws ScmException, AccuRevException
    {
        final List<File> removedFiles = new ArrayList<File>();

        AccuRev accuRev = repository.getAccuRev();

        String message = parameters.getString( CommandParameter.MESSAGE, "" );

        boolean success = true;

        File basedir = fileSet.getBasedir();

        @SuppressWarnings("unchecked")
        List<File> relativeFiles = fileSet.getFileList();

        success = accuRev.defunct( basedir, relativeFiles, message, removedFiles );

        List<ScmFile> resultFiles = getScmFiles( removedFiles, ScmFileStatus.DELETED );

        if ( success )
        {
            return new RemoveScmResult( accuRev.getCommandLines(), resultFiles );
        }
        else
        {
            return new RemoveScmResult( accuRev.getCommandLines(), "AccuRev Error", accuRev.getErrorOutput(), false );
        }
    }

    public RemoveScmResult remove( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (RemoveScmResult) execute( repository, fileSet, parameters );
    }

}
