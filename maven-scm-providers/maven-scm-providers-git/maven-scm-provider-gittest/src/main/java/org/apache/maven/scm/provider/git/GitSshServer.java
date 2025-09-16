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
package org.apache.maven.scm.provider.git;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.provider.git.sshd.git.pack.GitPackCommandFactory;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;
import org.apache.sshd.git.GitLocationResolver;
import org.apache.sshd.server.SshServer;
import org.apache.sshd.server.auth.pubkey.KeySetPublickeyAuthenticator;
import org.apache.sshd.server.auth.pubkey.PublickeyAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.util.test.CommonTestSupportUtils;
import org.apache.sshd.util.test.CoreTestSupportUtils;
import org.bouncycastle.openssl.PKCS8Generator;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.util.io.pem.PemObject;

/**
 * Local Git SSH server for testing purposes.
 * It acts on top of an existing repository root directory.
 * It uses <a href="https://mina.apache.org/sshd-project/">Apache MINA SSHD</a> for the SSH server implementation.
 * <p>
 * The server generates a key pair during initialization and accepts connections using the private key which can be
 * extracted via {@link GitSshServer#writePrivateKeyAsPkcs8(Path, String)}.
 * Alternatively one may use a custom key pair and add the public key using {@link GitSshServer#addPublicKey(PublicKey)}.
 */
public class GitSshServer {

    protected final SshServer sshServer;
    protected final KeyPair keyPair;
    protected final List<PublicKey> acceptedPublicKeys;

    public GitSshServer() throws GeneralSecurityException {
        sshServer = CoreTestSupportUtils.setupTestServer(getClass());
        keyPair = CommonTestSupportUtils.generateKeyPair(KeyUtils.RSA_ALGORITHM, 2048);
        acceptedPublicKeys = new ArrayList<>();
        acceptedPublicKeys.add(keyPair.getPublic());
        PublickeyAuthenticator authenticator = new KeySetPublickeyAuthenticator("onlykey", acceptedPublicKeys);
        sshServer.setPublickeyAuthenticator(authenticator);
    }

    /**
     * Writes a private key which is accepted by this server to the specified file in PKCS8 format.
     * If a passphrase is provided, the key will be encrypted using OpenSSH's format.
     * If no passphrase is provided, the key will be written as an unencrypted PKCS8 private key.
     * For the same server instance the private key is always the same, so it can be reused.
     *
     * @param file the file to write the private key to
     * @param passphrase the passphrase for encryption, or null for unencrypted
     * @throws GeneralSecurityException if a security error occurs
     * @throws IOException if an I/O error occurs
     */
    public void writePrivateKeyAsPkcs8(Path file, String passphrase) throws IOException, GeneralSecurityException {
        // encryption only optional
        if (passphrase != null) {
            // encryption with format outlined in https://dnaeon.github.io/openssh-private-key-binary-format/
            OpenSSHKeyPairResourceWriter writer = new OpenSSHKeyPairResourceWriter();
            OpenSSHKeyEncryptionContext context = new OpenSSHKeyEncryptionContext();
            context.setCipherType("192");
            context.setPassword(passphrase);
            try (OutputStream output = Files.newOutputStream(file)) {
                writer.writePrivateKey(keyPair, "comment", context, output);
            }
        } else {
            // wrap unencrypted private key as regular PKCS8 private key
            PKCS8Generator pkcs8Generator = new JcaPKCS8Generator(keyPair.getPrivate(), null);
            PemObject pemObject = pkcs8Generator.generate();

            try (Writer writer = Files.newBufferedWriter(file);
                    JcaPEMWriter pw = new JcaPEMWriter(writer)) {
                pw.writeObject(pemObject);
            }
        }

        if (file.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            // must only be readable/writeable by me
            Files.setPosixFilePermissions(file, PosixFilePermissions.fromString("rwx------"));
        }
    }

    public void addPublicKey(PublicKey publicKey) {
        if (publicKey == null) {
            throw new IllegalArgumentException("Public key must not be null");
        }
        acceptedPublicKeys.add(publicKey);
    }

    public int getPort() {
        if (!sshServer.isStarted()) {
            throw new IllegalStateException("SSH server is not started");
        }
        return sshServer.getPort();
    }

    public void start(Path repositoryRoot) throws IOException {
        GitLocationResolver gitLocationResolver = new GitLocationResolver() {
            @Override
            public Path resolveRootDirectory(String command, String[] args, ServerSession session, FileSystem fs)
                    throws IOException {
                return repositoryRoot;
            }
        };
        // use patched version of GitPackCommandFactory including https://github.com/apache/mina-sshd/pull/794
        sshServer.setCommandFactory(new GitPackCommandFactory(gitLocationResolver));
        sshServer.start();
    }

    public void stop() throws IOException {
        sshServer.stop();
    }
}
