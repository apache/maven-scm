package org.apache.maven.scm.provider.clearcase.command.checkout;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.ClearCaseCommand;
import org.apache.maven.scm.provider.clearcase.repository.ClearCaseScmProviderRepository;
import org.apache.maven.scm.providers.clearcase.settings.Settings;
import org.apache.maven.scm.providers.clearcase.settings.io.xpp3.ClearcaseXpp3Reader;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 * @author <a href="mailto:frederic.mura@laposte.net">Frederic Mura</a>
 */
public class ClearCaseCheckOutCommand
    extends AbstractCheckOutCommand
    implements ClearCaseCommand
{

    private static Settings settings;

    static
    {
        File scmUserHome = new File( System.getProperty( "user.home" ), ".scm" );
        File settingsFile = new File( scmUserHome, "clearcase-settings.xml" );
        if ( settingsFile.exists() )
        {
            ClearcaseXpp3Reader reader = new ClearcaseXpp3Reader();
            try
            {
                settings = reader.read( new FileReader( settingsFile ) );
            }
            catch ( FileNotFoundException e )
            {
            }
            catch ( IOException e )
            {
            }
            catch ( XmlPullParserException e )
            {
                String message = settingsFile.getAbsolutePath() + " isn't well formed. SKIPPED." + e.getMessage();

                System.out.println( message);
            }
        }
    }

    // ----------------------------------------------------------------------
    // AbstractCheckOutCommand Implementation
    // ----------------------------------------------------------------------

    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                        String tag )
        throws ScmException
    {
        getLogger().debug( "executing checkout command..." );
        ClearCaseScmProviderRepository repo = (ClearCaseScmProviderRepository) repository;
        File workingDirectory = fileSet.getBasedir();
        getLogger().debug( "tag: " + tag );
        if ( isClearCaseLT() )
        {
            getLogger().debug( "Running with CLEARCASE LT" );
        }
        else
        {
            getLogger().debug( "Running with CLEARCASE" );
        }
        // Commandline cl = createCommandLine( fileSet.getBasedir(), tag );

        ClearCaseCheckOutConsumer consumer = new ClearCaseCheckOutConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        Commandline cl;
        try
        {
            // Since clearcase only wants to checkout to a non-existent directory, first delete the working dir if it already exists
            FileUtils.deleteDirectory( workingDirectory );
            // First create the view
            String viewName = getUniqueViewName( repo, workingDirectory.getAbsolutePath() );
            cl = createCreateViewCommandLine( workingDirectory, viewName );
            getLogger().debug( "Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" + cl.toString() );
            exitCode = CommandLineUtils.executeCommandLine( cl, new CommandLineUtils.StringStreamConsumer(), stderr );

            if ( exitCode == 0 )
            {
                File configSpecLocation;

                if ( tag == null )
                {
                    configSpecLocation = repo.getConfigSpec();
                }
                else
                {
                    // TODO We are building on a label
                    throw new UnsupportedOperationException( "Building on a label not supported yet" );
                    // configSpecLocation = new File( "configspec.txt" );
                    // FileWriter writer = new FileWriter( configSpecLocation );
                    // writer.append( "ELEMENT * " + tag );
                    // // If we did not tag the directories leading to the root directory
                    // // of this module, then we need the following line also (otherwise, we will
                    // // not be able to access our module in the given view
                    // writer.append( "element * /main/LATEST" );
                    // writer.close();
                }
                cl = createUpdateConfigSpecCommandLine( workingDirectory, configSpecLocation, viewName );

                getLogger().debug( "Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" + cl.toString() );
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

        return new CheckOutScmResult( cl.toString(), consumer.getCheckedOutFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static Commandline createDeleteViewCommandLine( ClearCaseScmProviderRepository repository,
                                                            File workingDirectory )
    {
        Commandline command = new Commandline();

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        command.setExecutable( "cleartool" );

        command.createArgument().setValue( "rmview" );
        command.createArgument().setValue( "-force" );
        command.createArgument().setValue( "-tag" );
        if ( isClearCaseLT() )
        {
            command.createArgument().setValue( getViewStore() );
        }
        else
        {
            command.createArgument().setValue( getUniqueViewName( repository, workingDirectory.getAbsolutePath() ) );
        }

        return command;
    }

    protected static Commandline createCreateViewCommandLine( File workingDirectory, String viewName )
        throws IOException
    {
        Commandline command = new Commandline();

        // We have to execute from 1 level up from the working dir, since we had to delete the working dir
        command.setWorkingDirectory( workingDirectory.getParentFile().getAbsolutePath() );

        command.setExecutable( "cleartool" );

        command.createArgument().setValue( "mkview" );
        command.createArgument().setValue( "-snapshot" );
        command.createArgument().setValue( "-tag" );
        command.createArgument().setValue( viewName );

        if ( !isClearCaseLT() )
        {
            if ( useVWS() )
            {
                command.createArgument().setValue( "-vws" );
                command.createArgument().setValue( getViewStore() + viewName + ".vws" );
            }
        }

        command.createArgument().setValue( workingDirectory.getCanonicalPath() );

        return command;
    }

    protected static Commandline createUpdateConfigSpecCommandLine( File workingDirectory, File configSpecLocation,
                                                                    String viewName )
    {
        Commandline command = new Commandline();

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        command.setExecutable( "cleartool" );

        command.createArgument().setValue( "setcs" );
        command.createArgument().setValue( "-tag" );
        command.createArgument().setValue( viewName );
        command.createArgument().setValue( configSpecLocation.getAbsolutePath() );

        return command;

    }

    private static String getUniqueViewName( ClearCaseScmProviderRepository repository, String absolutePath )
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

    protected static String getViewStore()
    {
        String result = null;

        if ( settings != null )
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

    /**
     * @return the value of the setting property 'clearcaseLT'
     */
    protected static boolean isClearCaseLT()
    {
        boolean result = false;

        if ( settings != null )
        {
            result = settings.isClearcaseLT();
        }

        return result;
    }

    /**
     * Only use for test case
     * @param isClearCaseLT
     * @deprecated
     */
    protected static void setIsClearCaseLT( boolean isClearCaseLT )
    {
        if ( settings == null )
        {
            settings = new Settings();
        }
        settings.setClearcaseLT( isClearCaseLT );
    }

    /**
     * @return the value of the setting property 'useVWS'
     */
    protected static boolean useVWS()
    {
        boolean result = false;

        if ( settings != null )
        {
            result = settings.isUseVWSParameter();
        }

        return result;
    }

    /**
     * Only use for test case
     * @param useVWS
     * @deprecated
     */
    protected static void setUseVWS( boolean useVWS )
    {
        if ( settings == null )
        {
            settings = new Settings();
        }
        settings.setUseVWSParameter( useVWS );
    }

    private static String getHostName()
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

    private static String getUserName()
    {
        String username;
        username = System.getProperty( "user.name" );
        return username;
    }
}
