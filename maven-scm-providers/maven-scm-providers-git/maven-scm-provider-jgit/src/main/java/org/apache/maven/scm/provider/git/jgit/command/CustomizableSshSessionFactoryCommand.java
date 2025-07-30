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
package org.apache.maven.scm.provider.git.jgit.command;

import java.util.function.BiFunction;

import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.slf4j.Logger;

/**
 * Supplemental interface for commands that potentially involve a remote server communication.
 * It allows to customize the SSH session factory being used.
 */
public interface CustomizableSshSessionFactoryCommand {

    /**
     * Sets a different supplier for the SSH session factory that will be used by this command.
     * By default it uses {@link ScmProviderAwareSshdSessionFactory}.
     *
     * @param sshSessionFactorySupplier a function that takes a GitScmProviderRepository and Logger and returns a ScmProviderAwareSshdSessionFactory
     */
    void setSshSessionFactorySupplier(
            BiFunction<GitScmProviderRepository, Logger, ScmProviderAwareSshdSessionFactory> sshSessionFactorySupplier);
}
