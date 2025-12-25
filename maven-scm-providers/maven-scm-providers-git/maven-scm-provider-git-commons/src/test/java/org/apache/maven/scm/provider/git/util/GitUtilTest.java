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
package org.apache.maven.scm.provider.git.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test for {@link GitUtil}
 */
public class GitUtilTest {

    @Test
    public void testMaskPasswordInUrlWithPort() {
        // URL with port
        String urlWithPort = "https://user:password@host:8080/repo.git";
        String maskedWithPort = GitUtil.maskPasswordInUrl(urlWithPort);
        assertEquals("Password should be masked in URL with port",
                "https://user:********@host:8080/repo.git", maskedWithPort);
    }

    @Test
    public void testMaskPasswordInUrlWithoutPort() {
        // URL without port
        String url = "https://user:password@host/repo.git";
        String masked = GitUtil.maskPasswordInUrl(url);
        assertEquals("Password should be masked in URL without port",
                "https://user:********@host/repo.git", masked);
    }

    @Test
    public void testMaskPasswordInUrlNoPassword() {
        // URL with no password
        String noPassword = "https://user@host/repo.git";
        assertEquals("URL without password should remain unchanged",
                noPassword, GitUtil.maskPasswordInUrl(noPassword));
    }

    @Test
    public void testMaskPasswordInUrlNoUser() {
        // URL with no user
        String noUser = "https://host/repo.git";
        assertEquals("URL without user should remain unchanged",
                noUser, GitUtil.maskPasswordInUrl(noUser));
    }

    @Test
    public void testMaskPasswordInUrlWithSpecialCharacters() {
        // URL with special characters in password (but not @, which would be URL-encoded)
        String urlWithSpecialChars = "https://user:p!ssw0rd#@host/repo.git";
        String masked = GitUtil.maskPasswordInUrl(urlWithSpecialChars);
        assertEquals("Password with special characters should be masked",
                "https://user:********@host/repo.git", masked);
    }

    @Test
    public void testMaskPasswordInUrlSshWithPort() {
        // SSH URL with port
        String sshUrlWithPort = "ssh://user:password@host:22/repo.git";
        String masked = GitUtil.maskPasswordInUrl(sshUrlWithPort);
        assertEquals("Password should be masked in SSH URL with port",
                "ssh://user:********@host:22/repo.git", masked);
    }

    @Test
    public void testMaskPasswordInUrlHttpWithPort() {
        // HTTP URL with port
        String httpUrlWithPort = "http://user:password@host:80/repo.git";
        String masked = GitUtil.maskPasswordInUrl(httpUrlWithPort);
        assertEquals("Password should be masked in HTTP URL with port",
                "http://user:********@host:80/repo.git", masked);
    }
}
