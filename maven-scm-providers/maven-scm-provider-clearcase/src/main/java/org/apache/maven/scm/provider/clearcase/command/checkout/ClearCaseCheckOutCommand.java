package org.apache.maven.scm.provider.clearcase.command.checkout;

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
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.ClearCaseCommand;
import org.apache.maven.scm.provider.clearcase.repository.ClearCaseScmProviderRepository;
import org.apache.maven.scm.providers.clearcase.settings.Settings;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 * @author <a href="mailto:frederic.mura@laposte.net">Frederic Mura</a>
 *
 */
public class ClearCaseCheckOutCommand
    extends AbstractCheckOutCommand
    implements ClearCaseCommand
{
    private Settings settings = null;

    // ----------------------------------------------------------------------
    // AbstractCheckOutCommand Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                       ScmVersion version, boolean recursive, boolean shallow )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing checkout command..." );
        }
        ClearCaseScmProviderRepository repo = (ClearCaseScmProviderRepository) repository;
        File workingDirectory = fileSet.getBasedir();

        if ( version != null && getLogger().isDebugEnabled() )
        {
            getLogger().debug( version.getType() + ": " + version.getName() );
        }

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "Running with CLEARCASE " + settings.getClearcaseType() );
        }

        ClearCaseCheckOutConsumer consumer = new ClearCaseCheckOutConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        Commandline cl;
        String projectDirectory = "";

        try
        {
            // Since clearcase only wants to checkout to a non-existent directory, first delete the working dir
            // if it already exists
            FileUtils.deleteDirectory( workingDirectory );
            // First create the view
            String viewName = getUniqueViewName( repo, workingDirectory.getAbsolutePath() );
            String streamIdentifier = getStreamIdentifier( repo.getStreamName(), repo.getVobName() );
            cl = createCreateViewCommandLine( workingDirectory, viewName, streamIdentifier );
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" + cl.toString() );
            }
            exitCode =
                CommandLineUtils.executeCommandLine( cl, new CommandLineUtils.StringStreamConsumer(), stderr );

            if ( exitCode == 0 )
            {
                File configSpecLocation;

                if ( !repo.isAutoConfigSpec() )
                {
                    configSpecLocation = repo.getConfigSpec();
                    if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
                    {
                        // Another config spec is needed in this case.
                        //
                        // One option how to implement this would be to use a name convention for the config specs,
                        // e.g. the tag name could be appended to the original config spec name.
                        // If the config spec from the SCM URL would be \\myserver\configspecs\someproj.txt
                        // and the tag name would be mytag, the new config spec location could be
                        // \\myserver\configspecs\someproj-mytag.txt
                        //
                        throw new UnsupportedOperationException(
                            "Building on a label not supported with user-specified config specs" );
                    }
                }
                else
                {

                    // write config spec to temp file
                    String configSpec;
                    if ( !repo.hasElements() )
                    {
                        configSpec = createConfigSpec( repo.getLoadDirectory(), version );
                    }
                    else
                    {
                        configSpec = createConfigSpec( repo.getLoadDirectory(), repo.getElementName(), version );
                    }
                    if ( getLogger().isInfoEnabled() )
                    {
                        getLogger().info( "Created config spec for view '" + viewName + "':\n" + configSpec );
                    }
                    configSpecLocation = writeTemporaryConfigSpecFile( configSpec, viewName );

                    // When checking out from ClearCase, the directory structure of the
                    // SCM system is repeated within the checkout directory. E.g. if you check out the
                    // project "my/project" to "/some/dir", the project sources are actually checked out
                    // to "my/project/some/dir".
                    projectDirectory = repo.getLoadDirectory();
                    // strip off leading / to make the path relative
                    if ( projectDirectory.startsWith( "/" ) )
                    {
                        projectDirectory = projectDirectory.substring( 1 );
                    }
                }

                cl = createUpdateConfigSpecCommandLine( workingDirectory, configSpecLocation, viewName );

                if ( getLogger().isInfoEnabled() )
                {
                    getLogger().info( "Executing: " + cl.getWorkingDirectory().getAbsolutePath()
                                      + ">>" + cl.toString() );
                }
                exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );

            }
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing clearcase command.", ex );
        }
        catch ( IOException ex )
        {
            throw new ScmException( "Error while deleting working directory.", ex );
        }

        if ( exitCode != 0 )
        {
            return new CheckOutScmResult( cl.toString(), "The cleartool command failed.", stderr.getOutput(), false );
        }

        return new CheckOutScmResult( cl.toString(), consumer.getCheckedOutFiles(), projectDirectory );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * Creates a temporary config spec file with the given contents that will be
     * deleted on VM exit.
     *
     * @param configSpecContents The contents for the file
     * @param viewName           The name of the view; used to determine an appropriate file
     *                           name
     * @throws IOException
     */
    protected File writeTemporaryConfigSpecFile( String configSpecContents, String viewName )
        throws IOException
    {
        File configSpecLocation = File.createTempFile( "configspec-" + viewName, ".txt" );
        FileWriter fw = new FileWriter( configSpecLocation );
        try
        {
            fw.write( configSpecContents );
        }
        finally
        {
            try
            {
                fw.close();
            }
            catch ( IOException e )
            {
                // ignore
            }
        }
        configSpecLocation.deleteOnExit();
        return configSpecLocation;
    }

    /**
     * Creates a config spec that loads the given loadDirectory and uses the
     * given version tag
     *
     * @param loadDirectory the VOB directory to be loaded
     * @param version       ClearCase label type; notice that branch types are not
     *                      supported
     * @return Config Spec as String
     */
    protected String createConfigSpec( String loadDirectory, ScmVersion version )
    {
        // create config spec
        StringBuilder configSpec = new StringBuilder();
        configSpec.append( "element * CHECKEDOUT\n" );
        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            configSpec.append( "element * " + version.getName() + "\n" );
            configSpec.append( "element -directory * /main/LATEST\n" );
            // configSpec.append( "element * /main/QualityControl_INT/RAD7_Migration/LATEST\n" );
        }
        else
        {
            configSpec.append( "element * /main/LATEST\n" );
        }
        configSpec.append( "load " + loadDirectory + "\n" );
        return configSpec.toString();
    }

    protected String createConfigSpec( String loadDirectory, String elementName, ScmVersion version )
    {
        // create config spec
        StringBuilder configSpec = new StringBuilder();
        configSpec.append( "element * CHECKEDOUT\n" );
        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            configSpec.append( "element * " + version.getName() + "\n" );
            configSpec.append( "element * " + elementName + "\n" );
        }
        else
        {
            configSpec.append( "element * /main/LATEST\n" );
        }
        configSpec.append( "load " + loadDirectory + "\n" );
        return configSpec.toString();
    }

