package org.apache.maven.scm.provider.git.jgit.command.checkout;

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
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.jgit.command.branch.JGitBranchCommand;
import org.apache.maven.scm.provider.git.jgit.command.remoteinfo.JGitRemoteInfoCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.WindowCacheConfig;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
public class JGitCheckOutCommand
    extends AbstractCheckOutCommand
    implements GitCommand
{
    /**
     * For git, the given repository is a remote one. We have to clone it first if the working directory does not
     * contain a git repo yet, otherwise we have to git-pull it.
     * <p/>
     * {@inheritDoc}
     */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet,
                                                        ScmVersion version, boolean recursive )
        throws ScmException
    {
        GitScmProviderRepository repository = (GitScmProviderRepository) repo;

        if ( GitScmProviderRepository.PROTOCOL_FILE.equals( repository.getFetchInfo().getProtocol() )
            && repository.getFetchInfo().getPath().indexOf( fileSet.getBasedir().getPath() ) >= 0 )
        {
            throw new ScmException( "remote repository must not be the working directory" );
        }

        Git git = null;
        try
        {

            ProgressMonitor monitor = JGitUtils.getMonitor( getLogger() );

            String branch = version != null ? version.getName() : null;

            if ( StringUtils.isBlank( branch ) )
            {
                branch = Constants.MASTER;
            }

            getLogger().debug( "try checkout of branch: " + branch );

            if ( !fileSet.getBasedir().exists() || !( new File( fileSet.getBasedir(), ".git" ).exists() ) )
            {
                if ( fileSet.getBasedir().exists() )
                {
                    // git refuses to clone otherwise
                    fileSet.getBasedir().delete();
                }

                // FIXME only if windauze
                WindowCacheConfig cfg = new WindowCacheConfig();
                cfg.setPackedGitMMAP( false );
                cfg.install();

                // no git repo seems to exist, let's clone the original repo
                CredentialsProvider credentials = JGitUtils.getCredentials( (GitScmProviderRepository) repo );
                getLogger().info( "cloning [" + branch + "] to " + fileSet.getBasedir() );
                CloneCommand command = Git.cloneRepository().setURI( repository.getFetchUrl() );
                command.setCredentialsProvider( credentials ).setBranch( branch ).setDirectory( fileSet.getBasedir() );
                command.setProgressMonitor( monitor );
                git = command.call();
            }

            JGitRemoteInfoCommand remoteInfoCommand = new JGitRemoteInfoCommand();
            remoteInfoCommand.setLogger( getLogger() );
            RemoteInfoScmResult result = remoteInfoCommand.executeRemoteInfoCommand( repository, fileSet, null );

            if ( git == null )
            {
                // deliberately not using JGitUtils.openRepo(), the caller told us exactly where to checkout
                git = Git.open( fileSet.getBasedir() );
            }
            
            if ( fileSet.getBasedir().exists() && new File( fileSet.getBasedir(), ".git" ).exists()
                && result.getBranches().size() > 0 )
            {
                // git repo exists, so we must git-pull the changes
                CredentialsProvider credentials = JGitUtils.prepareSession( getLogger(), git, repository );

                if ( version != null && StringUtils.isNotEmpty( version.getName() ) && ( version instanceof ScmTag ) )
                {
                    // A tag will not be pulled but we only fetch all the commits from the upstream repo
                    // This is done because checking out a tag might not happen on the current branch
                    // but create a 'detached HEAD'.
                    // In fact, a tag in git may be in multiple branches. This occurs if
                    // you create a branch after the tag has been created
                    getLogger().debug( "fetch..." );
                    git.fetch().setCredentialsProvider( credentials ).setProgressMonitor( monitor ).call();
                }
                else
                {
                    getLogger().debug( "pull..." );
                    git.pull().setCredentialsProvider( credentials ).setProgressMonitor( monitor ).call();
                }
            }

            Set<String> localBranchNames = JGitBranchCommand.getShortLocalBranchNames( git );
            if ( version instanceof ScmTag )
            {
                getLogger().info( "checkout tag [" + branch + "] at " + fileSet.getBasedir() );
                git.checkout().setName( branch ).call();
            }
            else if ( localBranchNames.contains( branch ) )
            {
                getLogger().info( "checkout [" + branch + "] at " + fileSet.getBasedir() );
                git.checkout().setName( branch ).call();
            }
            else
            {
                getLogger().info( "checkout remote branch [" + branch + "] at " + fileSet.getBasedir() );
                git.checkout().setName( branch ).setCreateBranch( true ).setStartPoint( Constants.DEFAULT_REMOTE_NAME
                                                                                            + "/" + branch ).call();
            }

            RevWalk revWalk = new RevWalk( git.getRepository() );
            RevCommit commit = revWalk.parseCommit( git.getRepository().resolve( Constants.HEAD ) );
            revWalk.release();

            final TreeWalk walk = new TreeWalk( git.getRepository() );
            walk.reset(); // drop the first empty tree, which we do not need here
            walk.setRecursive( true );
            walk.addTree( commit.getTree() );

            List<ScmFile> listedFiles = new ArrayList<ScmFile>();
            while ( walk.next() )
            {
                listedFiles.add( new ScmFile( walk.getPathString(), ScmFileStatus.CHECKED_OUT ) );
            }
            walk.release();

            getLogger().debug( "current branch: " + git.getRepository().getBranch() );

            return new CheckOutScmResult( "checkout via JGit", listedFiles );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit checkout failure!", e );
        }
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }

}
