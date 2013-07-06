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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ProgressMonitor;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @version $Id: JGitCheckOutCommand.java 894145 2009-12-28 10:13:39Z struberg $
 */
public class JGitCheckOutCommand
    extends AbstractCheckOutCommand
    implements GitCommand
{
    /**
     * For git, the given repository is a remote one. We have to clone it first
     * if the working directory does not contain a git repo yet, otherwise we
     * have to git-pull it.
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

        try
        {

            ProgressMonitor monitor = JGitUtils.getMonitor( getLogger() );

            String branch = version != null ? version.getName() : null;
            
            if ( StringUtils.isBlank( branch ) )
            {
                branch = Constants.MASTER;
            }
            
            if ( !fileSet.getBasedir().exists() || !( new File( fileSet.getBasedir(), ".git" ).exists() ) )
            {
                if ( fileSet.getBasedir().exists() )
                {
                    // git refuses to clone otherwise
                    fileSet.getBasedir().delete();
                }

                // no git repo seems to exist, let's clone the original repo
                CredentialsProvider credentials = JGitUtils.getCredentials( (GitScmProviderRepository) repo );
                getLogger().info( "cloning [" + branch + "] to " + fileSet.getBasedir() );
                Git.cloneRepository().setURI( repository.getFetchUrl() ).setCredentialsProvider(
                    credentials ).setBranchesToClone( Collections.singleton( branch ) ).setDirectory(
                    fileSet.getBasedir() ).setProgressMonitor( monitor ).call();

            }
            else
            {
            	// repo already exists, fetch the remote
            	CredentialsProvider credentials = JGitUtils.getCredentials( (GitScmProviderRepository) repo );
                getLogger().info( "fetching..." );
                Git git = Git.open( fileSet.getBasedir() );
                git.fetch().setCredentialsProvider(credentials).setProgressMonitor( monitor ).call();            	
            }

            Git git = Git.open( fileSet.getBasedir() );
            // switch to branch/tag if we currently are not on the proper one
            getLogger().info( "checkout [" + branch + "] to " + fileSet.getBasedir() );
            git.checkout().setName( branch ).call();

            RevWalk revWalk = new RevWalk(git.getRepository());
            RevCommit commit = revWalk.parseCommit(git.getRepository().resolve(Constants.HEAD));
         
			final TreeWalk walk = new TreeWalk(git.getRepository());
        	walk.reset(); // drop the first empty tree, which we do not need here
        	walk.setRecursive(true);
        	walk.addTree(commit.getTree());
        	
        	List<ScmFile> listedFiles = new ArrayList<ScmFile>();
        	while (walk.next()) 
        	{
        		listedFiles.add( new ScmFile( walk.getPathString(), ScmFileStatus.CHECKED_OUT));
        	}

        	return new CheckOutScmResult( "checkout via JGit", listedFiles );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit checkout failure!", e );
        }
    }


}
