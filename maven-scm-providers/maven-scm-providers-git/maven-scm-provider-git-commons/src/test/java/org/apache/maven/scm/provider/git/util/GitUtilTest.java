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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GitUtilTest {

    @Test
    void scpStyleUrlHasNoPassword() {
        assertEquals("git@github.com:olamy/repo.git", GitUtil.maskPasswordInUrl("git@github.com:olamy/repo.git"));
    }

    @Test
    void scpStyleUrlWithAtInPathIsUnchanged() {
        assertEquals(
                "git@github.com:olamy/workspace@2/repo.git",
                GitUtil.maskPasswordInUrl("git@github.com:olamy/workspace@2/repo.git"));
    }

    @Test
    void sshUrlWithPortAndAtInPathIsUnchanged() {
        assertEquals(
                "ssh://git@github.com:2222/olamy/workspace@2/repo.git",
                GitUtil.maskPasswordInUrl("ssh://git@github.com:2222/olamy/workspace@2/repo.git"));
    }

    @Test
    void passwordInAuthorityIsMasked() {
        assertEquals(
                "https://user:********@github.com/olamy/repo.git",
                GitUtil.maskPasswordInUrl("https://user:secret@github.com/olamy/repo.git"));
    }

    @Test
    void passwordInAuthorityIsMaskedWhenPathContainsAt() {
        assertEquals(
                "https://user:********@github.com/olamy/workspace@2/repo.git",
                GitUtil.maskPasswordInUrl("https://user:secret@github.com/olamy/workspace@2/repo.git"));
    }

    @Test
    void userNameContainingAtKeepsHostAndPort() {
        assertEquals(
                "http://username@site.com:********@gitrepos.apache.org:8800/pmgt/trunk",
                GitUtil.maskPasswordInUrl("http://username@site.com:password@gitrepos.apache.org:8800/pmgt/trunk"));
    }

    @Test
    void passwordInCommandLineIsMasked() {
        assertEquals(
                "git push https://user:********@foo.com/git/trunk refs/tags/my-tag-1",
                GitUtil.maskPasswordInUrl("git push https://user:password@foo.com/git/trunk refs/tags/my-tag-1"));
    }

    @Test
    void testPasswordInScpStyleUrlIsMasked() {
        assertEquals("user:********@host:path/repo.git", GitUtil.maskPasswordInUrl("user:password@host:path/repo.git"));
        assertEquals("user:********@host:repo.git", GitUtil.maskPasswordInUrl("user:password@host:repo.git"));
    }

    @Test
    void testScpStyleUrlWithoutPasswordIsNotChanged() {
        assertEquals("git@github.com:repo.git", GitUtil.maskPasswordInUrl("git@github.com:repo.git"));
        assertEquals("host:path/repo.git", GitUtil.maskPasswordInUrl("host:path/repo.git"));
    }

    @Test
    void testPasswordInCommandLineIsMasked() {
        assertEquals(
                "git clone user:********@host:r.git dir",
                GitUtil.maskPasswordInUrl("git clone user:secret@host:r.git dir"));
        assertEquals(
                "/bin/sh -c 'git' 'clone' 'user:********@host:path/r.git' 'dir'",
                GitUtil.maskPasswordInUrl("/bin/sh -c 'git' 'clone' 'user:secret@host:path/r.git' 'dir'"));
        assertEquals(
                "/bin/sh -c 'git' 'push' 'https://user:********@host/x.git'",
                GitUtil.maskPasswordInUrl("/bin/sh -c 'git' 'push' 'https://user:pa'\"'\"'ss@host/x.git'"));
    }

    @Test
    void testPasswordBeforePortIsMaskedNotThePath() {
        assertEquals(
                "https://user:********@host:8443/x@y", GitUtil.maskPasswordInUrl("https://user:secret@host:8443/x@y"));
    }
}
