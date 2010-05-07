package org.apache.maven.scm.provider.accurev.command;

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
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;

public abstract class AbstractAccuRevCommand
    extends AbstractCommand
{

    public AbstractAccuRevCommand( ScmLogger logger )
    {
        super();
        setLogger( logger );
    }

    protected abstract ScmResult executeAccurevCommand( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                                        CommandParameters parameters )
        throws ScmException, AccuRevException;

    protected final ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                              CommandParameters parameters )
        throws ScmException
    {

        if ( !( repository instanceof AccuRevScmProviderRepository ) )
        {
            throw new ScmException( "Not an AccuRev repository " + repository );
        }

        AccuRevScmProviderRepository accuRevRepository = (AccuRevScmProviderRepository) repository;
        accuRevRepository.getAccuRev().reset();
        try
        {
            return executeAccurevCommand( accuRevRepository, fileSet, parameters );
        }
        catch ( AccuRevException e )
        {
            throw new ScmException( "Error invoking AccuRev command", e );
        }
    }

    protected static List<ScmFile> getScmFiles( final List<File> files, ScmFileStatus status )
    {
        ArrayList<ScmFile> resultFiles = new ArrayList<ScmFile>( files.size() );
        for ( File addedFile : files )
        {
            // TODO paths are relative to the workspace dir, should be made relative to project path.
            resultFiles.add( new ScmFile( addedFile.getPath(), status ) );
        }
        return resultFiles;
    }

}