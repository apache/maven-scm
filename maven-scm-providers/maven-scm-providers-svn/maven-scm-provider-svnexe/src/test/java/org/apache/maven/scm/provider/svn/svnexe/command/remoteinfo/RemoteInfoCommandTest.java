package org.apache.maven.scm.provider.svn.svnexe.command.remoteinfo;
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

import org.apache.maven.scm.command.remoteinfo.RemoteInfoScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.codehaus.plexus.PlexusTestCase;

/**
 * @author Olivier Lamy
 */
public class RemoteInfoCommandTest
    extends PlexusTestCase
{

    public void testRemoteInfoCommand()
        throws Exception
    {
        ScmProvider svnProvider = (ScmProvider) lookup( ScmProvider.ROLE, "svn" );
        SvnScmProviderRepository repository =
            new SvnScmProviderRepository( "http://svn.apache.org/repos/asf/maven/maven-3/trunk" );
        RemoteInfoScmResult remoteInfoScmResult = svnProvider.remoteInfo( repository, null, null );
        assertTrue( remoteInfoScmResult.getTags().keySet().contains( "maven-3.0" ) );
        // no test on branches as can be removed
    }
}
