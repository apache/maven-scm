package org.apache.maven.scm.provider.git.gitexe.command.remove;

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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitRemoveCommand extends AbstractRemoveCommand implements GitCommand
{
    protected ScmResult executeRemoveCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message )
        throws ScmException
    {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        if ( fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "You must provide at least one file/directory to remove" );
        }

        Commandline cl = createCommandLine( fileSet.getBasedir(), fileSet.getFileList() );
        
        GitRemoveConsumer consumer = new GitRemoveConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        exitCode = GitCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new RemoveScmResult( cl.toString(), "The git command failed.", stderr.getOutput(), false );
        }

        return new RemoveScmResult( cl.toString(), consumer.getRemovedFiles() );
    }

    public static Commandline createCommandLine( File workingDirectory, List/*File*/ files )
    throws ScmException
	{
	    Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "rm" );
	
	    GitCommandLineUtils.addTarget( cl, files );
	
	    return cl;
	}

}
