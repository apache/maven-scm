package org.apache.maven.scm.provider.starteam.command.diff;

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

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.starteam.command.StarteamCommand;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;

import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.CommandLineUtils;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version $Id$
 */
public class StarteamDiffCommand
    extends AbstractDiffCommand
    implements StarteamCommand
{
    // ----------------------------------------------------------------------
    // AbstractDiffCommand Implementation
    // ----------------------------------------------------------------------
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet, String startRevision,
                                                String endRevision )
        throws ScmException
    {

        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );

        if ( fileSet.getFiles().length != 0 )
        {
            throw new ScmException( "This provider doesn't support diff command on a subsets of a directory" );
        }

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo;

        StarteamDiffConsumer consumer = new StarteamDiffConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        Commandline cl = createCommandLine( repository, fileSet.getBasedir(), startRevision, endRevision );

        int exitCode = StarteamCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

        if ( exitCode != 0 )
        {
            return new DiffScmResult( cl.toString(), "The starteam command failed.", stderr.getOutput(), false );
        }

        return new DiffScmResult( cl.toString(), consumer.getChangedFiles(), consumer.getDifferences(), consumer.getPatch() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( StarteamScmProviderRepository repo, File workingDirectory,
                                                 String startLabel, String endLabel )
        throws ScmException
    {
        Commandline cl = StarteamCommandLineUtils.createStarteamBaseCommandLine( "diff", workingDirectory, repo );

        cl.createArgument().setValue( "-is" );

        cl.createArgument().setValue( "-filter" );

        cl.createArgument().setValue( "M" );

        if ( startLabel != null && startLabel.length() != 0 )
        {
            cl.createArgument().setValue( "-vl" );

            cl.createArgument().setValue( startLabel );
        }

        if ( endLabel != null && endLabel.length() != 0 )
        {
            cl.createArgument().setValue( "-vl" );

            cl.createArgument().setValue( endLabel );
        }

        if ( endLabel != null && ( startLabel == null || startLabel.length() == 0 ) )
        {
            throw new ScmException( "Missing start label." );
        }

        return cl;
    }
}
