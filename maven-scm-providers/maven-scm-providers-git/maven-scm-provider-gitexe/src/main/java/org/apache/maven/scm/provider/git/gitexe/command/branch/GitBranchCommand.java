package org.apache.maven.scm.provider.git.gitexe.command.branch;

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
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.branch.AbstractBranchCommand;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.gitexe.command.list.GitListCommand;
import org.apache.maven.scm.provider.git.gitexe.command.list.GitListConsumer;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitBranchCommand extends AbstractBranchCommand implements GitCommand
{
    public ScmResult executeBranchCommand( ScmProviderRepository repo, ScmFileSet fileSet, String branch,
                                           String message )
        throws ScmException
    {
        if ( branch == null || StringUtils.isEmpty( branch.trim() ) )
        {
            throw new ScmException( "branch name must be specified" );
        }

        if ( !fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "This provider doesn't support branching subsets of a directory" );
        }

        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        Commandline cl = createCommandLine( repository, fileSet.getBasedir(), branch );

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        int exitCode;

        exitCode = GitCommandLineUtils.execute( cl, stdout, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new BranchScmResult( cl.toString(), "The git-branch command failed.", stderr.getOutput(), false );
        }
        
        // and now push the branch to the origin repository
        Commandline clPush = createPushCommandLine( repository, fileSet, branch );
        
        exitCode = GitCommandLineUtils.execute( clPush, stdout, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new BranchScmResult( clPush.toString(), "The git-push command failed.", stderr.getOutput(), false );
        }

        // as last action we search for the branched files
        GitListConsumer listConsumer = new GitListConsumer( getLogger()
        		                                          , fileSet.getBasedir()
        		                                          , ScmFileStatus.TAGGED);

        Commandline clList = GitListCommand.createCommandLine( repository, fileSet.getBasedir() );
        
        exitCode = GitCommandLineUtils.execute( clList, listConsumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new BranchScmResult( clList.toString(), "The git-ls-files command failed.", stderr.getOutput(), false );
        }

        return new BranchScmResult( cl.toString(), listConsumer.getListedFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( GitScmProviderRepository repository, File workingDirectory,
                                                 String branch )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "branch" );

        cl.createArgument().setValue( branch );

        return cl;
    }
    
    public static Commandline createPushCommandLine( GitScmProviderRepository repository, ScmFileSet fileSet,
                                                     String branch )
	throws ScmException
	{
		Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( fileSet.getBasedir(), "push");
		
		cl.createArgument().setValue( "origin" );
		cl.createArgument().setValue( branch );
		
		return cl;
	}

}
