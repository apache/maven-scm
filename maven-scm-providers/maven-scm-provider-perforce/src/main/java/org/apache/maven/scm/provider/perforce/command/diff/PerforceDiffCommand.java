package org.apache.maven.scm.provider.perforce.command.diff;

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
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author Mike Perham
 * @version $Id$
 */
public class PerforceDiffCommand
    extends AbstractDiffCommand
    implements PerforceCommand
{
    /** {@inheritDoc} */
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet files, ScmVersion startRev,
                                                ScmVersion endRev )
        throws ScmException
    {
        Commandline cl =
            createCommandLine( (PerforceScmProviderRepository) repo, files.getBasedir(), startRev, endRev );
        PerforceDiffConsumer consumer = new PerforceDiffConsumer();
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + PerforceScmProvider.clean( cl.toString() ) );
        }
        boolean success = false;
        try
        {
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

        return new DiffScmResult( cl.toString(), success ? "Diff successful" : "Unable to diff", consumer
            .getOutput(), success );
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                 ScmVersion startRev, ScmVersion endRev )
    {
        String start = startRev != null && StringUtils.isNotEmpty( startRev.getName() ) ? "@" + startRev.getName() : "";
        String end = endRev != null && StringUtils.isNotEmpty( endRev.getName() ) ? endRev.getName() : "now";

        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        command.createArg().setValue( "diff2" );
        command.createArg().setValue( "-u" );
        // I'm assuming the "revs" are actually labels
        command.createArg().setValue( "..." + start );
        command.createArg().setValue( "...@" + end );
        return command;
    }

}
