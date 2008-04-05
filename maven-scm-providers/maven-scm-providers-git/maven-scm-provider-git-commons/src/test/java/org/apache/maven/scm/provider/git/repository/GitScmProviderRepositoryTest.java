package org.apache.maven.scm.provider.git.repository;

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
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class GitScmProviderRepositoryTest
    extends ScmTestCase
{
    private ScmManager scmManager;

    public void setUp()
        throws Exception
    {
        super.setUp();

        scmManager = getScmManager();
    }

    // ----------------------------------------------------------------------
    // Testing legal URLs
    // ----------------------------------------------------------------------

    public void testLegalFileURL()
        throws Exception
    {
        testUrl( "scm:git:file:///tmp/repo", "file:///tmp/repo", null, null, null );
    }

    public void testLegalLocalhostFileURL()
        throws Exception
    {
        testUrl( "scm:git:file://localhost/tmp/repo", "file://localhost/tmp/repo", null, null, null );
    }

    public void testLegalHistnameFileURL()
        throws Exception
    {
        testUrl( "scm:git:file://my_server/tmp/repo", "file://my_server/tmp/repo", null, null, null );
    }

    public void testLegalHttpURL()
        throws Exception
    {
        testUrl( "scm:git:http://gitrepos.apache.org", "http://gitrepos.apache.org", null, null,
                 "gitrepos.apache.org" );
    }

    public void testLegalHttpURLWithUser()
        throws Exception
    {
        testUrl( "scm:git:http://user@gitrepos.apache.org", "http://gitrepos.apache.org", "user", null,
                 "gitrepos.apache.org" );
    }

    public void testLegalHttpURLWithUserPassword()
        throws Exception
    {
        testUrl( "scm:git:http://user:password@gitrepos.apache.org", "http://gitrepos.apache.org", "user",
                 "password", "gitrepos.apache.org" );
    }

    public void testLegalHttpsURL()
        throws Exception
    {
        testUrl( "scm:git:https://gitrepos.apache.org", "https://gitrepos.apache.org", null, null,
                 "gitrepos.apache.org" );
    }

    public void testLegalHttpsURLWithUser()
        throws Exception
    {
        testUrl( "scm:git:https://user@gitrepos.apache.org", "https://gitrepos.apache.org", "user", null,
                 "gitrepos.apache.org" );
    }
        
    public void testLegalHttpsURLWithUserPassword()
        throws Exception
    {
        testUrl( "scm:git:https://user:password@gitrepos.apache.org", "https://gitrepos.apache.org", "user",
                 "password", "gitrepos.apache.org" );
    }

    public void testLegalSshURLWithUser()
    throws Exception
    {
        testUrl( "scm:git:ssh://user@gitrepos.apache.org", "ssh://user@gitrepos.apache.org", "user", null,
                 "gitrepos.apache.org" );
    }

    public void testLegalSshURLWithUserPassword()
    throws Exception
    {
        testUrl( "scm:git:ssh://user:password@gitrepos.apache.org", "ssh://user:password@gitrepos.apache.org", "user",
                 "password", "gitrepos.apache.org" );
    }
    
    public void testLegalGitURL()
        throws Exception
    {
        testUrl( "scm:git:git://gitrepos.apache.org", "git://gitrepos.apache.org", null, null,
                 "gitrepos.apache.org" );
    }

    public void testLegalGitPortUrl()
        throws Exception
    {
        testUrl( "scm:git:http://username@gitrepos.apache.org:8800/pmgt/trunk",
                 "http://gitrepos.apache.org:8800/pmgt/trunk", "username", "gitrepos.apache.org", 8800 );

        testUrl( "scm:git:https://username@gitrepos.apache.org:20443/pmgt/trunk",
                 "https://gitrepos.apache.org:20443/pmgt/trunk", "username", "gitrepos.apache.org", 8080 );
        
        testUrl( "scm:git:git://username@gitrepos.apache.org:8800/pmgt/trunk",
                 "git://gitrepos.apache.org:8800/pmgt/trunk", "username", "gitrepos.apache.org", 8800 );
        
        testUrl( "scm:git:ssh://username@gitrepos.apache.org:8080/pmgt/trunk",
                 "ssh://username@gitrepos.apache.org:8080/pmgt/trunk", "username", "gitrepos.apache.org", 8080 );

        testUrl( "scm:git:ssh://username:password@gitrepos.apache.org/pmgt/trunk",
                 "ssh://username:password@gitrepos.apache.org/pmgt/trunk", 
                 "username", "password", "gitrepos.apache.org" );
    }

    // ----------------------------------------------------------------------
    // Testing illegal URLs
    // ----------------------------------------------------------------------

    public void testIllegalFileUrl()
        throws Exception
    {
        testIllegalUrl( "file:/tmp/git" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testUrl( String scmUrl, String expectedUrl, String expectedUser, String expectedPassword,
                          String expectedHost )
        throws Exception
    {
        ScmRepository repository = scmManager.makeScmRepository( scmUrl );

        assertNotNull( "ScmManager.makeScmRepository() returned null", repository );

        assertNotNull( "The provider repository was null.", repository.getProviderRepository() );

        assertTrue( "The SCM Repository isn't a " + GitScmProviderRepository.class.getName() + ".",
                    repository.getProviderRepository() instanceof GitScmProviderRepository );

        GitScmProviderRepository providerRepository = (GitScmProviderRepository) repository.getProviderRepository();

        assertEquals( "url is incorrect", expectedUrl, providerRepository.getUrl() );

        assertEquals( "url string is incorrect", "git:" + expectedUrl, repository.toString() );

        assertEquals( "User is incorrect", expectedUser, providerRepository.getUser() );

        assertEquals( "Password is incorrect", expectedPassword, providerRepository.getPassword() );

        assertEquals( "Host is incorrect", expectedHost,
                      ( (GitScmProviderRepository) repository.getProviderRepository() ).getHost() );
    }

    private void testUrl( String scmUrl, String expectedUrl, String expectedUser, String expectedHost,
                          int expectedPort )
        throws Exception
    {
        testUrl( scmUrl, expectedUrl, expectedUser, null, expectedHost );
    }

    private void testIllegalUrl( String url )
        throws Exception
    {
        try
        {
            scmManager.makeScmRepository( "scm:git:" + url );

            fail( "Expected a ScmRepositoryException while testing the url '" + url + "'." );
        }
        catch ( ScmRepositoryException e )
        {
            // expected
        }
    }

    public void testGetParent()
    {
        new GitScmProviderRepository( "http://gitrepos.apache.org" );
    }

}
