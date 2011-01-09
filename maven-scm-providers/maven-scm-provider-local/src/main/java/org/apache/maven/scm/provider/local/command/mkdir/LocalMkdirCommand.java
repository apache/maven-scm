package org.apache.maven.scm.provider.local.command.mkdir;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.mkdir.AbstractMkdirCommand;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.add.LocalAddCommand;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version $Id$
 */
public class LocalMkdirCommand
    extends AbstractMkdirCommand
{
    protected MkdirScmResult executeMkdirCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message,
                                                  boolean createInLocal )
        throws ScmException
    {
        LocalScmProviderRepository repo = (LocalScmProviderRepository) repository;
        List<ScmFile> createdDirs = new ArrayList<ScmFile>();

        // create/commit the directory directly in the repository
        if ( !createInLocal )
        {
            File file = (File) fileSet.getFileList().get( 0 );
            File modulePath = new File( repo.getRoot(), repo.getModule() );
            File dir = new File( modulePath, file.getName() );

            if ( dir.exists() )
            {
                return new MkdirScmResult( null, "Directory already exists!", "Directory already exists.", false );
            }
            else
            {
                if ( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "Creating directory in '" + modulePath.getAbsolutePath() + "'" );
                }

                FileUtils.mkdir( dir.getAbsolutePath() );
                createdDirs.add( new ScmFile( dir.getPath(), ScmFileStatus.ADDED ) );
            }
        }
        else
        {
            // add the directory, but not commit
            LocalAddCommand addCmd = new LocalAddCommand();
            addCmd.setLogger( getLogger() );

            CommandParameters parameters = new CommandParameters();
            parameters.setString( CommandParameter.MESSAGE, message );
            parameters.setString( CommandParameter.BINARY, "false" );

            String path = ( (File) fileSet.getFileList().get( 0 ) ).getPath();
            if ( repo.isFileAdded( path ) )
            {
                return new MkdirScmResult( null, "Directory already exists!", "Directory already exists.", false );
            }

            AddScmResult result = (AddScmResult) addCmd.execute( repository, fileSet, parameters );
            createdDirs.addAll( result.getAddedFiles() );
        }

        return new MkdirScmResult( null, createdDirs );
    }
}
