package org.apache.maven.scm.plugin;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * @parameter expression="${branch}
     */
    private String branch;

    /**
     * The tag to use when checking out or tagging a project.
     *
     * @parameter expression="${tag}
     */
    private String tag;

    /**
     * The directory to checkout the sources to for the bootstrap and checkout goals
     *
     * @parameter expression="${checkoutDirectory}" default-value="${project.build.directory}/checkout"
     */
    private File checkoutDirectory;

    public void execute()
        throws MojoExecutionException
    {
        checkout();
    }

    protected File getCheckoutDirectory()
    {
        return this.checkoutDirectory;
    }

    protected void checkout()
        throws MojoExecutionException
    {
        try
        {
            ScmRepository repository = getScmRepository();

            String currentTag = null;

            if ( branch != null )
            {
                currentTag = branch;
            }

            if ( tag != null )
            {
                currentTag = tag;
            }

            try
            {
                this.getLog().info( "Removing " + this.checkoutDirectory );

                FileUtils.deleteDirectory( this.checkoutDirectory );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Cannot remove " + this.checkoutDirectory );
            }

            if ( ! this.checkoutDirectory.mkdirs() )
            {
                throw new MojoExecutionException( "Cannot create " + this.checkoutDirectory );
            }

            CheckOutScmResult result = getScmManager().getProviderByRepository( repository ).checkOut( repository,
                                                                                                       new ScmFileSet(
                                                                                                           this.checkoutDirectory.getAbsoluteFile() ),
                                                                                                       currentTag );

            checkResult( result );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run checkout command : ", e );
        }
    }
}
