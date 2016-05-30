package org.apache.maven.scm.provider.git.jgit.command.list;

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
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.CredentialsProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
public class JGitListCommand
    extends AbstractListCommand
    implements GitCommand
{

    @Override
    protected ListScmResult executeListCommand( ScmProviderRepository repo, ScmFileSet fileSet, boolean recursive,
                                                ScmVersion scmVersion )
        throws ScmException
    {

        Git git = null;
        try
        {
            git = JGitUtils.openRepo( fileSet.getBasedir() );
            CredentialsProvider credentials =
                JGitUtils.prepareSession( getLogger(), git, (GitScmProviderRepository) repo );

            List<ScmFile> list = new ArrayList<ScmFile>();
            Collection<Ref> lsResult = git.lsRemote().setCredentialsProvider( credentials ).call();
            for ( Ref ref : lsResult )
            {
                getLogger().debug( ref.getObjectId().getName() + "  " + ref.getTarget().getName() );
                list.add( new ScmFile( ref.getName(), ScmFileStatus.CHECKED_IN ) );
            }

            return new ListScmResult( "JGit ls-remote", list );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit ls-remote failure!", e );
        }
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }
}
