/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.provider.git.jgit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.command.untag.UntagScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.git.AbstractGitScmProvider;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.PlexusInteractivityCredentialsProvider;
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
import org.apache.maven.scm.provider.git.jgit.command.remove.JGitRemoveCommand;
import org.apache.maven.scm.provider.git.jgit.command.status.JGitStatusCommand;
import org.apache.maven.scm.provider.git.jgit.command.tag.JGitTagCommand;
import org.apache.maven.scm.provider.git.jgit.command.untag.JGitUntagCommand;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.eclipse.jgit.transport.CredentialsProvider;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Dominik Bartholdi (imod)
 * @since 1.9
 */
@Singleton
@Named("jgit")
public class JGitScmProvider extends AbstractGitScmProvider {
    private final PlexusInteractivityCredentialsProvider credentialsProvider;

    @Inject
    public JGitScmProvider(Prompter prompter) {
        credentialsProvider = new PlexusInteractivityCredentialsProvider(prompter);
        CredentialsProvider.setDefault(credentialsProvider);
    }

    @Override
    public void setInteractive(boolean interactive) {
        credentialsProvider.setInteractive(interactive);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<AddScmResult> getAddCommand() {
        return new JGitAddCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<BranchScmResult> getBranchCommand() {
        return new JGitBranchCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<ChangeLogScmResult> getChangeLogCommand() {
        return new JGitChangeLogCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<CheckInScmResult> getCheckInCommand() {
        return new JGitCheckInCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<CheckOutScmResult> getCheckOutCommand() {
        return new JGitCheckOutCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<DiffScmResult> getDiffCommand() {
        return new JGitDiffCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<ExportScmResult> getExportCommand() {
        throw new UnsupportedOperationException("getExportCommand");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<RemoveScmResult> getRemoveCommand() {
        return new JGitRemoveCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<StatusScmResult> getStatusCommand() {
        return new JGitStatusCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<TagScmResult> getTagCommand() {
        return new JGitTagCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<UntagScmResult> getUntagCommand() {
        return new JGitUntagCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<UpdateScmResult> getUpdateCommand() {
        throw new UnsupportedOperationException("getUpdateCommand");
    }

    /**
     * {@inheritDoc}
     */
    protected GitCommand<ListScmResult> getListCommand() {
        return new JGitListCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GitCommand<InfoScmResult> getInfoCommand() {
        return new JGitInfoCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getRepositoryURL(File path) throws ScmException {
        // Note: I need to supply just 1 absolute path, but ScmFileSet won't let
        // me without
        // a basedir (which isn't used here anyway), so use a dummy file.
        InfoScmResult result = info(null, new ScmFileSet(new File(""), path), null);

        if (result.getInfoItems().size() != 1) {
            throw new ScmRepositoryException("Cannot find URL: "
                    + (result.getInfoItems().size() == 0 ? "no" : "multiple") + " items returned by the info command");
        }

        return (result.getInfoItems().get(0)).getURL();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<BlameScmResult> getBlameCommand() {
        return new JGitBlameCommand();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected GitCommand<RemoteInfoScmResult> getRemoteInfoCommand() {
        return new JGitRemoteInfoCommand();
    }
}