//    private static Commandline createDeleteViewCommandLine( ClearCaseScmProviderRepository repository,
//                                                            File workingDirectory )
//    {
//        Commandline command = new Commandline();
//
//        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );
//
//        command.setExecutable( "cleartool" );
//
//        command.createArg().setValue( "rmview" );
//        command.createArg().setValue( "-force" );
//        command.createArg().setValue( "-tag" );
//        if ( isClearCaseLT() )
//        {
//            command.createArg().setValue( getViewStore() );
//        }
//        else
//        {
//            command.createArg().setValue( getUniqueViewName( repository, workingDirectory.getAbsolutePath() ) );
//        }
//
//        return command;
//    }

    protected Commandline createCreateViewCommandLine( File workingDirectory, String viewName, String streamIdentifier )
        throws IOException
    {
        Commandline command = new Commandline();

        // We have to execute from 1 level up from the working dir, since we had to delete the working dir
        command.setWorkingDirectory( workingDirectory.getParentFile().getAbsolutePath() );

        command.setExecutable( "cleartool" );

        command.createArg().setValue( "mkview" );
        command.createArg().setValue( "-snapshot" );
        command.createArg().setValue( "-tag" );
        command.createArg().setValue( viewName );

        if ( isClearCaseUCM() )
        {
            command.createArg().setValue( "-stream" );
            command.createArg().setValue( streamIdentifier );
        }

        if ( !isClearCaseLT() )
        {
            if ( useVWS() )
            {
                command.createArg().setValue( "-vws" );
                command.createArg().setValue( getViewStore() + viewName + ".vws" );
            }
        }

        command.createArg().setValue( workingDirectory.getCanonicalPath() );

        return command;
    }

    /**
     * Format the stream identifier for ClearCaseUCM
     * @param streamName
     * @param vobName
     * @return the formatted stream identifier if the two parameter are not null
     */
    protected String getStreamIdentifier( String streamName, String vobName )
    {
        if ( streamName == null || vobName == null )
        {
            return null;
        }
        return "stream:" + streamName + "@" + vobName;
    }

    protected Commandline createUpdateConfigSpecCommandLine( File workingDirectory, File configSpecLocation,
                                                                    String viewName )
    {
        Commandline command = new Commandline();

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        command.setExecutable( "cleartool" );

        command.createArg().setValue( "setcs" );
        command.createArg().setValue( "-tag" );
        command.createArg().setValue( viewName );
        command.createArg().setValue( configSpecLocation.getAbsolutePath() );

        return command;

    }

    private String getUniqueViewName( ClearCaseScmProviderRepository repository, String absolutePath )
    {
        String uniqueId;
        int lastIndexBack = absolutePath.lastIndexOf( '\\' );
        int lastIndexForward = absolutePath.lastIndexOf( '/' );
        if ( lastIndexBack != -1 )
        {
            uniqueId = absolutePath.substring( lastIndexBack + 1 );
        }
        else
        {
            uniqueId = absolutePath.substring( lastIndexForward + 1 );
        }
        return repository.getViewName( uniqueId );
    }

    protected String getViewStore()
    {
        String result = null;

        if ( settings.getViewstore() != null )
        {
            result = settings.getViewstore();
        }

        if ( result == null )
        {
            result = "\\\\" + getHostName() + "\\viewstore\\";
        }
        else
        {
            // If ClearCase LT are use, the View store is identify by the
            // username.
            if ( isClearCaseLT() )
            {
                result = result + getUserName() + "\\";
            }
        }
        return result;
    }

    protected boolean isClearCaseLT()
    {
        return ClearCaseScmProviderRepository.CLEARCASE_LT.equals( settings.getClearcaseType() );
    }

    protected boolean isClearCaseUCM()
    {
        return ClearCaseScmProviderRepository.CLEARCASE_UCM.equals( settings.getClearcaseType() );
    }

    /**
     * @return the value of the setting property 'useVWS'
     */
    protected boolean useVWS()
    {
        return settings.isUseVWSParameter();
    }

    private String getHostName()
    {
        String hostname;
        try
        {
            hostname = InetAddress.getLocalHost().getHostName();
        }
        catch ( UnknownHostException e )
        {
            // Should never happen
            throw new RuntimeException( e );
        }
        return hostname;
    }

    private String getUserName()
    {
        String username;
        username = System.getProperty( "user.name" );
        return username;
    }

    public void setSettings( Settings settings )
    {
        this.settings = settings;
    }
}
