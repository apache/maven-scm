package org.apache.maven.scm.provider.integrity.command.login;

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
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.login.AbstractLoginCommand;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.APISession;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Project;
import org.apache.maven.scm.provider.integrity.Sandbox;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

/**
 * MKS Integrity implementation of Maven's AbstractLoginCommand
 * <br>This command will execute a 'si login' followed by a 'si viewproject'
 * to prepare the subsequent commands that will be executed for a maven goal.
 * <br>The login command uses a Local Client API Integration Point and hence the
 * <br>MKS Integrity Client is required to be installed on the local client as a
 * pre-requisite.
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityLoginCommand.java 1.2 2011/08/22 13:06:33EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityLoginCommand
    extends AbstractLoginCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public LoginScmResult executeLoginCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                               CommandParameters params )
        throws ScmException
    {
        getLogger().info( "Attempting to connect with the MKS Integrity Server" );
        LoginScmResult result;
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        APISession api = iRepo.getAPISession();
        try
        {
            // First we will establish a connection to the MKS Integrity Server
            Response res = api.connect( iRepo.getHost(), iRepo.getPort(), iRepo.getUser(), iRepo.getPassword() );
            int exitCode = res.getExitCode();
            boolean success = ( exitCode == 0 ? true : false );
            result = new LoginScmResult( res.getCommandString(), "", "Exit Code: " + exitCode, success );

            // Next we will prepare the Project and Sandbox for the other commands
            Project siProject = new Project( api, iRepo.getConfigruationPath() );
            Sandbox siSandbox = new Sandbox( api, siProject, fileSet.getBasedir().getAbsolutePath() );
            iRepo.setProject( siProject );
            iRepo.setSandbox( siSandbox );
        }
        catch ( APIException aex )
        {
            ExceptionHandler eh = new ExceptionHandler( aex );
            getLogger().error( "MKS API Exception: " + eh.getMessage() );
            getLogger().info( eh.getCommand() + " exited with return code " + eh.getExitCode() );
            result = new LoginScmResult( eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false );
        }

        return result;
    }

}
