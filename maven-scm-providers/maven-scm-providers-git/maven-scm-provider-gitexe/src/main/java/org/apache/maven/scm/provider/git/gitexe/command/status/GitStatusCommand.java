package org.apache.maven.scm.provider.git.gitexe.command.status;

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

import java.net.URI;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class GitStatusCommand
    extends AbstractStatusCommand
    implements GitCommand
{
    /** {@inheritDoc} */
    protected StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        Commandline clRevparse = createRevparseShowToplevelCommand( fileSet );

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        URI relativeRepositoryPath = null;
        
        int exitCode;

        exitCode = GitCommandLineUtils.execute( clRevparse, stdout, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            // git-status returns non-zero if nothing to do
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "Could not resolve toplevel" );
            }
        }
        else
        {
            relativeRepositoryPath =
                GitStatusConsumer.resolveURI( stdout.getOutput().trim(), fileSet.getBasedir().toURI() );
        }

        Commandline cl = createCommandLine( (GitScmProviderRepository) repo, fileSet );

        GitStatusConsumer consumer = new GitStatusConsumer( getLogger(), fileSet.getBasedir(), relativeRepositoryPath );

        stderr = new CommandLineUtils.StringStreamConsumer();

        exitCode = GitCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            // git-status returns non-zero if nothing to do
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "nothing added to commit but untracked files present (use \"git add\" to track)" );
            }
        }

        return new StatusScmResult( cl.toString(), consumer.getChangedFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( GitScmProviderRepository repository, ScmFileSet fileSet )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( fileSet.getBasedir(), "status" );
        cl.addArguments( new String[] { "--porcelain", "." } );
        return cl;
    }
    
    public static Commandline createRevparseShowToplevelCommand( ScmFileSet fileSet )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( fileSet.getBasedir(), "rev-parse" );
        cl.addArguments( new String[] { "--show-toplevel" } );
        return cl;
    }
}
