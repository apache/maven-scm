package org.apache.maven.scm.provider.git.jgit.command.branch;

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
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
public class JGitBranchCommand
    extends AbstractBranchCommand
    implements GitCommand
{

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScmResult executeBranchCommand( ScmProviderRepository repo, ScmFileSet fileSet, String branch,
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

        Git git = null;
        try
        {
            git = JGitUtils.openRepo( fileSet.getBasedir() );
            Ref branchResult = git.branchCreate().setName( branch ).call();
            getLogger().info( "created [" + branchResult.getName() + "]" );

            if ( getLogger().isDebugEnabled() )
            {
                for ( String branchName : getShortLocalBranchNames( git ) )
                {
                    getLogger().debug( "local branch available: " + branchName );
                }
            }

            if ( repo.isPushChanges() )
            {
                getLogger().info( "push branch [" + branch + "] to remote..." );
                JGitUtils.push( getLogger(), git, (GitScmProviderRepository) repo, new RefSpec( Constants.R_HEADS
                    + branch ) );
            }

            // search for the tagged files
            final RevWalk revWalk = new RevWalk( git.getRepository() );
            RevCommit commit = revWalk.parseCommit( branchResult.getObjectId() );
            revWalk.release();

            final TreeWalk walk = new TreeWalk( git.getRepository() );
            walk.reset(); // drop the first empty tree, which we do not need here
            walk.setRecursive( true );
            walk.addTree( commit.getTree() );

            List<ScmFile> files = new ArrayList<ScmFile>();
            while ( walk.next() )
            {
                files.add( new ScmFile( walk.getPathString(), ScmFileStatus.CHECKED_OUT ) );
            }
            walk.release();

            return new BranchScmResult( "JGit branch", files );

        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit branch failed!", e );
        }
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }

    /**
     * gets a set of names of the available branches in the given repo
     * 
     * @param git the repo to list the branches for
     * @return set of short branch names
     * @throws GitAPIException
     */
    public static Set<String> getShortLocalBranchNames( Git git )
        throws GitAPIException
    {
        Set<String> branches = new HashSet<String>();
        Iterator<Ref> iter = git.branchList().call().iterator();
        while ( iter.hasNext() )
        {
            branches.add( Repository.shortenRefName( iter.next().getName() ) );
        }
        return branches;
    }
}
