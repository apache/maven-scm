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

import java.util.function.Consumer;

import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.git.command.GitCommand;
import org.apache.maven.scm.provider.git.jgit.command.CustomizableSshSessionFactoryCommand;
import org.apache.maven.scm.provider.git.jgit.command.branch.JGitBranchCommand;
import org.apache.maven.scm.provider.git.jgit.command.checkin.JGitCheckInCommand;
import org.apache.maven.scm.provider.git.jgit.command.checkout.JGitCheckOutCommand;
import org.apache.maven.scm.provider.git.jgit.command.remoteinfo.JGitRemoteInfoCommand;
import org.apache.maven.scm.provider.git.jgit.command.tag.JGitTagCommand;
import org.apache.maven.scm.provider.git.jgit.command.untag.JGitUntagCommand;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.sisu.Priority;

/**
 * Allows to register callbacks for all commands leveraging {@link TransportCommand}.
 */
@Singleton
@Named("jgit")
@Priority(1) // must have higher priority than default JGitScmProvider
public class JGitTestScmProvider extends JGitScmProvider implements ScmProvider {
    private Consumer<? super JGitCheckInCommand> checkInCommandCallback;
    private Consumer<? super JGitCheckOutCommand> checkOutCommandCallback;
    private Consumer<? super JGitRemoteInfoCommand> remoteInfoCommandCallback;
    private Consumer<? super JGitTagCommand> tagCommandCallback;
    private Consumer<? super JGitUntagCommand> untagCommandCallback;
    private Consumer<? super JGitBranchCommand> branchCommandCallback;

    @Inject
    public JGitTestScmProvider(Prompter prompter) {
        super(prompter);
    }

    public void registerCheckInCommandCallback(Consumer<? super JGitCheckInCommand> gitCommandConsumer) {
        checkInCommandCallback = gitCommandConsumer;
    }

    public void registerCheckOutCommandCallback(Consumer<? super JGitCheckOutCommand> gitCommandConsumer) {
        checkOutCommandCallback = gitCommandConsumer;
    }

    public void registerRemoteInfoCommandCallback(Consumer<? super JGitRemoteInfoCommand> gitCommandConsumer) {
        remoteInfoCommandCallback = gitCommandConsumer;
    }

    public void registerTagCommandCallback(Consumer<? super JGitTagCommand> gitCommandConsumer) {
        tagCommandCallback = gitCommandConsumer;
    }

    public void registerUntagCommandCallback(Consumer<? super JGitUntagCommand> gitCommandConsumer) {
        untagCommandCallback = gitCommandConsumer;
    }

    public void registerBranchCommandCallback(Consumer<? super JGitBranchCommand> gitCommandConsumer) {
        branchCommandCallback = gitCommandConsumer;
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

    @Override
    protected GitCommand getBranchCommand() {
        JGitBranchCommand command = (JGitBranchCommand) super.getBranchCommand();
        if (branchCommandCallback != null) {
            branchCommandCallback.accept(command);
        }
        return command;
    }

    @Override
    protected GitCommand getTagCommand() {
        JGitTagCommand command = (JGitTagCommand) super.getTagCommand();
        if (tagCommandCallback != null) {
            tagCommandCallback.accept(command);
        }
        return command;
    }

    @Override
    protected GitCommand getUntagCommand() {
        JGitUntagCommand command = (JGitUntagCommand) super.getUntagCommand();
        if (untagCommandCallback != null) {
            untagCommandCallback.accept(command);
        }
        return command;
    }

    /**
     * Uses a custom SSHD session factory which accepts all hosts for all commands which (potentially) involve
     * a server connection.
     */
    public void useLenientSshdSessionFactory() {
        Consumer<CustomizableSshSessionFactoryCommand> configureLenientSshSessionFactory = command -> {
            command.setSshSessionFactorySupplier(AcceptAllHostsSshdSessionFactory::new);
        };
        registerCheckOutCommandCallback(configureLenientSshSessionFactory);
        registerCheckInCommandCallback(configureLenientSshSessionFactory);
        registerBranchCommandCallback(configureLenientSshSessionFactory);
        registerTagCommandCallback(configureLenientSshSessionFactory);
        registerUntagCommandCallback(configureLenientSshSessionFactory);
    }
}
