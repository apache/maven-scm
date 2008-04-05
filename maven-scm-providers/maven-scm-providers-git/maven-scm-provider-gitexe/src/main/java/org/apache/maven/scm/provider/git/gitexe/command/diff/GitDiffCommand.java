package org.apache.maven.scm.provider.git.gitexe.command.diff;

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
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.command.diff.GitDiffConsumer;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitDiffCommand extends AbstractDiffCommand implements GitCommand
{
    protected DiffScmResult executeDiffCommand( ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion startVersion,
                                                ScmVersion endVersion )
        throws ScmException
    {
        GitDiffConsumer consumer = new GitDiffConsumer( getLogger(), fileSet.getBasedir() );
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        int exitCode;

        Commandline clDiff2Index = createCommandLine( fileSet.getBasedir(), startVersion, endVersion, false );

        exitCode = GitCommandLineUtils.execute( clDiff2Index, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new DiffScmResult( clDiff2Index.toString(), "The git-diff command failed.", stderr.getOutput(), false );
        }

        Commandline clDiff2Head = createCommandLine( fileSet.getBasedir(), startVersion, endVersion, true );

        exitCode = GitCommandLineUtils.execute( clDiff2Head, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new DiffScmResult( clDiff2Head.toString(), "The git-diff command failed.", stderr.getOutput(), false );
        }

        return new DiffScmResult( clDiff2Index.toString(), consumer.getChangedFiles(), consumer.getDifferences(),
                                  consumer.getPatch() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * @param cached if <code>true</code> diff the index to the head, else diff the tree to the index
     */
    public static Commandline createCommandLine( File workingDirectory, ScmVersion startVersion, ScmVersion endVersion, boolean cached )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "diff" );

        if ( cached ) {
        	cl.createArgument().setValue( "--cached" );
        }

        if ( startVersion != null && StringUtils.isNotEmpty( startVersion.getName() ) )
        {
            cl.createArgument().setValue( startVersion.getName() );
        }
        if ( endVersion != null && StringUtils.isNotEmpty( endVersion.getName() ) )
        {
            cl.createArgument().setValue( endVersion.getName() );
        }

        return cl;
    }
}
