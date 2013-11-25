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
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;

/**
 * Get a fresh copy of the latest source from the configured scm url.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 */
@Mojo( name = "checkout", requiresProject = false )
public class CheckoutMojo
    extends AbstractScmMojo
{
    /**
     * Use Export instead of checkout
     */
    @Parameter( property = "useExport", defaultValue = "false" )
    private boolean useExport;
    
    /**
     * The directory to checkout the sources to for the bootstrap and checkout goals.
     */
    @Parameter( property = "checkoutDirectory", defaultValue = "${project.build.directory}/checkout" )
    private File checkoutDirectory;

    /**
     * Skip checkout if checkoutDirectory exists.
     */
    @Parameter( property = "skipCheckoutIfExists", defaultValue = "false" )
    private boolean skipCheckoutIfExists = false;

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
     * allow extended mojo (ie BootStrap ) to see checkout result
     */
    private ScmResult checkoutResult;

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        super.execute();

        //skip checkout if checkout directory is already created. See SCM-201
        checkoutResult = null;
        if ( !getCheckoutDirectory().isDirectory() || !this.skipCheckoutIfExists )
        {
            checkoutResult = checkout();
        }
    }

    protected File getCheckoutDirectory()
    {
        return this.checkoutDirectory;
    }

    public void setCheckoutDirectory( File checkoutDirectory )
    {
        this.checkoutDirectory = checkoutDirectory;
    }

    protected ScmResult checkout()
        throws MojoExecutionException
    {
        try
        {
            ScmRepository repository = getScmRepository();

            this.prepareOutputDirectory( getCheckoutDirectory() );
            
            ScmResult result = null;
            
            ScmFileSet fileSet = new ScmFileSet( getCheckoutDirectory().getAbsoluteFile() );
            if ( useExport )
            {
                result = getScmManager().export( repository,fileSet, getScmVersion( scmVersionType, scmVersion ) );
            }
            else
            {
                result = getScmManager().checkOut( repository,fileSet , getScmVersion( scmVersionType, scmVersion ) );
            }

            checkResult( result );
            
           
            handleExcludesIncludesAfterCheckoutAndExport( this.checkoutDirectory );

            return result;
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run checkout command : ", e );
        }
    }

    private void prepareOutputDirectory( File ouputDirectory )
        throws MojoExecutionException
    {
        try
        {
            this.getLog().info( "Removing " + ouputDirectory );

            FileUtils.deleteDirectory( getCheckoutDirectory() );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Cannot remove " + ouputDirectory );
        }
        
        if ( !getCheckoutDirectory().mkdirs() )
        {
            throw new MojoExecutionException( "Cannot create " + ouputDirectory );
        }
    }
    
    protected ScmResult getCheckoutResult()
    {
        return checkoutResult;
    }
    

}
