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

import java.util.Iterator;
import java.util.List;

/**
 * Validate scm connection string
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal validate
 * @execute phase="validate"
 */
public class ValidateMojo
    extends AbstractScmMojo
{
    /**
     * @parameter expression=${project.scm.connection}
     * @readonly
     */
    private String scmConnection;

    /**
     * @parameter expression=${project.scm.developerConnection}
     * @readonly
     */
    private String scmDeveloperConnection;

    public void execute()
        throws MojoExecutionException
    {
        //check connectionUrl provided with cli
        try
        {
            validateConnection( getConnectionUrl(), "connectionUrl" );
        }
        catch ( NullPointerException e )
        {
            // nothing to do. connectionUrl isn't define
        }

        //check scm connection
        if ( scmConnection != null )
        {
            validateConnection( scmConnection, "project.scm.connection" );
        }

        // Check scm developerConnection
        if ( scmConnection != null )
        {
            validateConnection( scmDeveloperConnection, "project.scm.developerConnection" );
        }

    }

    private void validateConnection( String connectionString, String type )
        throws MojoExecutionException
    {
        List messages = getScmManager().validateScmRepository( connectionString );

        if ( !messages.isEmpty() )
        {
            getLog().error( "Error scm url connection (" + type + ") validation failed :" );

            Iterator iter = messages.iterator();

            while ( iter.hasNext() )
            {
                getLog().error( iter.next().toString() );
            }

            throw new MojoExecutionException( "Command failed." );
        }
        else
        {
            getLog().info( type + " scm connection string is valid." );
        }
    }
}
