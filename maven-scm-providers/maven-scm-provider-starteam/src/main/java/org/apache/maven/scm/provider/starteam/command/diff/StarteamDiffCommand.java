package org.apache.maven.scm.provider.starteam.command.diff;

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
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 *
 */
public class StarteamDiffCommand
    extends AbstractDiffCommand
    implements StarteamCommand
{
    // ----------------------------------------------------------------------
    // AbstractDiffCommand Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion startVersion,
                                                ScmVersion endVersion )
        throws ScmException
    {
        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );
        }

        if ( fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "This provider doesn't support diff command on a subsets of a directory" );
        }

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        StarteamDiffConsumer consumer = new StarteamDiffConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        Commandline cl = createCommandLine( repository, fileSet, startVersion, endVersion );

        int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

        if ( exitCode != 0 )
        {
            return new DiffScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(), false );
        }

        return new DiffScmResult( cl.toString(), consumer.getChangedFiles(), consumer.getDifferences(),
                                  consumer.getPatch() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( StarteamScmProviderRepository repo, ScmFileSet workingDirectory,
                                                 ScmVersion startLabel, ScmVersion endLabel )
        throws ScmException
    {

        List<String> args = new ArrayList<String>();

        args.add( "-filter" );
        args.add( "M" );

        if ( startLabel != null && StringUtils.isNotEmpty( startLabel.getName() ) )
        {
            args.add( "-vl" );

            args.add( startLabel.getName() );
        }

        if ( endLabel != null && StringUtils.isNotEmpty( endLabel.getName() ) )
        {
            args.add( "-vl" );

            args.add( endLabel.getName() );
        }

        if ( endLabel != null && ( startLabel == null || StringUtils.isEmpty( startLabel.getName() ) ) )
        {
            throw new ScmException( "Missing start label." );
        }

        StarteamCommandLineUtils.addEOLOption( args );

        return StarteamCommandLineUtils.createStarteamCommandLine( "diff", args, workingDirectory, repo );
    }
}
