package org.apache.maven.scm.provider.git.jgit.command.status;

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
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.JGitUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
public class JGitStatusCommand
    extends AbstractStatusCommand
    implements GitCommand
{
    /**
     * {@inheritDoc}
     */
    protected StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        Git git = null;
        try
        {
            git = JGitUtils.openRepo( fileSet.getBasedir() );
            Status status = git.status().call();
            List<ScmFile> changedFiles = getFileStati( status );

            return new StatusScmResult( "JGit status", changedFiles );
        }
        catch ( Exception e )
        {
            throw new ScmException( "JGit status failure!", e );
        }
        finally
        {
            JGitUtils.closeRepo( git );
        }
    }

    private List<ScmFile> getFileStati( Status status )
    {
        List<ScmFile> all = new ArrayList<ScmFile>();
        addAsScmFiles( all, status.getAdded(), ScmFileStatus.ADDED );
        addAsScmFiles( all, status.getChanged(), ScmFileStatus.UPDATED );
        addAsScmFiles( all, status.getConflicting(), ScmFileStatus.CONFLICT );
        addAsScmFiles( all, status.getModified(), ScmFileStatus.MODIFIED );
        addAsScmFiles( all, status.getRemoved(), ScmFileStatus.DELETED );
        return all;
    }

    private void addAsScmFiles( Collection<ScmFile> all, Collection<String> files, ScmFileStatus status )
    {
        for ( String f : files )
        {
            all.add( new ScmFile( f, status ) );
        }
    }
}
