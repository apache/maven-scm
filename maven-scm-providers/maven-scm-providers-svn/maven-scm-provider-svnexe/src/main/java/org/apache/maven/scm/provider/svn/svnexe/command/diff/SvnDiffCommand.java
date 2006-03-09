package org.apache.maven.scm.provider.svn.svnexe.command.diff;

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
import org.apache.maven.scm.command.diff.AbstractDiffCommand;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.command.diff.SvnDiffConsumer;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SvnDiffCommand
    extends AbstractDiffCommand
    implements SvnCommand
{
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet, String startRevision,
                                                String endRevision )
        throws ScmException
    {
        Commandline cl =
            createCommandLine( (SvnScmProviderRepository) repo, fileSet.getBasedir(), startRevision, endRevision );

        SvnDiffConsumer consumer = new SvnDiffConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Executing: " + SvnCommandLineUtils.cryptPassword( cl ) );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        int exitCode;

        try
        {
            exitCode = SvnCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new DiffScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new DiffScmResult( cl.toString(), consumer.getChangedFiles(), consumer.getDifferences(),
                                  consumer.getPatch() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 String startRevision, String endRevision )
    {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory, repository );

        cl.createArgument().setValue( "diff" );

        if ( startRevision != null )
        {
            cl.createArgument().setValue( "-r" );

            if ( endRevision != null )
            {
                cl.createArgument().setValue( startRevision + ":" + endRevision );
            }
            else
            {
                cl.createArgument().setValue( startRevision );
            }
        }

        return cl;
    }
}
