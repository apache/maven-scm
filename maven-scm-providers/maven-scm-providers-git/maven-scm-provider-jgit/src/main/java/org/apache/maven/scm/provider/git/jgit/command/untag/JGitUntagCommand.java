package org.apache.maven.scm.provider.git.jgit.command.untag;

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

import java.util.Collection;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmUntagParameters;
import org.apache.maven.scm.command.untag.AbstractUntagCommand;
import org.apache.maven.scm.command.untag.UntagScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.transport.PushResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteRefUpdate;

/** {@inheritDoc} */
public class JGitUntagCommand extends AbstractUntagCommand implements GitCommand
{

    @Override
    protected ScmResult executeUntagCommand( ScmProviderRepository repository, ScmFileSet fileSet,
        ScmUntagParameters scmUntagParameters ) throws ScmException
    {
        String tagName = scmUntagParameters.getTag();
        if ( tagName == null || StringUtils.isEmpty( tagName.trim() ) )
        {
            throw new ScmException( "tag name must be specified" );
        }
        String escapedTagName = tagName.trim().replace( ' ', '_' );

        Git git = null;
        try
        {
            git = JGitUtils.openRepo( fileSet.getBasedir() );

            // delete the tag
            if ( git.tagDelete().setTags( escapedTagName ).call().isEmpty() )
            {
                return new UntagScmResult( "JGit tagDelete", "Failed to delete tag", "", false );
            }

            if ( repository.isPushChanges() )
            {
                // From https://stackoverflow.com/q/11892766/696632
                RefSpec refSpec = new RefSpec()
                    .setSource( null )
                    .setDestination( Constants.R_TAGS + escapedTagName );

                getLogger().info( "push delete tag [" + escapedTagName + "] to remote..." );
                ScmLogger logger = getLogger();

                Iterable<PushResult> pushResultList
                        = JGitUtils.push( logger, git, (GitScmProviderRepository) repository, refSpec );
                if ( logger.isInfoEnabled() )
                {
                    for ( PushResult pushResult : pushResultList )
                    {
                        Collection<RemoteRefUpdate> ru = pushResult.getRemoteUpdates();
                        for ( RemoteRefUpdate remoteRefUpdate : ru )
                        {
                            logger.info( remoteRefUpdate.getStatus() + " - " + remoteRefUpdate );
                        }
                    }
                }
            }

            return new UntagScmResult( "JGit tagDelete" );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit tagDelete failure!", e );
        }
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }
}
