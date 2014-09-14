package org.apache.maven.scm.provider.git.gitexe.command.remoteinfo;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.remoteinfo.AbstractRemoteInfoCommand;
import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author Bertrand Paquet
 */
public class GitRemoteInfoCommand
    extends AbstractRemoteInfoCommand
    implements GitCommand
{

    @Override
    public boolean requiresToWorkInRepoRootDir()
    {
        return true;
    }

    @Override
    public RemoteInfoScmResult executeRemoteInfoCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                         CommandParameters parameters )
        throws ScmException
    {
        GitScmProviderRepository gitRepository = (GitScmProviderRepository) repository;

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        Commandline clLsRemote = createCommandLine( gitRepository );

        GitRemoteInfoConsumer consumer = new GitRemoteInfoConsumer( getLogger(), clLsRemote.toString() );

        int exitCode = GitCommandLineUtils.execute( clLsRemote, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            throw new ScmException( "unbale to execute ls-remote on " + gitRepository.getFetchUrl() );
        }

        return consumer.getRemoteInfoScmResult();
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( GitScmProviderRepository repository )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( null, "ls-remote" );

        cl.setWorkingDirectory( System.getProperty( "java.io.tmpdir" ) );

        String remoteUrl = repository.getPushUrl();
        cl.createArg().setValue( remoteUrl );

        return cl;
    }

}
