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

import java.io.File;
import java.net.InetSocketAddress;
import java.security.PublicKey;

import org.apache.maven.scm.provider.git.jgit.command.ScmProviderAwareSshdSessionFactory;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.eclipse.jgit.annotations.NonNull;
import org.eclipse.jgit.internal.transport.sshd.OpenSshServerKeyDatabase;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.sshd.ServerKeyDatabase;
import org.slf4j.Logger;

public final class AcceptAllHostsSshdSessionFactory extends ScmProviderAwareSshdSessionFactory {
    public AcceptAllHostsSshdSessionFactory(GitScmProviderRepository repo, Logger logger) {
        super(repo, logger);
    }

    @Override
    protected ServerKeyDatabase createServerKeyDatabase(File homeDir, File sshDir) {
        return new OpenSshServerKeyDatabase(false, null) {
            @Override
            public boolean accept(
                    @NonNull String connectAddress,
                    @NonNull InetSocketAddress remoteAddress,
                    @NonNull PublicKey serverKey,
                    @NonNull Configuration config,
                    CredentialsProvider provider) {
                return true;
            }
        };
    }
}
