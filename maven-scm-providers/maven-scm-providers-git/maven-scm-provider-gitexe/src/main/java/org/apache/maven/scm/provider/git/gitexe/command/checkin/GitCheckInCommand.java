package org.apache.maven.scm.provider.git.gitexe.command.checkin;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.gitexe.command.add.GitAddCommand;
import org.apache.maven.scm.provider.git.gitexe.command.status.GitStatusCommand;
import org.apache.maven.scm.provider.git.gitexe.command.status.GitStatusConsumer;
import org.codehaus.plexus.util.FileUtils;
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
public class GitCheckInCommand extends AbstractCheckInCommand implements GitCommand
{
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      ScmVersion version )
        throws ScmException
    {
    	GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        File messageFile = FileUtils.createTempFile( "maven-scm-", ".commit", null );
        try
        {
            FileUtils.fileWrite( messageFile.getAbsolutePath(), message );
        }
        catch ( IOException ex )
        {
            return new CheckInScmResult( null, "Error while making a temporary file for the commit message: " +
                ex.getMessage(), null, false );
        }

        try
        {
            if ( !fileSet.getFileList().isEmpty() )
            {
                // if specific fileSet is given, we have to git-add them first
                // otherwise we will use 'git-commit -a' later

                Commandline clAdd = GitAddCommand.createCommandLine( fileSet.getBasedir(), fileSet.getFileList() );
                
                exitCode = GitCommandLineUtils.execute( clAdd, stdout, stderr, getLogger() );
                
                if ( exitCode != 0 )
                {
                    return new CheckInScmResult( clAdd.toString(), "The git-add command failed.", stderr.getOutput(), false );
                }

            }
            
            // git-commit doesn't show single files, but only summary :/
            // so we must run git-status and consume the output
            // borrow a few things from the git-status command
            Commandline clStatus = GitStatusCommand.createCommandLine( repository, fileSet );
            
            GitStatusConsumer statusConsumer = new GitStatusConsumer( getLogger(), fileSet.getBasedir() );
            exitCode = GitCommandLineUtils.execute( clStatus, statusConsumer, stderr, getLogger() );
            if ( exitCode != 0 )
            {
                // git-status returns non-zero if nothing to do
                getLogger().info( "nothing added to commit but untracked files present (use \"git add\" to track)" );
            }
            
        	Commandline clCommit = createCommitCommandLine(repository, fileSet, messageFile);
        	
            exitCode = GitCommandLineUtils.execute( clCommit, stdout, stderr, getLogger() );
	        if ( exitCode != 0 )
	        {
	            return new CheckInScmResult( clCommit.toString(), "The git-commit command failed.", stderr.getOutput(), false );
	        }
	        
	        Commandline cl = createPushCommandLine( repository, fileSet, version );
	
            exitCode = GitCommandLineUtils.execute( cl, stdout, stderr, getLogger() );
	        if ( exitCode != 0 )
	        {
	            return new CheckInScmResult( cl.toString(), "The git-push command failed.", stderr.getOutput(), false );
	        }

	        List checkedInFiles = new ArrayList( statusConsumer.getChangedFiles().size() );
	        
	        // rewrite all detected files to now have status 'checked_in'
	        for ( Iterator it = statusConsumer.getChangedFiles().iterator(); it.hasNext(); )
	        {
	            ScmFile scmfile = new ScmFile( ((ScmFile) it.next()).getPath(), ScmFileStatus.CHECKED_IN );
	            
	            if ( fileSet.getFileList().isEmpty() ) {
	                checkedInFiles.add( scmfile );	                
	            }
	            else
	            {
	                // if a specific fileSet is given, we have to check if the file is really tracked
	                for ( Iterator itfl = fileSet.getFileList().iterator(); itfl.hasNext(); )
	                {
	                    File f = (File) itfl.next();
	                    if ( f.toString().equals( scmfile.getPath() )) 
	                    {
	                        checkedInFiles.add( scmfile );                  
	                    }

	                }
	            }
	        }
	        
	        return new CheckInScmResult( cl.toString(), checkedInFiles );
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( messageFile );
            }
            catch ( IOException ex )
            {
                // ignore
            }
        }

    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createPushCommandLine( GitScmProviderRepository repository, ScmFileSet fileSet,
    		                                         ScmVersion version )
        throws ScmException
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( fileSet.getBasedir(), "push");

        //X TODO handle version
        
        return cl;
    }
    
    public static Commandline createCommitCommandLine( GitScmProviderRepository repository, ScmFileSet fileSet,
                                                       File messageFile )
	throws ScmException
	{
		Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( fileSet.getBasedir(), "commit");

		cl.createArgument().setValue( "--verbose" );
		
		cl.createArgument().setValue( "-F" );
		cl.createArgument().setValue( messageFile.getAbsolutePath() );

		if ( fileSet.getFileList().isEmpty() ) 
		{
		    // commit all tracked files
		    cl.createArgument().setValue( "-a" );
		}
		else 
		{
		    // specify exactly which files to commit 
		    GitCommandLineUtils.addTarget( cl, fileSet.getFileList() );
		}
		
		return cl;
	}

}
