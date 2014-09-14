package org.apache.maven.scm.provider.git.gitexe.command.blame;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.blame.AbstractBlameCommand;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author Evgeny Mandrikov
 * @author Olivier Lamy
 * @since 1.4
 */
public class GitBlameCommand
    extends AbstractBlameCommand
    implements GitCommand
{

    @Override
    public boolean requiresToWorkInRepoRootDir()
    {
        return true;
    }

    @Override
    protected ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet workingDirectory,
                                        CommandParameters parameters )
        throws ScmException
    {
        String filename = parameters.getString( CommandParameter.FILE );
        Commandline cl =
            createCommandLine( workingDirectory.getBasedir(), filename,
                               parameters.getBoolean( CommandParameter.IGNORE_WHITESPACE, false ) );
        GitBlameConsumer consumer = new GitBlameConsumer( getLogger() );
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode = GitCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new BlameScmResult( cl.toString(), "The git blame command failed.", stderr.getOutput(), false );
        }
        return new BlameScmResult( cl.toString(), consumer.getLines() );
    }

    /**
     * {@inheritDoc}
     */
    public BlameScmResult executeBlameCommand( ScmProviderRepository repo, ScmFileSet workingDirectory,
                                               String filename )
        throws ScmException
    {
        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.FILE, filename );
        commandParameters.setString( CommandParameter.IGNORE_WHITESPACE, Boolean.FALSE.toString() );
        return (BlameScmResult) execute( repo, workingDirectory, commandParameters );
    }

    protected static Commandline createCommandLine( File workingDirectory, String filename, boolean ignoreWhitespace )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "blame" );
        cl.createArg().setValue( "--porcelain" );
        cl.createArg().setValue( filename );
        if ( ignoreWhitespace )
        {
            cl.createArg().setValue( "-w" );
        }
        return cl;
    }
}
