package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ScmBranchParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.IOException;

/**
 * Branch the project.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal branch
 * @aggregator
 */
public class BranchMojo
    extends AbstractScmMojo
{
    /**
     * The branch name.
     *
     * @parameter expression="${branch}"
     * @required
     */
    private String branch;

    /**
     * The message applied to the tag creation.
     *
     * @parameter expression="${message}"
     */
    private String message;
    
    /**
     * currently only implemented with svn scm. Enable a workaround to prevent issue 
     * due to svn client > 1.5.0 (http://jira.codehaus.org/browse/SCM-406)
     *      
     * 
     * @parameter expression="${remoteBranching}" default-value="true"
     * @since 1.3
     */    
    private boolean remoteBranching;     

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        super.execute();

        try
        {
            ScmRepository repository = getScmRepository();
            ScmProvider provider = getScmManager().getProviderByRepository( repository );

            String finalBranch = provider.sanitizeTagName( branch );
            getLog().info( "Final Branch Name: '" + finalBranch + "'" );

            ScmBranchParameters scmBranchParameters = new ScmBranchParameters( message );
            scmBranchParameters.setRemoteBranching( remoteBranching );
            
            BranchScmResult result = provider.branch( repository, getFileSet(), finalBranch, scmBranchParameters );

            checkResult( result );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Cannot run branch command : ", e );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run branch command : ", e );
        }
    }
}
