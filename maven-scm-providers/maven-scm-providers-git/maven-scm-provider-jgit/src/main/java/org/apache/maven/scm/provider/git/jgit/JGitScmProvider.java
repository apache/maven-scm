package org.apache.maven.scm.provider.git.jgit;

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

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.git.AbstractGitScmProvider;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.add.JGitAddCommand;
import org.apache.maven.scm.provider.git.jgit.command.blame.JGitBlameCommand;
import org.apache.maven.scm.provider.git.jgit.command.branch.JGitBranchCommand;
import org.apache.maven.scm.provider.git.jgit.command.changelog.JGitChangeLogCommand;
import org.apache.maven.scm.provider.git.jgit.command.checkin.JGitCheckInCommand;
import org.apache.maven.scm.provider.git.jgit.command.checkout.JGitCheckOutCommand;
import org.apache.maven.scm.provider.git.jgit.command.diff.JGitDiffCommand;
import org.apache.maven.scm.provider.git.jgit.command.info.JGitInfoCommand;
import org.apache.maven.scm.provider.git.jgit.command.list.JGitListCommand;
import org.apache.maven.scm.provider.git.jgit.command.remoteinfo.JGitRemoteInfoCommand;
import org.apache.maven.scm.provider.git.jgit.command.status.JGitStatusCommand;
import org.apache.maven.scm.provider.git.jgit.command.tag.JGitTagCommand;
import org.apache.maven.scm.provider.git.jgit.command.untag.JGitUntagCommand;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
@Singleton
@Named( "jgit" )
public class JGitScmProvider
    extends AbstractGitScmProvider
{
    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getAddCommand()
    {
        return new JGitAddCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getBranchCommand()
    {
        return new JGitBranchCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getChangeLogCommand()
    {
        return new JGitChangeLogCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getCheckInCommand()
    {
        return new JGitCheckInCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getCheckOutCommand()
    {
        return new JGitCheckOutCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getDiffCommand()
    {
        return new JGitDiffCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getExportCommand()
    {
        throw new UnsupportedOperationException( "getExportCommand" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getRemoveCommand()
    {
        throw new UnsupportedOperationException( "getRemoveCommand" );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getStatusCommand()
    {
        return new JGitStatusCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getTagCommand()
    {
        return new JGitTagCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getUntagCommand()
    {
        return new JGitUntagCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getUpdateCommand()
    {
        throw new UnsupportedOperationException( "getUpdateCommand" );
    }

    /**
     * {@inheritDoc}
     */
    protected GitCommand getListCommand()
    {
        return new JGitListCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GitCommand getInfoCommand()
    {
        return new JGitInfoCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRepositoryURL( File path )
        throws ScmException
    {
        // Note: I need to supply just 1 absolute path, but ScmFileSet won't let
        // me without
        // a basedir (which isn't used here anyway), so use a dummy file.
        InfoScmResult result = info( null, new ScmFileSet( new File( "" ), path ), null );

        if ( result.getInfoItems().size() != 1 )
        {
            throw new ScmRepositoryException(
                "Cannot find URL: " + ( result.getInfoItems().size() == 0 ? "no" : "multiple" )
                    + " items returned by the info command" );
        }

        return ( result.getInfoItems().get( 0 ) ).getURL();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getBlameCommand()
    {
        return new JGitBlameCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand getRemoteInfoCommand()
    {
        return new JGitRemoteInfoCommand();
    }
}
