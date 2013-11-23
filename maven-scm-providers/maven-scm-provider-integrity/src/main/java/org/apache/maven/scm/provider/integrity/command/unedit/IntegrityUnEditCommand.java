package org.apache.maven.scm.provider.integrity.command.unedit;

/**
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
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.unedit.AbstractUnEditCommand;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Sandbox;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

/**
 * MKS Integrity implementation of Maven's AbstractUnEditCommand
 * <br>This command will execute a 'si revert' command which will revert
 * any modified working files back to their server versions
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityUnEditCommand.java 1.4 2011/08/22 13:06:39EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityUnEditCommand
    extends AbstractUnEditCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public UnEditScmResult executeUnEditCommand( ScmProviderRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        getLogger().info( "Attempting to revert members in sandbox " + fileSet.getBasedir().getAbsolutePath() );
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        UnEditScmResult result;
        try
        {
            Sandbox siSandbox = iRepo.getSandbox();
            Response res = siSandbox.revertMembers();
            int exitCode = res.getExitCode();
            boolean success = ( exitCode == 0 ? true : false );
            result = new UnEditScmResult( res.getCommandString(), "", "Exit Code: " + exitCode, success );
        }
        catch ( APIException aex )
        {
            ExceptionHandler eh = new ExceptionHandler( aex );
            getLogger().error( "MKS API Exception: " + eh.getMessage() );
            getLogger().info( eh.getCommand() + " exited with return code " + eh.getExitCode() );
            result = new UnEditScmResult( eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false );
        }

        return result;
    }
}
