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
package org.apache.maven.scm.provider.hg;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HgScmProviderTest {

    private final HgScmProvider provider = new HgScmProvider();

    @Test
    void testInvalidFileUrlMessage() {
        String expected = "An hg 'file' URL must begin with 'file:///' or 'file://localhost/'.";
        assertEquals(Collections.singletonList(expected), provider.validateScmUrl("file:/tmp/repo", ':'));
    }

    @Test
    void testInvalidHttpsUrlMessage() {
        String expected = "An hg 'https' URL must begin with 'https://'.";
        assertEquals(Collections.singletonList(expected), provider.validateScmUrl("https:/example.com/repo", ':'));
    }

    @Test
    void testInvalidHttpUrlMessage() {
        String expected = "An hg 'http' URL must begin with 'http://'.";
        assertEquals(Collections.singletonList(expected), provider.validateScmUrl("http:/example.com/repo", ':'));
    }

    @Test
    void testValidUrlsHaveNoMessages() {
        List<String> messages = provider.validateScmUrl("https://example.com/repo", ':');
        assertTrue(messages.isEmpty(), messages.toString());
    }
}
