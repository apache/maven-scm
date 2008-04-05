package org.apache.maven.scm.provider.git.gitexe.command.add;

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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.GitCommandLineUtils;
import org.apache.maven.scm.provider.git.gitexe.command.status.GitStatusCommand;
import org.apache.maven.scm.provider.git.gitexe.command.status.GitStatusConsumer;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitAddCommand extends AbstractAddCommand implements GitCommand
{
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;
        
        if ( fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "You must provide at least one file/directory to add" );
        }

        Commandline cl = createCommandLine( fileSet.getBasedir(), fileSet.getFileList() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        exitCode = GitCommandLineUtils.execute( cl, stdout, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            return new AddScmResult( cl.toString(), "The git-add command failed.", stderr.getOutput(), false );
        }
        
        // git-add doesn't show single files, but only summary :/
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

        List changedFiles = new ArrayList();
        
        // rewrite all detected files to now have status 'checked_in'
        for ( Iterator it = statusConsumer.getChangedFiles().iterator(); it.hasNext(); )
        {
            ScmFile scmfile = (ScmFile) it.next();
            
            // if a specific fileSet is given, we have to check if the file is really tracked
            for ( Iterator itfl = fileSet.getFileList().iterator(); itfl.hasNext(); )
            {
                File f = (File) itfl.next();
                if ( f.toString().equals( scmfile.getPath() )) 
                {
                    changedFiles.add( scmfile );                  
                }
            }
        }        
        return new AddScmResult( cl.toString(), changedFiles );
    }

    public static Commandline createCommandLine( File workingDirectory, List/*File*/ files )
        throws ScmException
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "add" );

        GitCommandLineUtils.addTarget( cl, files );

        return cl;
    }

}
