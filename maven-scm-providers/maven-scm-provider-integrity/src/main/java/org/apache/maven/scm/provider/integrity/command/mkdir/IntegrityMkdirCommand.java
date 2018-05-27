package org.apache.maven.scm.provider.integrity.command.mkdir;

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

import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.mkdir.AbstractMkdirCommand;
import org.apache.maven.scm.command.mkdir.MkdirScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * MKS Integrity implementation of Maven's AbstractMkdirCommand
 * <br>This command will execute an 'si createsubproject' for the relative path
 * represented in the fileSet.getFileList().iterator().next() entry.
 * <br>A single subproject is created required for every directory encountered
 * in the relative path.
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @since 1.6
 */
public class IntegrityMkdirCommand
    extends AbstractMkdirCommand
{
    /**
     * Creates a subproject in the Integrity repository.
     * <br>However, since the subproject automatically creates a folder in
     * the local sandbox, the createInLocal argument will be ignored
     */
    @Override
    public MkdirScmResult executeMkdirCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message,
                                               boolean createInLocal )
        throws ScmException
    {
        String dirPath = "";
        Iterator<File> fit = fileSet.getFileList().iterator();
        if ( fit.hasNext() )
        {
            dirPath = fit.next().getPath().replace( '\\', '/' );
        }
        if ( null == dirPath || dirPath.length() == 0 )
        {
            throw new ScmException( "A relative directory path is required to execute this command!" );
        }
        getLogger().info( "Creating subprojects one per directory, as required for " + dirPath );
        MkdirScmResult result;
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        try
        {
            Response res = iRepo.getSandbox().createSubproject( dirPath );
            String subProject = res.getWorkItems().next().getResult().getField( "resultant" ).getItem().getDisplayId();
            List<ScmFile> createdDirs = new ArrayList<ScmFile>();
            createdDirs.add( new ScmFile( subProject, ScmFileStatus.ADDED ) );
            int exitCode = res.getExitCode();
            boolean success = ( exitCode == 0 ? true : false );
            getLogger().info( "Successfully created subproject " + subProject );
            result = new MkdirScmResult( createdDirs,
                                         new ScmResult( res.getCommandString(), "", "Exit Code: " + exitCode,
                                                        success ) );
        }
        catch ( APIException aex )
        {
            ExceptionHandler eh = new ExceptionHandler( aex );
            getLogger().error( "MKS API Exception: " + eh.getMessage() );
            getLogger().info( eh.getCommand() + " exited with return code " + eh.getExitCode() );
            result = new MkdirScmResult( eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false );
        }

        return result;
    }

}
