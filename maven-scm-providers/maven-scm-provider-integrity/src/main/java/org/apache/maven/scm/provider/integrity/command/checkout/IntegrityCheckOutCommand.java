package org.apache.maven.scm.provider.integrity.command.checkout;

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
import com.mks.api.response.Result;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.mks.api.si.SIModelTypeName;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Sandbox;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

/**
 * MKS Integrity implementation for Maven's AbstractCheckOutCommand
 * <br>The Checkout command will create a fresh sandbox in the checkoutDirectory
 * <br>Since, Maven deletes the prior checkout folder, this command will check
 * for a prior sandbox in the checkout directory and if a sandbox was found,
 * then the command will resynchronize the sandbox.
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityCheckOutCommand.java 1.3 2011/08/22 13:06:21EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityCheckOutCommand
    extends AbstractCheckOutCommand
{
    /**
     * Overridden function that performs a checkout (resynchronize) operation against an MKS Source Project
     * This function ignores the scmVerion and recursive arguments passed into this function as while there is
     * a suitable equivalent to checkout/resynchronize by label/revision, it doesn't make sense for the way
     * Maven seems to want to execute this command.  Hence we will create/resynchronize a sandbox, which will
     * be recursive in nature.  If the user chooses to checkout a specific versioned configuration (checkpoint),
     * then that information will be contained in the Configuration Path obtained from the
     * IntegrityScmProviderRepository
     */
    @Override
    public CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                    ScmVersion scmVersion, boolean recursive, boolean shallow )
        throws ScmException
    {
        CheckOutScmResult result;
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        try
        {
            getLogger().info(
                "Attempting to checkout source for project " + iRepo.getProject().getConfigurationPath() );
            String checkoutDir = System.getProperty( "checkoutDirectory" );
            // Override the sandbox definition, if a checkout directory is specified for this command
            Sandbox siSandbox;
            if ( null != checkoutDir && checkoutDir.length() > 0 )
            {
                siSandbox = new Sandbox( iRepo.getAPISession(), iRepo.getProject(), checkoutDir );
                iRepo.setSandbox( siSandbox );
            }
            else
            {
                siSandbox = iRepo.getSandbox();
            }
            getLogger().info( "Sandbox location is " + siSandbox.getSandboxDir() );
            // Now attempt to create the sandbox, if it doesn't already exist
            if ( siSandbox.create() )
            {
                // Resynchronize the new or previously created sandbox
                Response res = siSandbox.resync();
                // Lets output what we got from running this command
                WorkItemIterator wit = res.getWorkItems();
                while ( wit.hasNext() )
                {
                    WorkItem wi = wit.next();
                    if ( wi.getModelType().equals( SIModelTypeName.MEMBER ) )
                    {
                        Result message = wi.getResult();
                        getLogger().debug( wi.getDisplayId() + " " + ( null != message ? message.getMessage() : "" ) );
                    }
                }
                int exitCode = res.getExitCode();
                boolean success = ( exitCode == 0 ? true : false );
                result = new CheckOutScmResult( res.getCommandString(), "", "Exit Code: " + exitCode, success );
            }
            else
            {
                result = new CheckOutScmResult( "si createsandbox", "Failed to create sandbox!", "", false );
            }
        }
        catch ( APIException aex )
        {
            ExceptionHandler eh = new ExceptionHandler( aex );
            getLogger().error( "MKS API Exception: " + eh.getMessage() );
            getLogger().info( eh.getCommand() + " exited with return code " + eh.getExitCode() );
            result = new CheckOutScmResult( eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false );
        }

        return result;
    }

}
