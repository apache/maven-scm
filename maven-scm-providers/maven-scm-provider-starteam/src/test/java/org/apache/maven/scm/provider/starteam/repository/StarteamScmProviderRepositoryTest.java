package org.apache.maven.scm.provider.starteam.repository;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class StarteamScmProviderRepositoryTest
    extends ScmTestCase
{
    public void testParseConnection()
        throws Exception
    {
        StarteamScmProviderRepository repo = testUrl( "scm:starteam:myhost:1234/projecturl" );

        assertNull( repo.getUser() );

        assertNull( repo.getPassword() );

        assertEquals( "myhost:1234/projecturl", repo.getUrl() );
    }

    public void testParseConnectionWithUsername()
        throws Exception
    {
        StarteamScmProviderRepository repo = testUrl( "scm:starteam:myusername@myhost:1234/projecturl" );

        assertEquals( "myusername", repo.getUser() );

        assertNull( repo.getPassword() );

        assertEquals( "myhost:1234/projecturl", repo.getUrl() );
   }

    public void testParseConnectionWithUsernameAndPassword()
        throws Exception
    {
        StarteamScmProviderRepository repo = testUrl( "scm:starteam:myusername@myhost:1234/projecturl" );

        assertEquals( "myusername", repo.getUser() );

        assertNull( repo.getPassword() );

        assertEquals( "myhost:1234/projecturl", repo.getUrl() );    }

    public void testInvalidConnection()
        throws Exception
    {
        testIllegalUrl( "scm:starteam:invalidConnectionString" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private StarteamScmProviderRepository testUrl( String url )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( url );

        assertNotNull( "ScmManager.makeScmRepository() returned null", repository );

        assertNotNull( "The provider repository was null.", repository.getProviderRepository() );

        assertTrue( "The SCM Repository isn't a " + StarteamScmProviderRepository.class.getName() + ".",
                    repository.getProviderRepository() instanceof StarteamScmProviderRepository );

        return (StarteamScmProviderRepository) repository.getProviderRepository();
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
