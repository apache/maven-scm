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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;

/**
 * Pull the project source from the configured scm and execute the configured goals.
 *
 * @author <a href="dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 * @goal bootstrap
 * @requiresProject false
 */
public class BootstrapMojo
    extends CheckoutMojo
{
    /**
     * The goals to run on the clean checkout of a project for the bootstrap goal.
     * If none are specified, then the default goal for the project is executed.
     * Multiple goals should be comma separated.
     *
     * @parameter expression="${goals}"
     */
    private String goals;

    /**
     * A list of profiles to run with the goals.
     * Multiple profiles must be comma separated with no spaces.
     *
     * @parameter expression="${profiles}"
     */
    private String profiles;

    /**
     * The subdirectory (under the project directory) in which to run the goals.
     * The project directory is the same as the checkout directory in most cases,
     * but for some SCMs, it is a subdirectory of the checkout directory.
     *
     * @parameter expression="${goalsDirectory}" default-value=""
     */
    private String goalsDirectory;

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        super.execute();

        if ( this.getCheckoutResult() != null )
        {
            runGoals( this.getCheckoutResult().getRelativePathProjectDirectory() );
        }
    }

    /**
     * @param relativePathProjectDirectory the project directory's path relative to the checkout
     *                                     directory; or "" if they are the same
     * @throws MojoExecutionException if any
     */
    private void runGoals( String relativePathProjectDirectory )
        throws MojoExecutionException
    {
        Commandline cl = new Commandline();
        try
        {
            cl.addSystemEnvironment();
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Can't add system environment variables to mvn command line.", e );
        }
        cl.addEnvironment( "MAVEN_TERMINATE_CMD", "on" );
        cl.setExecutable( "mvn" );
        cl.setWorkingDirectory( determineWorkingDirectoryPath( this.getCheckoutDirectory(),
                                                               relativePathProjectDirectory, goalsDirectory ) );

        if ( this.goals != null )
        {
            String[] tokens = StringUtils.split( this.goals, ", " );

            for ( int i = 0; i < tokens.length; ++i )
            {
                cl.createArg().setValue( tokens[i] );
            }
        }

        if ( ! StringUtils.isEmpty( this.profiles ) )
        {
            cl.createArg().setValue( "-P" + this.profiles );
        }

        StreamConsumer consumer = new DefaultConsumer();

        try
        {
            int result = CommandLineUtils.executeCommandLine( cl, consumer, consumer );

            if ( result != 0 )
            {
                throw new MojoExecutionException( "Result of mvn execution is: \'" + result + "\'. Release failed." );
            }
        }
        catch ( CommandLineException e )
        {
            throw new MojoExecutionException( "Can't run goal " + goals, e );
        }
    }

    /**
     * Determines the path of the working directory. By default, this is the checkout directory. For some SCMs,
     * the project root directory is not the checkout directory itself, but a SCM-specific subdirectory. The
     * build can furthermore optionally be executed in a subdirectory of this project directory, in case.
     *
     * @param checkoutDirectory
     * @param relativePathProjectDirectory
     * @param goalsDirectory
     * @return
     */
    protected String determineWorkingDirectoryPath( File checkoutDirectory, String relativePathProjectDirectory,
                                                    String goalsDirectory )
    {
        File projectDirectory;
        if ( StringUtils.isNotEmpty( relativePathProjectDirectory ) )
        {
            projectDirectory = new File( checkoutDirectory, relativePathProjectDirectory );
        }
        else
        {
            projectDirectory = checkoutDirectory;
        }

        if ( StringUtils.isEmpty( goalsDirectory ) )
        {
            return projectDirectory.getPath();
        }

        return new File( projectDirectory, goalsDirectory ).getPath();
    }
}
