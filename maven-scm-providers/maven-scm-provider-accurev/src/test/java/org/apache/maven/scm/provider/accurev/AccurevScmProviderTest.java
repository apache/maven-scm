package org.apache.maven.scm.provider.accurev;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepositoryMatcher.isRepo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.junit.Test;

public class AccurevScmProviderTest
{

    @Test
    public void testMakeProviderScmRepository()
        throws Exception
    {

        // [:stream][:/project/dir]
        assertAccurevRepo( "", null, null );
        assertAccurevRepo( "aStream:/project/dir", "aStream", "project/dir" );
        assertAccurevRepo( "/project/dir", null, "project/dir" );
        assertAccurevRepo( "my_QA_Stream", "my_QA_Stream", null );

    }

    private static void assertAccurevRepo( String url, String expStream, String expPath )
        throws ScmRepositoryException
    {
        AccuRevScmProvider provider = new AccuRevScmProvider();

        AccuRevScmProviderRepository repository = (AccuRevScmProviderRepository) provider
            .makeProviderScmRepository( url, ':' );

        assertThat( repository,
                    isRepo( null, null, null, AccuRevScmProviderRepository.DEFAULT_PORT, expStream, expPath ) );

    }

    @Test
    public void testMakeProviderWithBothKindsOfDirectorySeparators()
        throws ScmRepositoryException
    {
        assertThat( getRepo( "aStream:\\project\\dir" ), isRepo( null, null, null, 5050, "aStream", "project\\dir" ) );
    }

    @Test
    public void testProviderWithHostPort()
        throws Exception
    {

        assertThat( getRepo( "@myHost:aStream:/project/dir" ), isRepo( null, null, "myHost",
                                                                       AccuRevScmProviderRepository.DEFAULT_PORT,
                                                                       "aStream", "project/dir" ) );
        assertThat( getRepo( "@myHost:5051:/project/dir" ), isRepo( null, null, "myHost", 5051, null, "project/dir" ) );
    }

    @Test
    public void testBlankAsUsedInTckTests()
        throws ScmRepositoryException
    {
        assertThat( getRepo( ":aDepotStream" ), isRepo( null, null, null, 5050, "aDepotStream", null ) );
    }

    @Test
    public void testProviderWithUserPass()
        throws Exception
    {
        assertThat( getRepo( "aUser/theirPassword:/project/dir" ), isRepo( "aUser", "theirPassword", null,
                                                                           AccuRevScmProviderRepository.DEFAULT_PORT,
                                                                           null, "project/dir" ) );

        assertThat( getRepo( "aUser/theirPassword@theHost:5051:aStream:/project/dir" ), isRepo( "aUser",
                                                                                                "theirPassword",
                                                                                                "theHost", 5051,
                                                                                                "aStream",
                                                                                                "project/dir" ) );

        assertThat( getRepo( "aUser@theHost:5050:aStream" ), isRepo( "aUser", null, "theHost", 5050, "aStream", null ) );

        assertThat( getRepo( "aUser/" ), isRepo( "aUser", null, null, 5050, null, null ) );
    }

    private static ScmProviderRepository getRepo( String url )
        throws ScmRepositoryException
    {
        ScmProviderRepository repo = new AccuRevScmProvider().makeProviderScmRepository( url, ':' );
        return repo;
    }

    @Test
    public void testGetSCMType()
    {

        assertThat( ( new AccuRevScmProvider() ).getScmType(), is( "accurev" ) );
    }
}
