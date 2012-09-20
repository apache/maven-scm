package org.apache.maven.scm.provider.starteam.repository;

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
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class StarteamScmProviderRepositoryTest
    extends ScmTestCase
{
    public void testParseConnection()
        throws Exception
    {
        testUrl( "scm:starteam:myhost:1234/projecturl", null, null, "myhost", 1234, "/projecturl" );
    }

    public void testParseConnectionWithUsername()
        throws Exception
    {
        testUrl( "scm:starteam:myusername@myhost:1234/projecturl", "myusername", null, "myhost", 1234, "/projecturl" );
    }

    public void testParseConnectionWithUsernameAndPassword()
        throws Exception
    {
        testUrl( "scm:starteam:myusername:mypassword@myhost:1234/projecturl", "myusername", "mypassword", "myhost",
                 1234, "/projecturl" );
    }

    public void testParseConnection2()
        throws Exception
    {
        testUrl( "scm:starteam:myhost:1234:/projecturl", null, null, "myhost", 1234, "/projecturl" );
    }

    public void testParseConnectionWithUsername2()
        throws Exception
    {
        testUrl( "scm:starteam:myusername@myhost:1234:/projecturl", "myusername", null, "myhost", 1234, "/projecturl" );
    }

    public void testParseConnectionWithUsernameAndPassword2()
        throws Exception
    {
        testUrl( "scm:starteam:myusername:mypassword@myhost:1234:/projecturl", "myusername", "mypassword", "myhost",
                 1234, "/projecturl" );
    }

    public void testInvalidConnection()
        throws Exception
    {
        testIllegalUrl( "scm:starteam:invalidConnectionString" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testUrl( String url, String expectedUser, String expectedPassword, String expectedHost,
                          int expectedPort, String expectedPath )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( url );

        assertNotNull( "ScmManager.makeScmRepository() returned null", repository );

        assertNotNull( "The provider repository was null.", repository.getProviderRepository() );

        assertTrue( "The SCM Repository isn't a " + StarteamScmProviderRepository.class.getName() + ".", repository
            .getProviderRepository() instanceof StarteamScmProviderRepository );

        StarteamScmProviderRepository repo = (StarteamScmProviderRepository) repository.getProviderRepository();

        assertEquals( expectedUser, repo.getUser() );

        assertEquals( expectedPassword, repo.getPassword() );

        assertEquals( expectedHost, repo.getHost() );

        assertEquals( expectedPort, repo.getPort() );

        assertEquals( expectedPath, repo.getPath() );
    }

    private void testIllegalUrl( String url )
        throws Exception
    {
        try
        {
            getScmManager().makeScmRepository( url );

            fail( "Expected a ScmRepositoryException while testing the url '" + url + "'." );
        }
        catch ( ScmRepositoryException e )
        {
            // expected
        }
    }
}
