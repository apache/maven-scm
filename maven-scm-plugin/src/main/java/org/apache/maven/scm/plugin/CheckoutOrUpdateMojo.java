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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.mountrepos.MountReposScmProviderRepository;
import org.apache.maven.scm.provider.mountrepos.MountReposScmProviderRepository.MountProjectRepository;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * checkout or update
 */
@Mojo( name = "checkout-or-update", requiresProject = false )
public class CheckoutOrUpdateMojo
    extends AbstractScmMojo
{

    /**
     * The directory to checkout the sources to for the bootstrap and checkout goals.
     */
    @Parameter( property = "checkoutDirectory", defaultValue = "${project.build.directory}/checkout" )
    private File checkoutDirectory;

    /**
     * The version type (branch/tag/revision) of scmVersion.
     */
    @Parameter( property = "scmVersionType" )
    private String scmVersionType;

    /**
     * The version (revision number/branch name/tag name).
     */
    @Parameter( property = "scmVersion" )
    private String scmVersion;

    /**
     * result of checkout
     */
    private ScmResult checkoutResult;

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        super.execute();

        if ( this.checkoutDirectory.getPath().contains( "${project.basedir}" ) )
        {
            //project.basedir is not set under maven 3.x when run without a project
            this.checkoutDirectory = new File( this.getBasedir(), "target/checkout" );
        }
        if ( ! this.checkoutDirectory.exists() )
        {
            if ( !this.checkoutDirectory.mkdirs() )
            {
                throw new MojoExecutionException( "Cannot create " + this.checkoutDirectory );
            }
        }

        try
        {
            ScmRepository repository = getScmRepository();
            ScmProviderRepository scmRepository = repository.getProviderRepository();

            ScmResult result = null;

            boolean alreadyCheckout = false;
            ScmProvider scmProvider = getScmManager().getProviderByRepository( repository );
            String scmSpecificFilename = scmProvider.getScmSpecificFilename();
            if ( scmSpecificFilename != null )
            {
                File scmSpecificFile = new File( checkoutDirectory.getAbsoluteFile(), scmSpecificFilename);
                alreadyCheckout = scmSpecificFile.exists();
            }

            if ( ! alreadyCheckout && scmRepository instanceof MountReposScmProviderRepository )
            {
                result = updateOrCheckoutMulti( (MountReposScmProviderRepository) scmRepository );
            }
            else
            {
                ScmFileSet fileSet = new ScmFileSet( checkoutDirectory.getAbsoluteFile() );
                if ( alreadyCheckout )
                {
                    result = getScmManager().update( repository, fileSet );
                }
                else
                {
                    result = getScmManager().checkOut( repository, fileSet, getScmVersion( scmVersionType, scmVersion ) );
                }

                checkResult( result );
            }

            handleExcludesIncludesAfterCheckoutAndExport( this.checkoutDirectory );
            this.checkoutResult = result;
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run checkout command : ", e );
        }
    }

    private CheckOutScmResult updateOrCheckoutMulti( MountReposScmProviderRepository mountRepos )
            throws MojoExecutionException
    {
        ScmVersion scmVersionObj = getScmVersion( scmVersionType, scmVersion );
        boolean recursive = true; // parameters.getBoolean( CommandParameter.RECURSIVE, true );
        // boolean shallow = parameters.getBoolean( CommandParameter.SHALLOW, false );

        List<String> failedProjectNames = new ArrayList<>();
        StringBuilder exceptionsMessage = new StringBuilder();

        List<ScmFile> checkedOutFiles = new ArrayList<ScmFile>();
        for (MountProjectRepository mountProject : mountRepos.getProjectScmProviderRepositories().values())
        {
            ScmRepository mountScmRepository = mountProject.getScmRepository();
            ScmVersion mountScmVersionObj = getScmVersion( "branch", mountProject.getRevision() );

            ScmFileSet mountScmFileSet = new ScmFileSet( new File( checkoutDirectory, mountProject.getPath() ) );

            try
            {
                CheckOutScmResult mountRes = getScmManager().checkOut(mountScmRepository, mountScmFileSet, mountScmVersionObj, recursive );

                checkedOutFiles.addAll( mountRes.getCheckedOutFiles() );
            }
            catch( Exception ex )
            {
                failedProjectNames.add( mountProject.getRepoProject().getName() );
                exceptionsMessage.append( ex.getMessage() + " project='" + mountProject.getRepoProject().getName() + "' in path='" + mountProject.getPath() + "'\n" );
            }
        }
        if ( ! failedProjectNames.isEmpty() )
        {
            throw new MojoExecutionException( "Failed to checkout " + failedProjectNames.size() + " sub project(s) "
                    + failedProjectNames
                    + " exceptions: " + exceptionsMessage);
        }

        return new CheckOutScmResult( "repo update", scmVersion, checkedOutFiles );
    }

    protected File getCheckoutDirectory()
    {
        return this.checkoutDirectory;
    }

    public void setCheckoutDirectory( File checkoutDirectory )
    {
        this.checkoutDirectory = checkoutDirectory;
    }

    protected ScmResult getCheckoutResult()
    {
        return checkoutResult;
    }

}
