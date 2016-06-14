package org.apache.maven.scm.provider.git.jgit.command;

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

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.*;
import org.eclipse.jgit.util.FS;
import org.eclipse.jgit.util.StringUtils;

/**
 * Implementation of {@link TransportConfigCallback} which adds
 * a public/private key identity to ssh URLs if configured.
 */
public class JGitTransportConfigCallback implements TransportConfigCallback {
    private SshSessionFactory sshSessionFactory = null;

    public JGitTransportConfigCallback(GitScmProviderRepository repo) {
        // File
        // File + Passphrase
        if (repo.getFetchInfo().getProtocol().equals("ssh")) {
            if (!StringUtils.isEmptyOrNull(repo.getPrivateKey()) && repo.getPassphrase() == null) {
                sshSessionFactory = new UnprotectedPrivateKeySessionFactory(repo);
            } else if (!StringUtils.isEmptyOrNull(repo.getPrivateKey()) && repo.getPassphrase() != null) {
                sshSessionFactory = new ProtectedPrivateKeyFileSessionFactory(repo);
            } else {
                sshSessionFactory = new SimpleSessionFactory();
            }
        }
    }

    @Override
    public void configure(Transport transport) {
        if (transport instanceof SshTransport) {
            SshTransport sshTransport = (SshTransport) transport;
            sshTransport.setSshSessionFactory(sshSessionFactory);
        }
    }

    static private class SimpleSessionFactory extends JschConfigSessionFactory {
        @Override
        protected void configure(OpenSshConfig.Host host, Session session) {
        }
    }

    static private abstract class PrivateKeySessionFactory extends SimpleSessionFactory {
        private final GitScmProviderRepository repo;

        public GitScmProviderRepository getRepo() {
            return repo;
        }

        public PrivateKeySessionFactory(GitScmProviderRepository repo) {
            this.repo = repo;
        }
    }

    static private class UnprotectedPrivateKeySessionFactory extends PrivateKeySessionFactory {

        public UnprotectedPrivateKeySessionFactory(GitScmProviderRepository repo) {
            super(repo);
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
            JSch defaultJSch = super.createDefaultJSch(fs);
            defaultJSch.addIdentity(getRepo().getPrivateKey());
            return defaultJSch;
        }
    }

    static private class ProtectedPrivateKeyFileSessionFactory extends PrivateKeySessionFactory {

        public ProtectedPrivateKeyFileSessionFactory(GitScmProviderRepository repo) {
            super(repo);
        }

        @Override
        protected JSch createDefaultJSch(FS fs) throws JSchException {
            JSch defaultJSch = super.createDefaultJSch(fs);
            defaultJSch.addIdentity(getRepo().getPrivateKey(), getRepo().getPassphrase());
            return defaultJSch;
        }
    }
}
