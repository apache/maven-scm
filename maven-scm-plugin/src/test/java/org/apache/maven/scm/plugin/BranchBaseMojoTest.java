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
package org.apache.maven.scm.plugin;

import org.apache.maven.api.plugin.testing.InjectMojo;
import org.apache.maven.api.plugin.testing.MojoTest;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MojoTest
class BranchBaseMojoTest {

    @Test
    @InjectMojo(goal = "branch", pom = "classpath:/mojos/branch-base/branchBase.xml")
    void testBranchBaseAndTagBaseAreApplied(BranchMojo mojo) throws Exception {
        ScmRepository repository = mojo.getScmRepository();
        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        assertEquals("http://svn.example.org/repos/project/stable", svnRepository.getBranchBase());
        assertEquals("http://svn.example.org/repos/project/releases", svnRepository.getTagBase());
    }
}
