package org.apache.maven.scm.provider.perforce.command;

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

import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Encapsulates the 'p4 where' command which can be very useful in determining
 * a file's location within the depot.  Use <code>getDepotLocation(String path)</code> to query
 * the depot location for a particular file.  The data from p4 where looks like this:
 * <p/>
 * <pre>
 * p4 where pom.xml
 * //depot/modules/fabric/trunk/pom.xml //mikeperham-dt/depot/modules/fabric/trunk/pom.xml
 * d:\perforce\depot\modules\fabric\trunk\pom.xml
 * </pre>
 *
 * @author mperham
 */
public class PerforceWhereCommand
{
    private ScmLogger logger = null;

    private PerforceScmProviderRepository repo = null;

    public PerforceWhereCommand( ScmLogger log, PerforceScmProviderRepository repos )
    {
        logger = log;
        repo = repos;
    }

    public String getDepotLocation( File file )
    {
        return getDepotLocation( file.getAbsolutePath() );
    }

    /**
     * @param filepath an absolute file path
     * @return the absolute location of the given file within the Perforce repository or null if the file
     *         does not exist in a mapping within the current clientspec.
     */
    public String getDepotLocation( String filepath )
    {
        if ( !PerforceScmProvider.isLive() )
        {
            return null;
        }

        InputStreamReader isReader = null;
        InputStreamReader isReaderErr = null;
        try
        {
            Commandline command = PerforceScmProvider.createP4Command( repo, null );
            command.createArg().setValue( "where" );
            command.createArg().setValue( filepath );
            if ( logger.isDebugEnabled() )
            {
                logger.debug( PerforceScmProvider.clean( "Executing: " + command.toString() ) );
            }
            Process proc = command.execute();
            isReader = new InputStreamReader( proc.getInputStream() );
            isReaderErr = new InputStreamReader( proc.getErrorStream() );
            BufferedReader br = new BufferedReader( isReader );
            BufferedReader brErr = new BufferedReader( isReaderErr );
            String line;
            String path = null;
            while ( ( line = br.readLine() ) != null )
            {
                if ( line.indexOf( "not in client view" ) != -1 )
                {
                    // uh oh, something bad is happening
                    if ( logger.isErrorEnabled() )
                    {
                        logger.error( line );
                    }
                    return null;
                }
                if ( line.indexOf( "is not under" ) != -1 )
                {
                    // uh oh, something bad is happening
                    if ( logger.isErrorEnabled() )
                    {
                        logger.error( line );
                    }
                    return null;
                }

                if ( logger.isDebugEnabled() )
                {
                    logger.debug( line );
                }
                // verify that "//" appears twice in the line
                path = line.substring( 0, line.lastIndexOf( "//" ) - 1 );
            }
            // Check for errors
            while ( ( line = brErr.readLine() ) != null )
            {
                if ( line.indexOf( "not in client view" ) != -1 )
                {
                    // uh oh, something bad is happening
                    if ( logger.isErrorEnabled() )
                    {
                        logger.error( line );
                    }
                    return null;
                }
                if ( line.indexOf( "is not under" ) != -1 )
                {
                    // uh oh, something bad is happening
                    if ( logger.isErrorEnabled() )
                    {
                        logger.error( line );
                    }
                    return null;
                }

                if ( logger.isDebugEnabled() )
                {
                    logger.debug( line );
                }
            }

            return path;
        }
        catch ( CommandLineException e )
        {
            if ( logger.isErrorEnabled() )
            {
                logger.error( e );
            }
            throw new RuntimeException( e.getLocalizedMessage() );
        }
        catch ( IOException e )
        {
            if ( logger.isErrorEnabled() )
            {
                logger.error( e );
            }
            throw new RuntimeException( e.getLocalizedMessage() );
        }
        finally
        {
            IOUtil.close( isReader );
            IOUtil.close( isReaderErr );
        }
    }
}
