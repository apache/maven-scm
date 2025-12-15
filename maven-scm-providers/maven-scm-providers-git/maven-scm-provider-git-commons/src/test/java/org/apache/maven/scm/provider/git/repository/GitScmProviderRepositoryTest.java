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
package org.apache.maven.scm.provider.git.repository;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
class GitScmProviderRepositoryTest extends ScmTestCase {
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
    void legalFileURL() throws Exception {
        testUrl("scm:git:file:///tmp/repo", null, "file:///tmp/repo", null, null, null, null, 0, null);
    }

    @Test
    void legalFileHomeURL() throws Exception {
        testUrl("scm:git:file://~/repo", null, "file://~/repo", null, null, null, null, 0, null);
    }

    @Test
    void legalSshHomeURL() throws Exception {
        testUrl("scm:git:ssh://~/repo", null, "ssh://~/repo", null, null, null, null, 0, null);
    }

    @Test
    void legalLocalhostFileURL() throws Exception {
        testUrl(
                "scm:git:file://somedirectory/tmp/repo",
                null,
                "file://somedirectory/tmp/repo",
                null,
                null,
                null,
                null,
                0,
                "somedirectory/tmp/repo");
    }

    @Test
    void legalHistnameFileURL() throws Exception {
        testUrl(
                "scm:git:file://my_server/tmp/repo",
                null,
                "file://my_server/tmp/repo",
                null,
                null,
                null,
                null,
                0,
                "my_server/tmp/repo");
    }

    @Test
    void legalHttpURL() throws Exception {
        testUrl(
                "scm:git:http://gitrepos.apache.org",
                null,
                "http://gitrepos.apache.org",
                null,
                null,
                null,
                "gitrepos.apache.org",
                0,
                null);
    }

    @Test
    void legalHttpURLWithUser() throws Exception {
        testUrl(
                "scm:git:http://user@gitrepos.apache.org",
                null,
                "http://user@gitrepos.apache.org",
                null,
                "user",
                null,
                "gitrepos.apache.org",
                0,
                null);
    }

    @Test
    void legalHttpURLWithUserPassword() throws Exception {
        testUrl(
                "scm:git:http://user:password@gitrepos.apache.org",
                "http://user:********@gitrepos.apache.org",
                "http://user:password@gitrepos.apache.org",
                null,
                "user",
                "password",
                "gitrepos.apache.org",
                0,
                null);
    }

    @Test
    void legalHttpsURL() throws Exception {
        testUrl(
                "scm:git:https://gitrepos.apache.org/repos/projectA",
                null,
                "https://gitrepos.apache.org/repos/projectA",
                null,
                null,
                null,
                "gitrepos.apache.org",
                0,
                "repos/projectA");
    }

    @Test
    void legalFileWindowsURL() throws Exception {
        // FIXME This URL is invalid, hell knows why Git accepts it. It should be by us right away
        testUrl("scm:git:file://c:\\tmp\\repo", null, "file://c:\\tmp\\repo", null, null, null, null, 0, null);
    }

    @Test
    void legalHttpsURLWithUser() throws Exception {
        testUrl(
                "scm:git:https://user@gitrepos.apache.org",
                null,
                "https://user@gitrepos.apache.org",
                null,
                "user",
                null,
                "gitrepos.apache.org",
                0,
                null);
    }

    @Test
    void legalHttpsURLWithUserPassword() throws Exception {
        testUrl(
                "scm:git:https://user:password@gitrepos.apache.org",
                "https://user:********@gitrepos.apache.org",
                "https://user:password@gitrepos.apache.org",
                null,
                "user",
                "password",
                "gitrepos.apache.org",
                0,
                null);
    }

    @Test
    void legalSshURLWithUser() throws Exception {
        testUrl(
                "scm:git:ssh://user@gitrepos.apache.org",
                null,
                "ssh://user@gitrepos.apache.org",
                null,
                "user",
                null,
                "gitrepos.apache.org",
                0,
                null);
    }

    @Test
    void legalSshURLWithUserPassword() throws Exception {
        testUrl(
                "scm:git:ssh://user:password@gitrepos.apache.org",
                "ssh://user:********@gitrepos.apache.org",
                "ssh://user:password@gitrepos.apache.org",
                null,
                "user",
                "password",
                "gitrepos.apache.org",
                0,
                null);
    }

    @Test
    void legalGitURL() throws Exception {
        testUrl(
                "scm:git:git://gitrepos.apache.org",
                null,
                "git://gitrepos.apache.org",
                null,
                null,
                null,
                "gitrepos.apache.org",
                0,
                null);
    }

    @Test
    void gitDevURL() throws Exception {
        testUrl(
                "scm:git:git@github.com:olamy/scm-git-test-one-module.git",
                null,
                "git@github.com:olamy/scm-git-test-one-module.git",
                null,
                "git",
                null,
                "github.com",
                0,
                null);
    }

