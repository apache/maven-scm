package org.apache.maven.scm.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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


/**
 * @goal bootstrap
 * @description Boostrap a project
 *
 * @author <a href="dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 * @requiresProject false
 */
public class BootstrapMojo
    extends CheckoutMojo
{
    
    /**
     * The goals to run on the clean checkout of a project for the bootstrap goal. 
     * If none are specified, then the default goal for the project is executed. 
     * Multiple goals should be comma separated. 
     * @parameter expression="${goals}
     */
    
    private String  goals;
    
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

        cl.setExecutable( "mvn" );

        cl.setWorkingDirectory(  this.getCheckoutDirectory().getPath() );

        if ( this.goals != null )
        {
            String [] tokens = StringUtils.split( this.goals, ", " );
            
            for ( int i = 0 ; i < tokens.length ; ++i )
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
    
}
