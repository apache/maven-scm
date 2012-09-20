package org.apache.maven.scm.plugin;

import java.io.File;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.repository.ScmRepository;

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

/**
 * This mojo will fail the build if there is any local modifications
 * @goal check-local-modification
 * @author Olivier Lamy
 * @since 1.2
 */
public class CheckLocalModificationsMojo
    extends AbstractScmMojo
{

    /**
     * Custom error message
     *
     * @parameter expression="${scm.checkLocalModification.errorMessage}" 
     *            default-value="The build will stop as there is local modifications";
     */
    private String errorMessage; 
    
    /**
     * Custom error message
     *
     * @parameter expression="${scm.checkLocalModification.skip}" default-value="false";
     */    
    private boolean skip;
    
    /**
     * current directory
     *
     * @parameter default-value="${basedir}";
     * @readonly
     */     
    private File baseDirectory;

    public void execute()
        throws MojoExecutionException
    {
        if ( skip )
        {
            getLog().info( "check-local-modification execution has been skipped" );
            return;
        }
        super.execute();

        StatusScmResult result = null;

        try
        {
            ScmRepository repository = getScmRepository();
            result = getScmManager().status( repository, new ScmFileSet( baseDirectory ) );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        if ( !result.isSuccess() )
        {
            throw new MojoExecutionException( "Unable to check for local modifications :" + result.getProviderMessage() );
        }

        if ( !result.getChangedFiles().isEmpty() )
        {
            getLog().error( errorMessage );
            throw new MojoExecutionException( errorMessage );
        }

    }
    
}
