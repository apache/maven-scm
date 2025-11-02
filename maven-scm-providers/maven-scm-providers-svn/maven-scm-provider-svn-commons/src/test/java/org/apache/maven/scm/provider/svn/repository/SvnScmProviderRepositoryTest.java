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
package org.apache.maven.scm.provider.svn.repository;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class SvnScmProviderRepositoryTest extends ScmTestCase {
    private ScmManager scmManager;

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();

        scmManager = getScmManager();
    }

    // ----------------------------------------------------------------------
    // Testing legal URLs
    // ----------------------------------------------------------------------

    @Test
    public void testLegalFileURL() throws Exception {
        testUrl("scm:svn:file:///tmp/repo", "file:///tmp/repo", null, null, null);
    }

    @Test
    public void testLegalLocalhostFileURL() throws Exception {
        testUrl("scm:svn:file://localhost/tmp/repo", "file://localhost/tmp/repo", null, null, null);
    }

    @Test
    public void testLegalHistnameFileURL() throws Exception {
        testUrl("scm:svn:file://my_server/tmp/repo", "file://my_server/tmp/repo", null, null, null);
    }

    @Test
    public void testLegalHttpURL() throws Exception {
        testUrl(
                "scm:svn:http://subversion.tigris.org",
                "http://subversion.tigris.org",
                null,
                null,
                "subversion.tigris.org");
    }

    @Test
    public void testLegalHttpURLWithUser() throws Exception {
        testUrl(
                "scm:svn:http://user@subversion.tigris.org",
                "http://subversion.tigris.org",
                "user",
                null,
                "subversion.tigris.org");
    }

    @Test
    public void testLegalHttpURLWithUserPassword() throws Exception {
        testUrl(
                "scm:svn:http://user:password@subversion.tigris.org",
                "http://subversion.tigris.org",
                "user",
                "password",
                "subversion.tigris.org");
    }

    @Test
    public void testLegalHttpsURL() throws Exception {
        testUrl(
                "scm:svn:https://subversion.tigris.org",
                "https://subversion.tigris.org",
                null,
                null,
                "subversion.tigris.org");
    }

    @Test
    public void testLegalHttpsURLWithUser() throws Exception {
        testUrl(
                "scm:svn:https://user@subversion.tigris.org",
                "https://subversion.tigris.org",
                "user",
                null,
                "subversion.tigris.org");
    }

    @Test
    public void testLegalHttpsURLWithUserPassword() throws Exception {
        testUrl(
                "scm:svn:https://user:password@subversion.tigris.org",
                "https://subversion.tigris.org",
                "user",
                "password",
                "subversion.tigris.org");
    }

    @Test
    public void testLegalSvnURL() throws Exception {
        testUrl(
                "scm:svn:svn://subversion.tigris.org",
                "svn://subversion.tigris.org",
                null,
                null,
                "subversion.tigris.org");
    }

    @Test
    public void testLegalSvnPlusUsernameURL() throws Exception {
        testUrl(
                "scm:svn:svn://username@subversion.tigris.org",
                "svn://subversion.tigris.org",
                "username",
                null,
                "subversion.tigris.org");
    }

    @Test
    public void testLegalSvnPlusUsernamePasswordURL() throws Exception {
        testUrl(
                "scm:svn:svn://username:password@subversion.tigris.org",
                "svn://subversion.tigris.org",
                "username",
                "password",
                "subversion.tigris.org");
    }

    @Test
    public void testLegalSvnPlusSshURL() throws Exception {
        testUrl(
                "scm:svn:svn+ssh://subversion.tigris.org",
                "svn+ssh://subversion.tigris.org",
                null,
                null,
                "subversion.tigris.org");
    }

    /* This test require a specific subversion config file
    public void testLegalSvnPlusXxxURL()
        throws Exception
    {
        testUrl( "scm:svn:svn+something://subversion.tigris.org", "svn+something://subversion.tigris.org", null,
                 "subversion.tigris.org" );
    }*/

    @Test
    public void testLegalSvnPlusSshPlusUsernameURL() throws Exception {
        testUrl(
                "scm:svn:svn+ssh://username@subversion.tigris.org",
                "svn+ssh://username@subversion.tigris.org",
                null,
                null,
                "username@subversion.tigris.org");
    }

    /* This test require a specific subversion config file
    public void testLegalSvnPlusXxxPlusUsernameURL()
        throws Exception
    {
        testUrl( "scm:svn:svn+something://username@subversion.tigris.org", "svn+something://username@subversion.tigris.org", null,
                 "username@subversion.tigris.org" );
    }*/

    @Test
    public void testLegalSvnPortUrl() throws Exception {
        testUrl(
                "scm:svn:http://username@subversion.tigris.org:8800/pmgt/trunk",
                "http://subversion.tigris.org:8800/pmgt/trunk",
                "username",
                "subversion.tigris.org",
                8800);
        testUrl(
                "scm:svn:https://username@subversion.tigris.org:8080/pmgt/trunk",
                "https://subversion.tigris.org:8080/pmgt/trunk",
                "username",
                "subversion.tigris.org",
                8080);
        testUrl(
                "scm:svn:svn://username@subversion.tigris.org:8800/pmgt/trunk",
                "svn://subversion.tigris.org:8800/pmgt/trunk",
                "username",
                "subversion.tigris.org",
                8800);
        testUrl(
                "scm:svn:svn+ssh://username@subversion.tigris.org:8080/pmgt/trunk",
                "svn+ssh://username@subversion.tigris.org:8080/pmgt/trunk",
                null,
                "username@subversion.tigris.org",
                8080);
    }

    // ----------------------------------------------------------------------
    // Testing illegal URLs
    // ----------------------------------------------------------------------

    @Test
    public void testIllegalFileUrl() throws Exception {
        testIllegalUrl("file:/tmp/svn");
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testUrl(
            String scmUrl, String expectedUrl, String expectedUser, String expectedPassword, String expectedHost)
            throws Exception {
        ScmRepository repository = scmManager.makeScmRepository(scmUrl);

        assertNotNull(repository, "ScmManager.makeScmRepository() returned null");

        assertNotNull(repository.getProviderRepository(), "The provider repository was null.");

        assertTrue(
                repository.getProviderRepository() instanceof SvnScmProviderRepository,
                "The SCM Repository isn't a " + SvnScmProviderRepository.class.getName() + ".");

        SvnScmProviderRepository providerRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        assertEquals(expectedUrl, providerRepository.getUrl(), "url is incorrect");

        assertEquals("svn:" + expectedUrl, repository.toString(), "url string is incorrect");

        assertEquals(expectedUser, providerRepository.getUser(), "User is incorrect");

        assertEquals(expectedPassword, providerRepository.getPassword(), "Password is incorrect");

        assertEquals(
                expectedHost,
                ((SvnScmProviderRepository) repository.getProviderRepository()).getHost(),
                "Host is incorrect");
    }

    private void testUrl(String scmUrl, String expectedUrl, String expectedUser, String expectedHost, int expectedPort)
            throws Exception {
        testUrl(scmUrl, expectedUrl, expectedUser, null, expectedHost);
    }

    @SuppressWarnings("unused")
    private void testUrl(
            String scmUrl,
            String expectedUrl,
            String expectedUser,
            String expectedPassword,
            String expectedHost,
            int expectedPort)
            throws Exception {
        testUrl(scmUrl, expectedUrl, expectedUser, expectedPassword, expectedHost);

        ScmRepository repository = scmManager.makeScmRepository(scmUrl);

        assertEquals(
                expectedPort,
                ((SvnScmProviderRepository) repository.getProviderRepository()).getPort(),
                "Port is incorrect");
    }

    private void testIllegalUrl(String url) throws Exception {
        try {
            scmManager.makeScmRepository("scm:svn:" + url);

            fail("Expected a ScmRepositoryException while testing the url '" + url + "'.");
        } catch (ScmRepositoryException e) {
            // expected
        }
    }

    @Test
    public void testGetParent() {
        new SvnScmProviderRepository("http://subversion.tigris.org");
    }

    @Test
    public void testGetParentDotSlashEndingURL() {
        SvnScmProviderRepository slashDotRepo = new SvnScmProviderRepository("file://a/b/c/././.");
        assertTrue(slashDotRepo.getParent() instanceof SvnScmProviderRepository);
        assertEquals("file://a/b", ((SvnScmProviderRepository) slashDotRepo.getParent()).getUrl());
    }

    @Test
    public void testGetParentSlashEndingURL() {
        SvnScmProviderRepository slashRepo = new SvnScmProviderRepository("file://a/b/c///");
        assertTrue(slashRepo.getParent() instanceof SvnScmProviderRepository);
        assertEquals("file://a/b", ((SvnScmProviderRepository) slashRepo.getParent()).getUrl());
    }
}
