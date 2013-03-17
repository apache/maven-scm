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

import org.apache.commons.io.FilenameUtils;
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
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitAddCommand
    extends AbstractAddCommand
    implements GitCommand
{
    /**
     * {@inheritDoc}
     */
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        if ( fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "You must provide at least one file/directory to add" );
        }

        AddScmResult result = executeAddFileSet( fileSet );

        if ( result != null )
        {
            return result;
        }
        
        // SCM-709: statusCommand uses repositoryRoot instead of workingDirectory, adjust it with relativeRepositoryPath
        Commandline clRevparse = GitStatusCommand.createRevparseShowToplevelCommand( fileSet );
        
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        String relativeRepositoryPath = null;
        
        int exitCode;

        exitCode = GitCommandLineUtils.execute( clRevparse, stdout, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            // git-status returns non-zero if nothing to do
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "Could not resolve toplevel" );
            }
        }
        else
        {
            relativeRepositoryPath = URI.create( stdout.getOutput().trim() ).relativize( fileSet.getBasedir().toURI() ).getPath(); 
        }

        // git-add doesn't show single files, but only summary :/
        // so we must run git-status and consume the output
        // borrow a few things from the git-status command
        Commandline clStatus = GitStatusCommand.createCommandLine( repository, fileSet );

        GitStatusConsumer statusConsumer = new GitStatusConsumer( getLogger(), fileSet.getBasedir(), relativeRepositoryPath );
        stderr = new CommandLineUtils.StringStreamConsumer();
        exitCode = GitCommandLineUtils.execute( clStatus, statusConsumer, stderr, getLogger() );
        if ( exitCode != 0 )
        {
            // git-status returns non-zero if nothing to do
            if ( getLogger().isInfoEnabled() )
            {
                getLogger().info( "nothing added to commit but untracked files present (use \"git add\" to track)" );
            }
        }

        List<ScmFile> changedFiles = new ArrayList<ScmFile>();

        // rewrite all detected files to now have status 'checked_in'
        for ( ScmFile scmfile : statusConsumer.getChangedFiles() )
        {
            // if a specific fileSet is given, we have to check if the file is really tracked
            for ( File f : fileSet.getFileList() )
            {
                if ( FilenameUtils.separatorsToUnix( f.getPath() ).equals( scmfile.getPath() ) )
                {
                    changedFiles.add( scmfile );
                }
            }
        }

        Commandline cl = createCommandLine( fileSet.getBasedir(), fileSet.getFileList() );
        return new AddScmResult( cl.toString(), changedFiles );
    }

    public static Commandline createCommandLine( File workingDirectory, List<File> files )
        throws ScmException
    {
        Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDirectory, "add" );

        // use this separator to make clear that the following parameters are files and not revision info.
        cl.createArg().setValue( "--" );

        GitCommandLineUtils.addTarget( cl, files );

        return cl;
    }

    private AddScmResult executeAddFileSet( ScmFileSet fileSet )
        throws ScmException
    {
        File workingDirectory = fileSet.getBasedir();
        List<File> files = fileSet.getFileList();

        // command line can be too long for windows so add files individually (see SCM-697)
        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            for ( File file : files )
            {
                AddScmResult result = executeAddFiles( workingDirectory, Collections.singletonList( file ) );

                if ( result != null )
                {
                    return result;
                }
            }
        }
        else
        {
            AddScmResult result = executeAddFiles( workingDirectory, files );

            if ( result != null )
            {
                return result;
            }
        }

        return null;
    }

    private AddScmResult executeAddFiles( File workingDirectory, List<File> files )
        throws ScmException
    {
        Commandline cl = createCommandLine( workingDirectory, files );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();
        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        int exitCode = GitCommandLineUtils.execute( cl, stdout, stderr, getLogger() );

        if ( exitCode != 0 )
        {
            return new AddScmResult( cl.toString(), "The git-add command failed.", stderr.getOutput(), false );
        }

        return null;
    }
}
