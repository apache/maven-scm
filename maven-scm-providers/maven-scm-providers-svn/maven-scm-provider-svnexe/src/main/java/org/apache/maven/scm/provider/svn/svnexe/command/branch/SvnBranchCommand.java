package org.apache.maven.scm.provider.svn.svnexe.command.branch;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmBranchParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.branch.AbstractBranchCommand;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.SvnCommandUtils;
import org.apache.maven.scm.provider.svn.SvnTagBranchUtils;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 *
 * @todo since this is just a copy, use that instead.
 */
public class SvnBranchCommand
    extends AbstractBranchCommand
    implements SvnCommand
{

    public ScmResult executeBranchCommand( ScmProviderRepository repo, ScmFileSet fileSet, String branch,
                                           ScmBranchParameters scmBranchParameters )
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

        SvnScmProviderRepository repository = (SvnScmProviderRepository) repo;

        File messageFile = FileUtils.createTempFile( "maven-scm-", ".commit", null );

        try
        {
            FileUtils.fileWrite( messageFile.getAbsolutePath(), "UTF-8", scmBranchParameters.getMessage() );
        }
        catch ( IOException ex )
        {
            return new BranchScmResult( null, "Error while making a temporary file for the commit message: "
                + ex.getMessage(), null, false );
        }

        Commandline cl = createCommandLine( repository, fileSet.getBasedir(), branch, messageFile,
                                            scmBranchParameters );

        CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + SvnCommandLineUtils.cryptPassword( cl ) );

            if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
            {
                getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
            }
        }

        int exitCode;

        try
        {
            exitCode = SvnCommandLineUtils.execute( cl, stdout, stderr, getLogger() );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
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

        if ( exitCode != 0 )
        {
            return new BranchScmResult( cl.toString(), "The svn branch command failed.", stderr.getOutput(), false );
        }

        List<ScmFile> fileList = new ArrayList<ScmFile>();

        List<File> files = null;

        try
        {
            List<File> listFiles = FileUtils.getFiles( fileSet.getBasedir(), "**", "**/.svn/**", false );
            files = listFiles;
        }
        catch ( IOException e )
        {
            throw new ScmException( "Error while executing command.", e );
        }

        for ( File f : files )
        {
            fileList.add( new ScmFile( f.getPath(), ScmFileStatus.TAGGED ) );
        }

        return new BranchScmResult( cl.toString(), fileList );
    }

    /** {@inheritDoc} */
    public ScmResult executeBranchCommand( ScmProviderRepository repo, ScmFileSet fileSet, String branch,
                                           String message )
        throws ScmException
    {
        ScmBranchParameters scmBranchParameters = new ScmBranchParameters( message );
        return executeBranchCommand( repo, fileSet, branch, scmBranchParameters );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 String branch, File messageFile )
    {
        ScmBranchParameters scmBranchParameters = new ScmBranchParameters();
        scmBranchParameters.setRemoteBranching( false );
        scmBranchParameters.setPinExternals( false );
        return createCommandLine( repository, workingDirectory, branch, messageFile, scmBranchParameters );
    }

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory,
                                                 String branch, File messageFile,
                                                 ScmBranchParameters scmBranchParameters )
    {
        Commandline cl = SvnCommandLineUtils.getBaseSvnCommandLine( workingDirectory, repository );

        cl.createArg().setValue( "copy" );

        cl.createArg().setValue( "--parents" );

        cl.createArg().setValue( "--file" );

        cl.createArg().setValue( messageFile.getAbsolutePath() );

        cl.createArg().setValue( "--encoding" );

        cl.createArg().setValue( "UTF-8" );

        if ( scmBranchParameters != null && scmBranchParameters.isPinExternals() )
        {
            cl.createArg().setValue( "--pin-externals" );
        }

        if ( scmBranchParameters != null && scmBranchParameters.isRemoteBranching() )
        {
            if ( StringUtils.isNotBlank( scmBranchParameters.getScmRevision() ) )
            {
                cl.createArg().setValue( "--revision" );
                cl.createArg().setValue( scmBranchParameters.getScmRevision() );
            }
            String url = SvnCommandUtils.fixUrl( repository.getUrl(), repository.getUser() );
            cl.createArg().setValue( url + "@" );
        }
        else
        {
            cl.createArg().setValue( "." );
        }
        // Note: this currently assumes you have the branch base checked out too
        String branchUrl = SvnTagBranchUtils.resolveBranchUrl( repository, new ScmBranch( branch ) );
        branchUrl = SvnCommandUtils.fixUrl( branchUrl, repository.getUser() );
        cl.createArg().setValue( branchUrl + "@" );

        return cl;
    }
}