    @Test
    void gitDevURLWIthPort() throws Exception {
        testUrl(
                "scm:git:git@github.com:222:olamy/scm-git-test-one-module.git",
                null,
                "git@github.com:222:olamy/scm-git-test-one-module.git",
                null,
                "git",
                null,
                "github.com",
                222,
                null);
    }

    // For SCM-639
    @Test
    void gitDevUrlWithNumberedRepoAndNoPort() throws Exception {
        testUrl(
                "scm:git:git@github.com:4sh/blah.git",
                null,
                "git@github.com:4sh/blah.git",
                null,
                "git",
                null,
                "github.com",
                0,
                null);
    }

    // For SCM-629
    @Test
    void gitDevUrlWithNumberedRepoAndMinus() throws Exception {
        testUrl(
                "scm:git:ssh://git@github.com/360-Innovations/FJPAQuery.git",
                null,
                "ssh://git@github.com/360-Innovations/FJPAQuery.git",
                null,
                "git",
                null,
                "github.com",
                0,
                null);
    }

    // For SCM-707
    @Test
    void specialCharacters() throws Exception {
        testUrl(
                "scm:git:http://gitrepos.apache.org",
                "/_@_&_:_?_#_%20",
                "pass word",
                null,
                "http://gitrepos.apache.org",
                null,
                "http://%2F_%40_&_:_%3F_%23_%2520:pass%20word@gitrepos.apache.org",
                null,
                "gitrepos.apache.org",
                0,
                null);

        testUrl(
                "scm:git:http://gitrepos.apache.org",
                "user name",
                "/_@_&_:_?_#_%20",
                null,
                "http://gitrepos.apache.org",
                null,
                "http://user%20name:%2F_%40_&_:_%3F_%23_%2520@gitrepos.apache.org",
                null,
                "gitrepos.apache.org",
                0,
                null);
    }

    @Test
    void legalGitPortUrl() throws Exception {
        testUrl(
                "scm:git:http://username@gitrepos.apache.org:8800/pmgt/trunk",
                null,
                "http://username@gitrepos.apache.org:8800/pmgt/trunk",
                null,
                "username",
                null,
                "gitrepos.apache.org",
                8800,
                null);

        testUrl(
                "scm:git:https://username@gitrepos.apache.org:20443/pmgt/trunk",
                null,
                "https://username@gitrepos.apache.org:20443/pmgt/trunk",
                null,
                "username",
                null,
                "gitrepos.apache.org",
                20443,
                null);

        testUrl(
                "scm:git:git://username@gitrepos.apache.org:8800/pmgt/trunk",
                null,
                "git://username@gitrepos.apache.org:8800/pmgt/trunk",
                null,
                "username",
                null,
                "gitrepos.apache.org",
                8800,
                null);

        testUrl(
                "scm:git:ssh://username@gitrepos.apache.org:8080/pmgt/trunk",
                null,
                "ssh://username@gitrepos.apache.org:8080/pmgt/trunk",
                null,
                "username",
                null,
                "gitrepos.apache.org",
                8080,
                null);

        testUrl(
                "scm:git:ssh://username:password@gitrepos.apache.org/pmgt/trunk",
                "ssh://username:********@gitrepos.apache.org/pmgt/trunk",
                "ssh://username:password@gitrepos.apache.org/pmgt/trunk",
                null,
                "username",
                "password",
                "gitrepos.apache.org",
                0,
                null);
    }

    @Test
    void usernameWithAtAndPasswordInUrl() throws Exception {
        testUrl(
                "scm:git:http://username@site.com:password@gitrepos.apache.org:8800/pmgt/trunk",
                "http://username%40site.com:********@gitrepos.apache.org:8800/pmgt/trunk",
                "http://username%40site.com:password@gitrepos.apache.org:8800/pmgt/trunk",
                null,
                "username@site.com",
                "password",
                "gitrepos.apache.org",
                8800,
                null);
    }

    // ----------------------------------------------------------------------
    // the following tests are for combined fetch + push URLs
    // ----------------------------------------------------------------------

    @Test
    void httpFetchSshPushUrl() throws Exception {
        testUrl(
                "scm:git:[fetch=]http://git.apache.org/myprj.git[push=]ssh://myuser:mypassword@git.apache.org/~/myrepo/myprj.git",
                "[fetch=]http://myuser:********@git.apache.org/myprj.git[push=]ssh://myuser:********@git.apache.org/~/myrepo/myprj.git",
                "http://myuser:mypassword@git.apache.org/myprj.git",
                "ssh://myuser:mypassword@git.apache.org/~/myrepo/myprj.git",
                "myuser",
                "mypassword",
                "git.apache.org",
                0,
                null);

        testUrl(
                "scm:git:[push=]ssh://myuser:mypassword@git.apache.org/~/myrepo/myprj.git[fetch=]http://git.apache.org/myprj.git",
                "[fetch=]http://myuser:********@git.apache.org/myprj.git[push=]ssh://myuser:********@git.apache.org/~/myrepo/myprj.git",
                "http://myuser:mypassword@git.apache.org/myprj.git",
                "ssh://myuser:mypassword@git.apache.org/~/myrepo/myprj.git",
                "myuser",
                "mypassword",
                "git.apache.org",
                0,
                null);
    }

