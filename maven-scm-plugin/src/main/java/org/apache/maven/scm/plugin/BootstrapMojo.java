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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;
//update
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.update.UpdateScmResultWithRevision;
import org.apache.maven.scm.ScmException;
import org.apache.maven.project.MavenProject;
import java.io.IOException;

/**
 * Pull the project source from the configured scm and execute the configured goals.
 *
 * @author <a href="dantran@gmail.com">Dan T. Tran</a>
 */
@Mojo( name = "bootstrap", requiresProject = false )
public class BootstrapMojo
    extends CheckoutMojo
{
    /**
     * The goals to run on the clean checkout of a project for the bootstrap goal.
     * If none are specified, then the default goal for the project is executed.
     * Multiple goals should be comma separated.
     */
    @Parameter( property = "goals" )
    private String goals;

    /**
     * A list of profiles to run with the goals.
     * Multiple profiles must be comma separated with no spaces.
     */
    @Parameter( property = "profiles" )
    private String profiles;

    /**
     * The subdirectory (under the project directory) in which to run the goals.
     * The project directory is the same as the checkout directory in most cases,
     * but for some SCMs, it is a subdirectory of the checkout directory.
     */
    @Parameter( property = "goalsDirectory" )
    private String goalsDirectory;

    /**
     * The path where your maven is installed
     */
    @Parameter( property = "mavenHome", defaultValue = "${maven.home}" )
    private File mavenHome;

    //update
    @Parameter( property = "revisionKey", defaultValue = "scm.revision" )
    private String revisionKey;

    @Parameter( defaultValue = "${project}", required = true, readonly = true )
    private MavenProject project;

    @Parameter( property = "skipUpdateIfExists", defaultValue = "false", readonly = false )
    private boolean skipUpdateIfExists;

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        super.execute();
        boolean skipCheckout = true;

        String relativePathProjectDirectory = "";
        if ( this.getCheckoutResult() != null )
        {

            ScmResult checkoutResult = this.getCheckoutResult();

            //At the time of useExport feature is requested only SVN and and CVS have export command implemented
            // we will deal with this as more user using this feature specially clearcase where we need to
            // add relativePathProjectDirectory support to ExportScmResult
            if ( checkoutResult instanceof CheckOutScmResult )
            {
                relativePathProjectDirectory = ( (CheckOutScmResult) checkoutResult ).getRelativePathProjectDirectory();
            }

            runGoals( relativePathProjectDirectory );
        }

        if ( skipCheckout )
        {
            if ( !skipUpdateIfExists )
            {
                executeUpdate();
            }
            runGoals( relativePathProjectDirectory );
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

        if ( this.mavenHome == null )
        {
            cl.setExecutable( "mvn" ); //none windows only
        }
        else
        {
            String mvnPath = this.mavenHome.getAbsolutePath() + "/bin/mvn";
            if ( Os.isFamily( "windows" ) )
            {
                String winMvnPath = mvnPath + ".cmd";
                if ( !new File( winMvnPath ).exists() )
                {
                    winMvnPath = mvnPath + ".bat";
                }
                mvnPath = winMvnPath;
            }
            cl.setExecutable( mvnPath );
        }

        cl.setWorkingDirectory( determineWorkingDirectoryPath( this.getCheckoutDirectory(), //
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

    public void executeUpdate()
        throws MojoExecutionException
    {

        try
        {
            ScmRepository repository = getScmRepository();

            UpdateScmResult result = getScmManager().update( repository, 
                getFileSetBU( this.getCheckoutDirectory(), this.getIncludes(), this.getExcludes() ), 
                getScmVersion( this.getScmVersionType(), this.getScmVersion() ), 
                false );

            checkResult( result );

            if ( result instanceof UpdateScmResultWithRevision )
            {
                String revision = ( (UpdateScmResultWithRevision) result ).getRevision();

                getLog().info( "Storing revision in '" + revisionKey + "' project property." );

                if ( project.getProperties() != null ) // Remove the test when we'll use plugin-test-harness 1.0-alpha-2
                {
                    project.getProperties().put( revisionKey, revision );
                }

                getLog().info( "Project at revision " + revision );
            }
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Cannot run update command : ", e );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run update command : ", e );
        }
    }
}
