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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
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
    /**
     * {@inheritDoc}
     */
    public BlameScmResult executeBlameCommand( ScmProviderRepository repo, ScmFileSet workingDirectory,
                                               String filename )
        throws ScmException
    {
        Commandline cl = createCommandLine( workingDirectory.getBasedir(), filename );
        GitBlameConsumer consumer = new GitBlameConsumer( getLogger() );
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode = GitCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            throw new UnsupportedOperationException();
        }
        return new BlameScmResult( cl.toString(), consumer.getLines() );
    }

    public static Commandline createCommandLine( File workingDirectory, String filename )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "blame" );
        cl.createArg().setValue( "-c" );
        cl.createArg().setValue( "-l" );
        cl.createArg().setValue( filename );
        return cl;
    }
}
