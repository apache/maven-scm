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
        testUrl( "scm:git:file:///tmp/repo", null, "file:///tmp/repo", null, null, null, null, 0 );
    }

    public void testLegalFileHomeURL()
    throws Exception
    {
        testUrl( "scm:git:file://~/repo", null, "file://~/repo", null, null, null, null, 0 );
    }

    public void testLegalSshHomeURL()
    throws Exception
    {
        testUrl( "scm:git:ssh://~/repo", null, "ssh://~/repo", null, null, null, null, 0 );
    }

    public void testLegalLocalhostFileURL()
        throws Exception
    {
        testUrl( "scm:git:file://localhost/tmp/repo", null, "file://localhost/tmp/repo", null, null, null, "localhost", 0 );
    }

    public void testLegalHistnameFileURL()
        throws Exception
    {
        testUrl( "scm:git:file://my_server/tmp/repo", null, "file://my_server/tmp/repo", null, null, null, "my_server", 0 );
    }

    public void testLegalHttpURL()
        throws Exception
    {
        testUrl( "scm:git:http://gitrepos.apache.org", null, "http://gitrepos.apache.org", null, null,
                 null, "gitrepos.apache.org", 0 );
    }

    public void testLegalHttpURLWithUser()
        throws Exception
    {
        testUrl( "scm:git:http://user@gitrepos.apache.org", null, "http://user@gitrepos.apache.org", null, "user",
                 null, "gitrepos.apache.org", 0 );
    }

    public void testLegalHttpURLWithUserPassword()
        throws Exception
    {
        testUrl( "scm:git:http://user:password@gitrepos.apache.org", null, "http://user:password@gitrepos.apache.org",
                 null, "user", "password", "gitrepos.apache.org", 0 );
    }

    public void testLegalHttpsURL()
        throws Exception
    {
        testUrl( "scm:git:https://gitrepos.apache.org", null, "https://gitrepos.apache.org", null, null,
                 null, "gitrepos.apache.org", 0 );
    }

    public void testLegalHttpsURLWithUser()
        throws Exception
    {
        testUrl( "scm:git:https://user@gitrepos.apache.org", null, "https://user@gitrepos.apache.org", null, "user",
                 null, "gitrepos.apache.org", 0 );
    }
        
    public void testLegalHttpsURLWithUserPassword()
        throws Exception
    {
        testUrl( "scm:git:https://user:password@gitrepos.apache.org", null, "https://user:password@gitrepos.apache.org",
                 null, "user", "password", "gitrepos.apache.org", 0 );
    }

    public void testLegalSshURLWithUser()
    throws Exception
    {
        testUrl( "scm:git:ssh://user@gitrepos.apache.org", null, "ssh://user@gitrepos.apache.org", null, "user",
                 null, "gitrepos.apache.org", 0 );
    }

    public void testLegalSshURLWithUserPassword()
    throws Exception
    {
        testUrl( "scm:git:ssh://user:password@gitrepos.apache.org", null, "ssh://user:password@gitrepos.apache.org",  
                 null, "user", "password", "gitrepos.apache.org", 0 );
    }
    
    public void testLegalGitURL()
        throws Exception
    {
        testUrl( "scm:git:git://gitrepos.apache.org", null, "git://gitrepos.apache.org", null, null,
                 null, "gitrepos.apache.org", 0 );
    }
    
    public void testGitDevURL()
        throws Exception, ScmRepositoryException
    {
        testUrl( "scm:git:git@github.com:olamy/scm-git-test-one-module.git",
                 null, "git@github.com:olamy/scm-git-test-one-module.git", null, "git" , null, "github.com", 0 );
    }

    public void testGitDevURLWIthPort()
        throws Exception, ScmRepositoryException
    {
        testUrl( "scm:git:git@github.com:222:olamy/scm-git-test-one-module.git",
                 null, "git@github.com:222:olamy/scm-git-test-one-module.git", null, "git", null, "github.com", 222 );
    }     

    
    
    public void testLegalGitPortUrl()
        throws Exception
    {
        testUrl( "scm:git:http://username@gitrepos.apache.org:8800/pmgt/trunk",
                 null, "http://username@gitrepos.apache.org:8800/pmgt/trunk",
                 null, "username", null, "gitrepos.apache.org", 8800 );

        testUrl( "scm:git:https://username@gitrepos.apache.org:20443/pmgt/trunk",
                 null, "https://username@gitrepos.apache.org:20443/pmgt/trunk",
                 null, "username", null, "gitrepos.apache.org", 20443 );
        
        testUrl( "scm:git:git://username@gitrepos.apache.org:8800/pmgt/trunk",
                 null, "git://username@gitrepos.apache.org:8800/pmgt/trunk", 
                 null, "username", null, "gitrepos.apache.org", 8800 );
        
        testUrl( "scm:git:ssh://username@gitrepos.apache.org:8080/pmgt/trunk",
                 null, "ssh://username@gitrepos.apache.org:8080/pmgt/trunk", 
                 null, "username", null, "gitrepos.apache.org", 8080 );

        testUrl( "scm:git:ssh://username:password@gitrepos.apache.org/pmgt/trunk",
                 null, "ssh://username:password@gitrepos.apache.org/pmgt/trunk", 
                 null, "username", "password", "gitrepos.apache.org", 0 );
    }

    // ----------------------------------------------------------------------
    // the following tests are for combined fetch + push URLs
    // ----------------------------------------------------------------------

    public void testHttpFetchSshPushUrl()
        throws Exception
    {
        testUrl( "scm:git:[fetch=]http://git.apache.org/myprj.git[push=]ssh://myuser:mypassword@git.apache.org/~/myrepo/myprj.git", 
                 "[fetch=]http://myuser:mypassword@git.apache.org/myprj.git[push=]ssh://myuser:mypassword@git.apache.org/~/myrepo/myprj.git",
                 "http://myuser:mypassword@git.apache.org/myprj.git",
                 "ssh://myuser:mypassword@git.apache.org/~/myrepo/myprj.git", "myuser", "mypassword", "git.apache.org", 0 );

        testUrl( "scm:git:[push=]ssh://myuser:mypassword@git.apache.org/~/myrepo/myprj.git[fetch=]http://git.apache.org/myprj.git", 
                 "[fetch=]http://myuser:mypassword@git.apache.org/myprj.git[push=]ssh://myuser:mypassword@git.apache.org/~/myrepo/myprj.git",
                 "http://myuser:mypassword@git.apache.org/myprj.git",
                 "ssh://myuser:mypassword@git.apache.org/~/myrepo/myprj.git", "myuser", "mypassword", "git.apache.org", 0 );
    }
    
    // ----------------------------------------------------------------------
    // Testing illegal URLs
    // ----------------------------------------------------------------------

    //X in fact this url is perfectly valid from a technical perspective
    //X it will be interpreted by git as git://file/tmp/git
    public void nottestIllegalFileUrl()
        throws Exception
    {
        testIllegalUrl( "file:/tmp/git" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private GitScmProviderRepository testUrl( String scmUrl, String expectedToString, 
                                              String expectedFetchUrl, String expectedPushUrl,
                                              String expectedUser, String expectedPassword, 
                                              String expectedHost, int expectedPort )
        throws Exception, ScmRepositoryException
    {
        ScmRepository repository = scmManager.makeScmRepository( scmUrl );

        assertNotNull( "ScmManager.makeScmRepository() returned null", repository );

        assertNotNull( "The provider repository was null.", repository.getProviderRepository() );

        assertTrue( "The SCM Repository isn't a " + GitScmProviderRepository.class.getName() + ".", repository
            .getProviderRepository() instanceof GitScmProviderRepository );

        GitScmProviderRepository providerRepository = (GitScmProviderRepository) repository.getProviderRepository();

        assertEquals( "fetch url is incorrect", expectedFetchUrl, providerRepository.getFetchUrl() );
        
        if ( expectedPushUrl != null )
        {
            assertEquals( "push url is incorrect", expectedPushUrl, providerRepository.getPushUrl() );
        }

        if ( expectedToString != null )
        {
            assertEquals( "toString is incorrect", "git:" + expectedToString, repository.toString() );
        }
        else
        {
            assertEquals( "toString is incorrect", "git:" + expectedFetchUrl, repository.toString() );
        }

        assertEquals( "User is incorrect", expectedUser, providerRepository.getUser() );

        assertEquals( "Password is incorrect", expectedPassword, providerRepository.getPassword() );

        assertEquals( "Host is incorrect", expectedHost == null ? "" : expectedHost, providerRepository.getHost() );
        
        if ( expectedPort > 0 )
        {
            assertEquals( "Port is incorrect", expectedPort, providerRepository.getPort() );
        }

        return providerRepository;
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

    public void testGetParent() throws Exception
    {
        new GitScmProviderRepository( "http://gitrepos.apache.org" );
    }

}
