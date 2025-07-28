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
package org.apache.maven.scm.provider.git.gitexe;

import javax.inject.Named;
import javax.inject.Singleton;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.info.InfoScmResult;
import org.apache.maven.scm.provider.git.AbstractGitScmProvider;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.gitexe.command.add.GitAddCommand;
import org.apache.maven.scm.provider.git.gitexe.command.blame.GitBlameCommand;
import org.apache.maven.scm.provider.git.gitexe.command.branch.GitBranchCommand;
import org.apache.maven.scm.provider.git.gitexe.command.changelog.GitChangeLogCommand;
import org.apache.maven.scm.provider.git.gitexe.command.checkin.GitCheckInCommand;
import org.apache.maven.scm.provider.git.gitexe.command.checkout.GitCheckOutCommand;
import org.apache.maven.scm.provider.git.gitexe.command.diff.GitDiffCommand;
import org.apache.maven.scm.provider.git.gitexe.command.info.GitInfoCommand;
import org.apache.maven.scm.provider.git.gitexe.command.remoteinfo.GitRemoteInfoCommand;
import org.apache.maven.scm.provider.git.gitexe.command.remove.GitRemoveCommand;
import org.apache.maven.scm.provider.git.gitexe.command.status.GitStatusCommand;
import org.apache.maven.scm.provider.git.gitexe.command.tag.GitTagCommand;
import org.apache.maven.scm.provider.git.gitexe.command.untag.GitUntagCommand;
import org.apache.maven.scm.provider.git.gitexe.command.update.GitUpdateCommand;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
@Singleton
@Named("git")
public class GitExeScmProvider extends AbstractGitScmProvider {
    private final Map<String, String> environmentVariables;

    public GitExeScmProvider() {
        environmentVariables = new HashMap<>();
    }

    /** {@inheritDoc} */
    protected GitCommand getAddCommand() {
        return new GitAddCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getBranchCommand() {
        return new GitBranchCommand(environmentVariables);
    }

    /** {@inheritDoc} */
    protected GitCommand getChangeLogCommand() {
        return new GitChangeLogCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getCheckInCommand() {
        return new GitCheckInCommand(environmentVariables);
    }

    /** {@inheritDoc} */
    protected GitCommand getCheckOutCommand() {
        return new GitCheckOutCommand(environmentVariables);
    }

    /** {@inheritDoc} */
    protected GitCommand getDiffCommand() {
        return new GitDiffCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getExportCommand() {
        return null; // X TODO
    }

    /** {@inheritDoc} */
    protected GitCommand getRemoveCommand() {
        return new GitRemoveCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getStatusCommand() {
        return new GitStatusCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getTagCommand() {
        return new GitTagCommand(environmentVariables);
    }

    /** {@inheritDoc} */
    protected GitCommand getUntagCommand() {
        return new GitUntagCommand(environmentVariables);
    }

    /** {@inheritDoc} */
    protected GitCommand getUpdateCommand() {
        return new GitUpdateCommand();
    }

    /** {@inheritDoc} */
    public GitCommand getInfoCommand() {
        return new GitInfoCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getBlameCommand() {
        return new GitBlameCommand();
    }

    /** {@inheritDoc} */
    protected GitCommand getRemoteInfoCommand() {
        return new GitRemoteInfoCommand(environmentVariables);
    }

    /** {@inheritDoc} */
    protected String getRepositoryURL(File path) throws ScmException {
        // Note: I need to supply just 1 absolute path, but ScmFileSet won't let me without
        // a basedir (which isn't used here anyway), so use a dummy file.
        // and a dummy ScmProviderRepository
        InfoScmResult result =
                info(new GitScmProviderRepository(path.toPath().toUri().toASCIIString()), new ScmFileSet(path), null);

        if (result.getInfoItems().size() != 1) {
            throw new ScmRepositoryException("Cannot find URL: "
                    + (result.getInfoItems().size() == 0 ? "no" : "multiple") + " items returned by the info command");
        }

        return result.getInfoItems().get(0).getURL();
    }

    public void setEnvironmentVariable(String key, String value) {
        environmentVariables.put(key, value);
    }
}
