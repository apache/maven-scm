package org.apache.maven.scm.provider.perforce.command.status;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.command.PerforceVerbMapper;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mike Perham
 *
 */
public class PerforceStatusCommand
    extends AbstractStatusCommand
    implements PerforceCommand
{
    private String actualLocation;

    /** {@inheritDoc} */
    protected StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet files )
        throws ScmException
    {
        PerforceScmProviderRepository prepo = (PerforceScmProviderRepository) repo;
        actualLocation = PerforceScmProvider.getRepoPath( getLogger(), prepo, files.getBasedir() );
        PerforceStatusConsumer consumer = new PerforceStatusConsumer();
        Commandline command = readOpened( prepo, files, consumer );

        if ( consumer.isSuccess() )
        {
            List<ScmFile> scmfiles = createResults( actualLocation, consumer );
            return new StatusScmResult( command.toString(), scmfiles );
        }

        return new StatusScmResult( command.toString(), "Unable to get status", consumer
                .getOutput(), consumer.isSuccess() );
    }

    public static List<ScmFile> createResults( String repoPath, PerforceStatusConsumer consumer )
    {
        List<ScmFile> results = new ArrayList<ScmFile>();
        List<String> files = consumer.getDepotfiles();
        Pattern re = Pattern.compile( "([^#]+)#\\d+ - ([^ ]+) .*" );
        for ( Iterator<String> it = files.iterator(); it.hasNext(); )
        {
            String filepath = it.next();
            Matcher matcher = re.matcher( filepath );
            if ( !matcher.matches() )
            {
                System.err.println( "Skipping " + filepath );
                continue;
            }
            String path = matcher.group( 1 );
            String verb = matcher.group( 2 );

            ScmFile scmfile = new ScmFile( path.substring( repoPath.length() + 1 ).trim(), PerforceVerbMapper
                .toStatus( verb ) );
            results.add( scmfile );
        }
        return results;
    }

    private Commandline readOpened( PerforceScmProviderRepository prepo, ScmFileSet files,
                                    PerforceStatusConsumer consumer )
    {
        Commandline cl = createOpenedCommandLine( prepo, files.getBasedir(), actualLocation );
        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( PerforceScmProvider.clean( "Executing " + cl.toString() ) );
            }

            CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
            int exitCode = CommandLineUtils.executeCommandLine( cl, consumer, err );

            if ( exitCode != 0 )
            {
                String cmdLine = CommandLineUtils.toString( cl.getCommandline() );

                StringBuilder msg = new StringBuilder( "Exit code: " + exitCode + " - " + err.getOutput() );
                msg.append( '\n' );
                msg.append( "Command line was:" + cmdLine );

                throw new CommandLineException( msg.toString() );
            }
        }
        catch ( CommandLineException e )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "CommandLineException " + e.getMessage(), e );
            }
        }

        return cl;
    }

    public static Commandline createOpenedCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                       String location )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );
        command.createArg().setValue( "opened" );
        command.createArg().setValue( PerforceScmProvider.getCanonicalRepoPath( location ) );
        return command;
    }
}
