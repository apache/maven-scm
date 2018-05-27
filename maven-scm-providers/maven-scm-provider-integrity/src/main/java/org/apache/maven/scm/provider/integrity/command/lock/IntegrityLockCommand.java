package org.apache.maven.scm.provider.integrity.command.lock;

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
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.lock.AbstractLockCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Sandbox;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

import java.io.File;

/**
 * MKS Integrity implementation of Maven's AbstractLockCommand
 * <br>This command will execute a 'si lock' on the specified member,
 * which assumes the user has a valid Sandbox to work with.
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @since 1.6
 */
public class IntegrityLockCommand
    extends AbstractLockCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public ScmResult executeLockCommand( ScmProviderRepository repository, File workingDirectory, String filename )
        throws ScmException
    {
        getLogger().info( "Attempting to lock file: " + filename );
        if ( null == filename || filename.length() == 0 )
        {
            throw new ScmException( "A single filename is required to execute the lock command!" );
        }

        ScmResult result;
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        try
        {
            Sandbox siSandbox = iRepo.getSandbox();
            File memberFile = new File( workingDirectory.getAbsoluteFile() + File.separator + filename );
            Response res = siSandbox.lock( memberFile, filename );
            int exitCode = res.getExitCode();
            boolean success = ( exitCode == 0 ? true : false );
            result = new ScmResult( res.getCommandString(), "", "Exit Code: " + exitCode, success );
        }
        catch ( APIException aex )
        {
            ExceptionHandler eh = new ExceptionHandler( aex );
            getLogger().error( "MKS API Exception: " + eh.getMessage() );
            getLogger().info( eh.getCommand() + " exited with return code " + eh.getExitCode() );
            result = new ScmResult( eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false );
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        return executeLockCommand( repository, fileSet.getBasedir(), parameters.getString( CommandParameter.FILE ) );
    }

}
