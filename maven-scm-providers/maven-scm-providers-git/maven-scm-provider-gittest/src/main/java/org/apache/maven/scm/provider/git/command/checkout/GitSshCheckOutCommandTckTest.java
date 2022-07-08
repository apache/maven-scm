package org.apache.maven.scm.provider.git.command.checkout;

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

import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.checkout.CheckOutCommandTckTest;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyEncryptionContext;
import org.apache.sshd.common.config.keys.writer.openssh.OpenSSHKeyPairResourceWriter;
import org.apache.sshd.git.GitLocationResolver;
import org.apache.sshd.git.pack.GitPackCommandFactory;
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
import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 *
 */
public abstract class GitSshCheckOutCommandTckTest
    extends CheckOutCommandTckTest
{
    protected final SshServer sshServer;
    protected final KeyPair keyPair;
    protected final List<PublicKey> acceptedPublicKeys;

    @Rule
    public TemporaryFolder tmpDirectory = new TemporaryFolder();

    protected GitSshCheckOutCommandTckTest() throws GeneralSecurityException
    {
         sshServer = CoreTestSupportUtils.setupTestServer( getClass() );
         keyPair = CommonTestSupportUtils.generateKeyPair( KeyUtils.RSA_ALGORITHM, 2048 );
         acceptedPublicKeys = new ArrayList<>();
         acceptedPublicKeys.add( keyPair.getPublic() );
         PublickeyAuthenticator authenticator = new KeySetPublickeyAuthenticator( "onlykey",
                  acceptedPublicKeys );
         sshServer.setPublickeyAuthenticator( authenticator );
    }

    void writePrivateKeyAsPkcs8( Path file, String passphrase )
            throws IOException, GeneralSecurityException
    {
        // encryption only optional
        if ( passphrase != null )
        {
            // encryption with format outlined in https://dnaeon.github.io/openssh-private-key-binary-format/
            OpenSSHKeyPairResourceWriter writer = new OpenSSHKeyPairResourceWriter();
            OpenSSHKeyEncryptionContext context = new OpenSSHKeyEncryptionContext();
            context.setCipherType( "192" );
            context.setPassword( passphrase );
            try ( OutputStream output = Files.newOutputStream( file ) )
            {
                writer.writePrivateKey( keyPair, "comment", context, output );
            }
        }
        else
        {
            // wrap unencrypted private key as regular PKCS8 private key
            PKCS8Generator pkcs8Generator = new JcaPKCS8Generator( keyPair.getPrivate(), null );
            PemObject pemObject = pkcs8Generator.generate();

            try ( Writer writer = Files.newBufferedWriter( file );
                            JcaPEMWriter pw = new JcaPEMWriter( writer ) )
            {
                pw.writeObject( pemObject );
            }
        }

        if ( file.getFileSystem().supportedFileAttributeViews().contains( "posix" ) )
        {
            // must only be readable/writeable by me
            Files.setPosixFilePermissions( file, PosixFilePermissions.fromString( "rwx------" ) );
        }
    }

    protected abstract String getScmProvider();

    /** {@inheritDoc} */
    public String getScmUrl()
        throws Exception
    {
        return "scm:" + getScmProvider() + ":ssh://localhost:" + sshServer.getPort() + "/repository";
    }

    public void configureCredentials( ScmRepository repository, String passphrase )
        throws Exception
    {
        ScmProviderRepositoryWithHost providerRepository =
             ScmProviderRepositoryWithHost.class.cast( repository.getProviderRepository() );
        // store as file
        Path privateKeyFile = tmpDirectory.newFile().toPath();
        writePrivateKeyAsPkcs8( privateKeyFile, passphrase );
        providerRepository.setPrivateKey( privateKeyFile.toString() );
        providerRepository.setPassphrase( passphrase ); // may be null
    }

    /** {@inheritDoc} */
    public void initRepo()
        throws Exception
    {
        GitScmTestUtils.initRepo( "src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory() );

        GitLocationResolver gitLocationResolver = new GitLocationResolver()
        {
            @Override
            public Path resolveRootDirectory( String command, String[] args, ServerSession session, FileSystem fs )
                throws IOException
            {
                return getRepositoryRoot().getParentFile().toPath();
            }
        };
        sshServer.setCommandFactory( new GitPackCommandFactory( gitLocationResolver ) );
        sshServer.start();

        // as checkout also already happens in setup() make sure to configure credentials here as well
        configureCredentials( getScmRepository(), null );
    }

    @Override
    public void removeRepo() throws Exception
    {
        sshServer.stop();
        super.removeRepo();
    }

    @Override
    @Test
    public void testCheckOutCommandTest()
        throws Exception
    {
        configureCredentials( getScmRepository(), null );
        super.testCheckOutCommandTest();
    }

    @Test
    public void testCheckOutCommandWithPassphraseTest() throws Exception
    {
        // TODO: currently no easy way to pass passphrase in gitexe
        Assume.assumeTrue( "Ignore test with passphrase for provider " + getScmProvider(),
                           "jgit".equals( getScmProvider() ) );
        configureCredentials( getScmRepository(), "mySecret" );
        super.testCheckOutCommandTest();
    }
}