    // ----------------------------------------------------------------------
    // Testing illegal URLs
    // ----------------------------------------------------------------------

    // X in fact this url is perfectly valid from a technical perspective
    // X it will be interpreted by git as git://file/tmp/git
    @Test
    @Disabled
    void nottestIllegalFileUrl() throws Exception {
        checkIllegalUrl("file:/tmp/git");
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private GitScmProviderRepository testUrl(
            String scmUrl,
            String expectedToString,
            String expectedFetchUrl,
            String expectedPushUrl,
            String expectedUser,
            String expectedPassword,
            String expectedHost,
            int expectedPort,
            String expectedPath)
            throws Exception, ScmRepositoryException {

        ScmRepository repository = testScmRepository(scmUrl, expectedToString, expectedFetchUrl);

        GitScmProviderRepository providerRepository = (GitScmProviderRepository) repository.getProviderRepository();

        return testScmProviderRepository(
                expectedToString,
                expectedFetchUrl,
                expectedPushUrl,
                expectedUser,
                expectedPassword,
                expectedHost,
                expectedPort,
                providerRepository);
    }

    private GitScmProviderRepository testUrl(
            String scmUrl,
            String username,
            String password,
            String expectedScmRepositoryToString,
            String expectedScmRepositoryFetchUrl,
            String expectedScmProviderRepositoryToString,
            String expectedScmProviderRepositoryFetchUrl,
            String expectedPushUrl,
            String expectedHost,
            int expectedPort,
            String expectedPath)
            throws Exception, ScmRepositoryException {

        ScmRepository repository =
                testScmRepository(scmUrl, expectedScmRepositoryToString, expectedScmRepositoryFetchUrl);

        GitScmProviderRepository providerRepository = (GitScmProviderRepository) repository.getProviderRepository();

        providerRepository.setUser(username);

        providerRepository.setPassword(password);

        return testScmProviderRepository(
                expectedScmProviderRepositoryToString,
                expectedScmProviderRepositoryFetchUrl,
                expectedPushUrl,
                username,
                password,
                expectedHost,
                expectedPort,
                providerRepository);
    }

    private GitScmProviderRepository testScmProviderRepository(
            String expectedToString,
            String expectedFetchUrl,
            String expectedPushUrl,
            String expectedUser,
            String expectedPassword,
            String expectedHost,
            int expectedPort,
            GitScmProviderRepository providerRepository) {
        assertEquals(expectedFetchUrl, providerRepository.getFetchUrl(), "fetch url is incorrect");

        if (expectedPushUrl != null) {
            assertEquals(expectedPushUrl, providerRepository.getPushUrl(), "push url is incorrect");
        }

        assertEquals(expectedUser, providerRepository.getUser(), "User is incorrect");

        assertEquals(expectedPassword, providerRepository.getPassword(), "Password is incorrect");

        assertEquals(expectedHost == null ? "" : expectedHost, providerRepository.getHost(), "Host is incorrect");

        if (expectedPort > 0) {
            assertEquals(expectedPort, providerRepository.getPort(), "Port is incorrect");
        }

        return providerRepository;
    }

    private ScmRepository testScmRepository(String scmUrl, String expectedToString, String expectedFetchUrl)
            throws ScmRepositoryException, NoSuchScmProviderException {
        ScmRepository repository = scmManager.makeScmRepository(scmUrl);

        assertNotNull(repository, "ScmManager.makeScmRepository() returned null");

        assertNotNull(repository.getProviderRepository(), "The provider repository was null.");

        assertTrue(
                repository.getProviderRepository() instanceof GitScmProviderRepository,
                "The SCM Repository isn't a " + GitScmProviderRepository.class.getName() + ".");

        if (expectedToString != null) {
            assertEquals("git:" + expectedToString, repository.toString(), "toString is incorrect");
        } else {
            assertEquals("git:" + expectedFetchUrl, repository.toString(), "toString is incorrect");
        }

        return repository;
    }

    private void checkIllegalUrl(String url) throws Exception {
        try {
            scmManager.makeScmRepository("scm:git:" + url);

            fail("Expected a ScmRepositoryException while testing the url '" + url + "'.");
        } catch (ScmRepositoryException e) {
            // expected
        }
    }

    @Test
    void getParent() throws Exception {
        new GitScmProviderRepository("http://gitrepos.apache.org");
    }
}
