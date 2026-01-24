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
package org.apache.maven.scm.provider.hg.repository;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HgScmProviderRepositoryTest {

    //    public void testInvalidRepo()
    //    {
    //        //No protocol - makes it invalid
    //        String url = "username:password@myhost.com/~/dev/maven";
    //        HgScmProviderRepository repo = new HgScmProviderRepository( url );
    //        assertNotNull( repo.validateURI() );
    //    }

    @Test
    void testFileRepo() {
        // 1. Test *nix like paths
        String url = "/home/username/dev/maven";
        HgScmProviderRepository repo = new HgScmProviderRepository(url);
        assertNull(repo.validateURI());

        // 2. Test windows like paths (with slash)
        url = "C:/Documents and Settings/username/dev/maven";
        repo = new HgScmProviderRepository(url);
        assertNull(repo.validateURI());

        // 3. Test windows like paths (with backslash)
        url = "C:\\Documents and Settings\\username\\dev\\maven";
        repo = new HgScmProviderRepository(url);
        assertNull(repo.validateURI());

        //        //4. Test invalid file url
        //        url = "file:/C:\\Documents and Settings\\username\\dev\\maven";
        //        repo = new HgScmProviderRepository( url );
        //        assertNotNull( repo.validateURI() );
    }

    @Test
    void testSSHRepo() {
        // todo: check assert
        // 1. Test with relativ path
        String url = "ssh://username:password@myhost.com/~/dev/maven";
        HgScmProviderRepository repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());
        assertNull(repo.validateURI());

        // 2. Test with absolute path
        url = "ssh://username:password@myhost.com/home/username/dev/maven";
        repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());
        assertNull(repo.validateURI());

        // 3. Test with passwordless (Public-key auth)
        String incompleteUrl = "ssh://username@myhost.com/home/username/dev/maven";
        repo = new HgScmProviderRepository(incompleteUrl);
        assertEquals(incompleteUrl, repo.getURI());
        assertNull(repo.validateURI());
    }

    @Test
    void testHTTPRepo() {
        // todo: check assert
        // 1. Test with relativ path
        String url = "http://www.myhost.com/~username/dev/maven";
        HgScmProviderRepository repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());
        assertNull(repo.validateURI());

        // 2. Test with absolute path
        url = "http://www.myhost.com/dev/maven";
        repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());
        assertNull(repo.validateURI());

        // 3. Test with authentication information
        repo.setPassword("Password");
        repo.setUser("User");
        repo.setPassphrase("Passphrase");
        assertEquals("http://User:Password@www.myhost.com/dev/maven", repo.getURI());
        assertNull(repo.validateURI());
        repo.setPort(81);
        assertEquals("http://User:Password@www.myhost.com:81/dev/maven", repo.getURI());
        assertNull(repo.validateURI());
        assertTrue(true);
    }

    @Test
    void testHTTPRepoWithHgInUrl() {
        String url = "http://hg/hg/maven";
        HgScmProviderRepository repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());
        assertNull(repo.validateURI());
    }

    /**
     * Test SCM-391
     *
     * @throws Exception
     */
    @Test
    void testParseHostAndPort() throws Exception {
        String url = "http://localhost:8000/";
        HgScmProviderRepository repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());

        url = "http://localhost/";
        repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());

        url = "http://www.myhost.com:81/dev/maven";
        repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());
    }

    /**
     * Test SCM-431
     *
     * @throws Exception
     */
    @Test
    void testParseBasicAuth() throws Exception {
        String url = "http://a:b@localhost:8000/";
        HgScmProviderRepository repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());

        url = "http://aa@localhost/";
        repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());

        url = "http://SCM:431@www.myhost.com:81/dev/maven";
        repo = new HgScmProviderRepository(url);
        assertEquals(url, repo.getURI());
    }
}
