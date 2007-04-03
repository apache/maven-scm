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
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Get a fresh copy of the latest source from the configured scm url.
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal checkout
 * @description Check out a project
 * @requiresProject false
 */
public class CheckoutMojo
    extends AbstractScmMojo
{
    /**
     * The directory to checkout the sources to for the bootstrap and checkout goals.
     *
     * @parameter expression="${checkoutDirectory}" default-value="${project.build.directory}/checkout"
     */
    private File checkoutDirectory;

    /**
     * Skip checkout if checkoutDirectory exists.
     *
     * @parameter expression="${skipCheckoutIfExists}" default-value="false"
     */
    private boolean skipCheckoutIfExists = false;

    /**
     * The version type (branch/tag/revision) of scmVersion.
     *
     * @parameter expression="${scmVersionType}"
     */
    private String scmVersionType;

    /**
     * The version (revision number/branch name/tag name).
     *
     * @parameter expression="${scmVersion}"
     */
    private String scmVersion;

    public void execute()
        throws MojoExecutionException
    {
        //skip checkout if checkout directory is already created. See SCM-201
        if ( !getCheckoutDirectory().isDirectory() || !this.skipCheckoutIfExists )
        {
            checkout();
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

    protected CheckOutScmResult checkout()
        throws MojoExecutionException
    {
        try
        {
            ScmRepository repository = getScmRepository();

            try
            {
                this.getLog().info( "Removing " + getCheckoutDirectory() );

                FileUtils.deleteDirectory( getCheckoutDirectory() );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Cannot remove " + getCheckoutDirectory() );
            }

            if ( !getCheckoutDirectory().mkdirs() )
            {
                throw new MojoExecutionException( "Cannot create " + getCheckoutDirectory() );
            }

            CheckOutScmResult result = getScmManager().checkOut( repository, new ScmFileSet(
                getCheckoutDirectory().getAbsoluteFile() ), getScmVersion( scmVersionType, scmVersion ) );

            checkResult( result );

            return result;
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run checkout command : ", e );
        }
    }
}
