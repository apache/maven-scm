package org.apache.maven.scm.provider.git.gitexe.command.checkout;

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
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.gitexe.command.list.GitListCommand;
import org.apache.maven.scm.provider.git.gitexe.command.list.GitListConsumer;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitCheckOutCommand extends AbstractCheckOutCommand implements GitCommand
{
    /**
     * For git, the given repository is a remote one.
     * We have to clone it first if the working directory does not contain a git repo yet,
     * otherwise we have to git-pull it.
     * 
     * TODO We currently assume a '.git' directory, so this does not work for --bare repos
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                        ScmVersion version )
        throws ScmException
    {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        if ( GitScmProviderRepository.PROTOCOL_FILE.equals( repository.getProtocol() ) &&
             repository.getUrl().indexOf( fileSet.getBasedir().getPath() ) >= 0 ) 
        {
            throw new ScmException( "remote repository must not be the working directory" );
        }

        int exitCode;

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        

        if ( !fileSet.getBasedir().exists() || 
             !( new File( fileSet.getBasedir(), ".git" ).exists() ) )
        {
            if ( fileSet.getBasedir().exists() ) 
            {
                // git refuses to clone otherwise
                fileSet.getBasedir().delete();
            }
            
            // no git repo seems to exist, let's clone the original repo
            Commandline clClone = createCloneCommand( repository, fileSet.getBasedir() );
            
            exitCode = GitCommandLineUtils.execute( clClone, stdout, stderr, getLogger() );
            if ( exitCode != 0 )
            {
                return new CheckOutScmResult( clClone.toString(), "The git-clone command failed.", stderr.getOutput(), false );
            }
        }

        if ( fileSet.getBasedir().exists() && 
             new File( fileSet.getBasedir(), ".git" ).exists() ) 
        {
            // git repo exists, so we must git-pull the changes
            Commandline clPull = createPullCommand( repository, fileSet.getBasedir(), version );
            
            exitCode = GitCommandLineUtils.execute( clPull, stdout, stderr, getLogger() );
            if ( exitCode != 0 )
            {
                return new CheckOutScmResult( clPull.toString(), "The git-pull command failed.", stderr.getOutput(), false );
            }
        }
        
        // and now lets do the git-checkout itself
        Commandline cl = createCommandLine( repository, fileSet.getBasedir(), version );

        exitCode = GitCommandLineUtils.execute( cl, stdout, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new CheckOutScmResult( cl.toString(), "The git-checkout command failed.", stderr.getOutput(), false );
        }

        // and now search for the files
        GitListConsumer listConsumer = new GitListConsumer( getLogger()
        		                                          , fileSet.getBasedir()
        		                                          , ScmFileStatus.CHECKED_IN);

        Commandline clList = GitListCommand.createCommandLine( repository, fileSet.getBasedir() );
        
        exitCode = GitCommandLineUtils.execute( clList, listConsumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new CheckOutScmResult( clList.toString(), "The git-ls-files command failed.", stderr.getOutput(), false );
        }

        return new CheckOutScmResult( cl.toString(), listConsumer.getListedFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( GitScmProviderRepository repository, File workingDirectory,
                                                 ScmVersion version )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "checkout" );

        
        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            cl.createArgument().setValue( version.getName() );
        }

        return cl;
    }
    
    /**
     * create a git-clone repository command 
     */
    private Commandline createCloneCommand( GitScmProviderRepository repository, File workingDirectory )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory.getParentFile(), "clone" );
        
        cl.createArgument().setValue( repository.getUrl() );
        
        cl.createArgument().setFile( workingDirectory );
        
        return cl;
    }
    
    /**
     * create a git-pull repository command 
     */
    private Commandline createPullCommand( GitScmProviderRepository repository, File workingDirectory,
                                           ScmVersion version )
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "pull" );
        
        cl.createArgument().setValue( repository.getUrl() );
        
        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            if ( version instanceof ScmTag )
            {
            	cl.createArgument().setValue( "tag" );
            	cl.createArgument().setValue( version.getName() );
            }
            else
            {
            	cl.createArgument().setValue( version.getName() + ":" + version.getName() );
            }
        }
        else {
        	cl.createArgument().setValue( "master" );
        }
        return cl;
    }    
}
