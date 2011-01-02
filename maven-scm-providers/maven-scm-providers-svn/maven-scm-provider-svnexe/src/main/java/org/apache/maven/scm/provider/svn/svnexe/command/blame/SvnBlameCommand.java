package org.apache.maven.scm.provider.svn.svnexe.command.blame;

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
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author Evgeny Mandrikov
 * @author Olivier Lamy
 * @since 1.4
 */
public class SvnBlameCommand
    extends AbstractBlameCommand
    implements SvnCommand
{
    /**
     * {@inheritDoc}
     */
    public BlameScmResult executeBlameCommand( ScmProviderRepository repo, ScmFileSet workingDirectory,
                                               String filename )
        throws ScmException
    {
        Commandline cl = createCommandLine( (SvnScmProviderRepository) repo, workingDirectory.getBasedir(), filename );

        SvnBlameConsumer consumer = new SvnBlameConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + SvnCommandLineUtils.cryptPassword( cl ) );
            getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
        }

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
            return new BlameScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new BlameScmResult( cl.toString(), consumer.getLines() );
    }

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 String filename )
    {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory, repository );
        cl.createArg().setValue( "blame" );
        cl.createArg().setValue( "--xml" );
        cl.createArg().setValue( filename );
        return cl;
    }
}
