package org.apache.maven.scm.provider.git.jgit.command.tag;

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
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
public class JGitTagCommand
    extends AbstractTagCommand
    implements GitCommand
{

    public ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag, String message )
        throws ScmException
    {
        return executeTagCommand( repo, fileSet, tag, new ScmTagParameters( message ) );
    }

    /**
     * {@inheritDoc}
     */
    public ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag,
                                        ScmTagParameters scmTagParameters )
        throws ScmException
    {
        if ( tag == null || StringUtils.isEmpty( tag.trim() ) )
        {
            throw new ScmException( "tag name must be specified" );
        }

        if ( !fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "This provider doesn't support tagging subsets of a directory" );
        }

        String escapedTagName = tag.trim().replace( ' ', '_' );

        Git git = null;
        try
        {
            git = JGitUtils.openRepo( fileSet.getBasedir() );

            // tag the revision
            String tagMessage = scmTagParameters.getMessage();
            Ref tagRef = git.tag().setName( escapedTagName ).setMessage( tagMessage ).setForceUpdate( false ).call();

            if ( repo.isPushChanges() )
            {
                getLogger().info( "push tag [" + escapedTagName + "] to remote..." );
                JGitUtils.push( getLogger(), git, (GitScmProviderRepository) repo, new RefSpec( Constants.R_TAGS
                    + escapedTagName ) );
            }

            // search for the tagged files
            RevWalk revWalk = new RevWalk( git.getRepository() );
            RevCommit commit = revWalk.parseCommit( tagRef.getObjectId() );
            revWalk.release();

            final TreeWalk walk = new TreeWalk( git.getRepository() );
            walk.reset(); // drop the first empty tree, which we do not need here
            walk.setRecursive( true );
            walk.addTree( commit.getTree() );

            List<ScmFile> taggedFiles = new ArrayList<ScmFile>();
            while ( walk.next() )
            {
                taggedFiles.add( new ScmFile( walk.getPathString(), ScmFileStatus.CHECKED_OUT ) );
            }
            walk.release();

            return new TagScmResult( "JGit tag", taggedFiles );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit tag failure!", e );
        }
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }

}
