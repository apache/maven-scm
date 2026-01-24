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
package org.apache.maven.scm.tck.command.remoteinfo;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.junit.jupiter.api.Test;

/**
 * This test tests the remote info command.
 * <p>
 * This test have to be subclassed. The expected result have to be implemented in
 * sub class
 * <p>
 *
 * @author Bertrand Paquet
 */
public abstract class AbstractRemoteInfoCommandTckTest extends ScmTckTestCase {

    protected abstract void checkResult(RemoteInfoScmResult result);

    protected abstract ScmProviderRepository getScmProviderRepository() throws Exception;

    @Test
    void testRemoteInfoCommand() throws Exception {
        ScmProvider provider = getScmManager().getProviderByRepository(getScmRepository());
        RemoteInfoScmResult result =
                provider.remoteInfo(getScmProviderRepository(), new ScmFileSet(getWorkingCopy()), null);

        checkResult(result);
    }
}
