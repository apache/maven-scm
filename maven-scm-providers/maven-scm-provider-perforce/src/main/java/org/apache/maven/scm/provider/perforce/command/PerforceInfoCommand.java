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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the 'p4 info' command which can be very useful in determining
 * the runtime environment.  Use <code>getEntry(String key)</code> to query
 * the info set for a particular property.  The data from p4 info looks like this:
 * <p/>
 * <pre>
 * User name: mperham
 * Client name: mikeperham-dt
 * Client host: mikeperham-dt
 * Client root: d:\perforce
 * </pre>
 * <p/>
 * where the key is the content before the first colon and the value is the data after
 * the first colon, trimmed.  For example:
 * <code>PerforceInfoCommand.getInfo( this, repo ).getEntry( "User name" )</code>
 * <p/>
 * Note that this is not a traditional SCM command.  This uses the Command class
 * simply because it needs a logger for error handling and the current repository data for
 * command line creation.
 *
 * @author mperham
 * @version $Id: $
 */
public class PerforceInfoCommand
    extends AbstractCommand
    implements PerforceCommand
{
    private static PerforceInfoCommand singleton = null;

    private Map entries = null;

    public static PerforceInfoCommand getInfo( ScmLogger logger, PerforceScmProviderRepository repo )
    {
        return getSingleton( logger, repo );
    }

    public String getEntry( String key )
    {
        return (String) entries.get( key );
    }

    private static synchronized PerforceInfoCommand getSingleton( ScmLogger logger,
                                                                  PerforceScmProviderRepository repo )
    {
        if ( singleton == null )
        {
            PerforceInfoCommand pic = new PerforceInfoCommand();
            if ( logger != null )
            {
                pic.setLogger( logger );
            }
            try
            {
                pic.executeCommand( repo, null, null );
                singleton = pic;
            }
            catch ( ScmException e )
            {
                if ( pic.getLogger().isErrorEnabled() )
                {
                    pic.getLogger().error( "ScmException " + e.getMessage(), e );
                }
            }
        }

        return singleton;
    }

    /** {@inheritDoc} */
    protected ScmResult executeCommand( ScmProviderRepository repo, ScmFileSet scmFileSet,
                                        CommandParameters commandParameters )
        throws ScmException
    {
        if ( !PerforceScmProvider.isLive() )
        {
            return null;
        }

        try
        {
            Commandline command = PerforceScmProvider.createP4Command( (PerforceScmProviderRepository) repo, null );
            command.createArg().setValue( "info" );
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( PerforceScmProvider.clean( "Executing: " + command.toString() ) );
            }
            Process proc = command.execute();
            BufferedReader br = new BufferedReader( new InputStreamReader( proc.getInputStream() ) );
            String line;
            entries = new HashMap();
            while ( ( line = br.readLine() ) != null )
            {
                int idx = line.indexOf( ':' );
                if ( idx == -1 )
                {
                    if ( line.indexOf( "Client unknown." ) == -1 )
                    {
                        throw new IllegalStateException( "Unexpected results from 'p4 info' command: " + line );
                    }

                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( "Cannot find client." );
                    }
                    entries.put( "Client root", "" );
                }
                else
                {
                    String key = line.substring( 0, idx );
                    String value = line.substring( idx + 1 ).trim();
                    entries.put( key, value );
                }
            }
        }
        catch ( CommandLineException e )
        {
            throw new ScmException( e.getLocalizedMessage() );
        }
        catch ( IOException e )
        {
            throw new ScmException( e.getLocalizedMessage() );
        }
        return null;
    }
}
