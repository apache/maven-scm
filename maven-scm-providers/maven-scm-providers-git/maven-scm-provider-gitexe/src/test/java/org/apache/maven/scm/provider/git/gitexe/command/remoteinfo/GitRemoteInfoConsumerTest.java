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
package org.apache.maven.scm.provider.git.gitexe.command.remoteinfo;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author Bertrand Paquet
 */
public class GitRemoteInfoConsumerTest extends ScmTestCase {

    @Test
    public void testConsumerRemoteInfo() {
        GitRemoteInfoConsumer consumer = new GitRemoteInfoConsumer(null);

        consumer.consumeLine("344581899752998038a74989142221ae08c381bc	HEAD");
        consumer.consumeLine("344581899752998038a74989142221ae08c381bc	refs/heads/master");
        consumer.consumeLine("9006c3dbaa9749aa435694f261638583c9088419	refs/tags/staging");

        RemoteInfoScmResult remoteInfoScmResult = consumer.getRemoteInfoScmResult();

        assertEquals(1, remoteInfoScmResult.getBranches().size());
        assertEquals(
                "344581899752998038a74989142221ae08c381bc",
                remoteInfoScmResult.getBranches().get("master"));

        assertEquals(1, remoteInfoScmResult.getTags().size());
        assertEquals(
                "9006c3dbaa9749aa435694f261638583c9088419",
                remoteInfoScmResult.getTags().get("staging"));
    }
}
