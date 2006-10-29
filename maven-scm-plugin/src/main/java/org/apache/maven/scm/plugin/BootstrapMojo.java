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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Properties;

/**
 * Pull the project source from the configured scm and execute the configured goals.
 * 
 * @author <a href="dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 * @goal bootstrap
 * @description Boostrap a project
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
     * @parameter expression="${goals}
     */
    private String goals;

    public void execute()
        throws MojoExecutionException
    {
        checkout();

        runGoals();
    }

    private void runGoals()
        throws MojoExecutionException
    {
        Commandline cl = new Commandline();

        try
        {
            addSystemEnvironment( cl );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Can't add system environment variables to mvn command line.", e );
        }

        cl.addEnvironment( "MAVEN_TERMINATE_CMD", "on" );

        cl.setExecutable( "mvn" );

        cl.setWorkingDirectory( this.getCheckoutDirectory().getPath() );

        if ( this.goals != null )
        {
            String [] tokens = StringUtils.split( this.goals, ", " );

            for ( int i = 0; i < tokens.length; ++i )
            {
                cl.createArgument().setValue( tokens[i] );
            }
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
     * Add system environment variables
     * Moved to plexus-utils 1.0.5
     */
    private void addSystemEnvironment( Commandline cl )
        throws Exception
    {
        Properties envVars = getSystemEnvVars();

        for ( Iterator i = envVars.keySet().iterator(); i.hasNext(); )
        {
            String key = (String) i.next();

            cl.addEnvironment( key, envVars.getProperty( key ) );
        }
    }

    private Properties getSystemEnvVars()
        throws Exception
    {
        Process p = null;

        Properties envVars = new Properties();

        Runtime r = Runtime.getRuntime();

        String os = System.getProperty( "os.name" ).toLowerCase();

        //If this is windows set the shell to command.com or cmd.exe with correct arguments.
        if ( os.indexOf( "windows" ) != -1 )
        {
            if ( os.indexOf( "95" ) != -1 || os.indexOf( "98" ) != -1 || os.indexOf( "Me" ) != -1 )
            {
                p = r.exec( "command.com /c set" );
            }
            else
            {
                p = r.exec( "cmd.exe /c set" );
            }
        }
        else
        {
            p = r.exec( "env" );
        }

        BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );

        String line;

        while ( ( line = br.readLine() ) != null )
        {
            int idx = line.indexOf( '=' );

            String key = line.substring( 0, idx );

            String value = line.substring( idx + 1 );

            envVars.setProperty( key, value );
            // System.out.println( key + " = " + value );
        }

        return envVars;
    }
}
