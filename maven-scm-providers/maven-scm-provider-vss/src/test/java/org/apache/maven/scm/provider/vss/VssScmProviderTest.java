package org.apache.maven.scm.provider.vss;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class VssScmProviderTest
    extends ScmTestCase
{
    private ScmManager scmManager;

    public void setUp()
        throws Exception
    {
        super.setUp();

        scmManager = getScmManager();
    }

    public void testScmUrl()
        throws Exception
    {
        testUrl( "scm:vss|vssdir|projectPath", "vssdir", "projectPath", null, null );
        testUrl( "scm:vss|username@vssdir|projectPath", "vssdir", "projectPath", "username", null );
        testUrl( "scm:vss|username|password@vssdir|projectPath", "vssdir", "projectPath", "username", "password" );
    }

    private void testUrl( String scmUrl, String expectedVssDir, String expectedProjectPath, String expectedUser,
                          String expectedPassword )
        throws Exception
    {
        ScmRepository repository = scmManager.makeScmRepository( scmUrl );

        assertNotNull( "ScmManager.makeScmRepository() returned null", repository );

        assertNotNull( "The provider repository was null.", repository.getProviderRepository() );

        assertTrue( "The SCM Repository isn't a " + VssScmProviderRepository.class.getName() + ".",
                    repository.getProviderRepository() instanceof VssScmProviderRepository );

        VssScmProviderRepository providerRepository = (VssScmProviderRepository) repository.getProviderRepository();

        assertEquals( "vssdir is incorrect", expectedVssDir, providerRepository.getVssdir() );

        assertEquals( "projectPath is incorrect", expectedProjectPath, providerRepository.getProject() );

        assertEquals( "User is incorrect", expectedUser, providerRepository.getUser() );

        assertEquals( "Password is incorrect", expectedPassword, providerRepository.getPassword() );
    }
}
