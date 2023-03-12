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

import javax.annotation.Priority;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import java.util.function.Consumer;

import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.checkin.JGitCheckInCommand;
import org.apache.maven.scm.provider.git.jgit.command.checkout.JGitCheckOutCommand;
import org.apache.maven.scm.provider.git.jgit.command.remoteinfo.JGitRemoteInfoCommand;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.eclipse.jgit.api.TransportCommand;

/**
 * Allows to register callbacks for all commands leveraging {@link TransportCommand}.
 */
@Singleton
@Named("jgit")
@Priority(1) // must have higher priority than default JGitScmProvider
public class JGitTestScmProvider extends JGitScmProvider implements ScmProvider {
    private Consumer<JGitCheckInCommand> checkInCommandCallback;
    private Consumer<JGitCheckOutCommand> checkOutCommandCallback;
    private Consumer<JGitRemoteInfoCommand> remoteInfoCommandCallback;

    @Inject
    public JGitTestScmProvider(Prompter prompter) {
        super(prompter);
    }

    public void registerCheckInCommandCallback(Consumer<JGitCheckInCommand> gitCommandConsumer) {
        checkInCommandCallback = gitCommandConsumer;
    }

    public void registerCheckOutCommandCallback(Consumer<JGitCheckOutCommand> gitCommandConsumer) {
        checkOutCommandCallback = gitCommandConsumer;
    }

    public void registerRemoteInfoCommandCallback(Consumer<JGitRemoteInfoCommand> gitCommandConsumer) {
        remoteInfoCommandCallback = gitCommandConsumer;
    }

    @Override
    protected GitCommand getCheckInCommand() {
        JGitCheckInCommand command = (JGitCheckInCommand) super.getCheckInCommand();
        if (checkInCommandCallback != null) {
            checkInCommandCallback.accept(command);
        }
        return command;
    }

    @Override
    protected GitCommand getCheckOutCommand() {
        JGitCheckOutCommand command = (JGitCheckOutCommand) super.getCheckOutCommand();
        if (checkOutCommandCallback != null) {
            checkOutCommandCallback.accept(command);
        }
        return command;
    }

    @Override
    protected GitCommand getRemoteInfoCommand() {
        JGitRemoteInfoCommand command = (JGitRemoteInfoCommand) super.getRemoteInfoCommand();
        if (remoteInfoCommandCallback != null) {
            remoteInfoCommandCallback.accept(command);
        }
        return command;
    }
}
