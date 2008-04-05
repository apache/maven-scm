package org.apache.maven.scm.provider.git.gitexe.command.list;

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
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.gitexe.command.list.GitListConsumer;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitListCommand extends AbstractListCommand implements GitCommand
{

    protected ListScmResult executeListCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                boolean recursive, ScmVersion scmVersion )
        throws ScmException
    {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        if ( GitScmProviderRepository.PROTOCOL_FILE.equals( repository.getProtocol() ) &&
             repository.getUrl().indexOf( fileSet.getBasedir().getPath() ) >= 0 ) 
        {
            throw new ScmException( "remote repository must not be the working directory" );
        }

        int exitCode;

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        GitListConsumer consumer = new GitListConsumer( getLogger()
        		                                      , fileSet.getBasedir().getParentFile()
        		                                      , ScmFileStatus.CHECKED_IN );
        
        Commandline cl = createCommandLine( repository, fileSet.getBasedir() );

        exitCode = GitCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new ListScmResult( cl.toString(), "The git-ls-files command failed.", stderr.getOutput(), false );
        }

        return new ListScmResult( cl.toString(), consumer.getListedFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( GitScmProviderRepository repository, File workingDirectory )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "ls-files" );
        
        return cl;
    }
    
}
