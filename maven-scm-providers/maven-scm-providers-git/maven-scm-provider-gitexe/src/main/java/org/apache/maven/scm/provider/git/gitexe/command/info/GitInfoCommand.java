package org.apache.maven.scm.provider.git.gitexe.command.info;

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
import org.apache.maven.scm.command.AbstractCommand;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author Olivier Lamy
 * @since 1.5
 */
public class GitInfoCommand
    extends AbstractCommand
    implements GitCommand
{

    public static final int NO_REVISION_LENGTH = -1;

    @Override
    protected ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {

        GitInfoConsumer consumer = new GitInfoConsumer( getLogger(), fileSet );
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        Commandline cli = createCommandLine( repository, fileSet, parameters );

        int exitCode = GitCommandLineUtils.execute( cli, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new InfoScmResult( cli.toString(), "The git rev-parse command failed.", stderr.getOutput(), false );
        }
        return new InfoScmResult( cli.toString(), consumer.getInfoItems() );
    }

    public static Commandline createCommandLine( ScmProviderRepository repository, ScmFileSet fileSet,
                                                 CommandParameters parameters )
        throws ScmException
    {
        Commandline cli = GitCommandLineUtils.getBaseGitCommandLine( fileSet.getBasedir(), "rev-parse" );
        cli.createArg().setValue( "--verify" );
        final int revLength = getRevisionLength( parameters );
        if ( revLength > NO_REVISION_LENGTH )// set the --short key only if revision length parameter is passed and
                                             // different from -1
        {
            cli.createArg().setValue( "--short=" + revLength );
        }
        cli.createArg().setValue( "HEAD" );

        return cli;
    }

    /**
     * Get the revision length from the parameters
     *
     * @param parameters
     * @return -1 if parameter {@link CommandParameter.SCM_SHORT_REVISION_LENGTH} is absent, <br>
     *         and otherwise - the length to be applied for the revision formatting
     * @throws ScmException
     * @since 1.7
     */
    private static int getRevisionLength( final CommandParameters parameters )
        throws ScmException
    {
        if ( parameters == null )
        {
            return NO_REVISION_LENGTH;
        }
        else
        {
            return parameters.getInt( CommandParameter.SCM_SHORT_REVISION_LENGTH, NO_REVISION_LENGTH );
        }
    }


}
