package org.apache.maven.scm.provider.svn.svnjava.repository;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.provider.svn.svnjava.SvnJavaScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:dh-maven@famhq.com">David Hawkins</a>
 * @version $Id$
 */
public class SvnScmProviderRepositoryTest
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

    public void testFileURLNotSupported()
        throws Exception
    {
        testUrl( "scm:svn:file:///tmp/repo", "file:///tmp/repo", null );
    }

    public void testLocalhostFileURLNotSupported()
        throws Exception
    {
        testUrl( "scm:svn:file://localhost/tmp/repo", "file://localhost/tmp/repo", null );
    }

    public void testLocalhostFileURLNotSupportedBis()
        throws Exception
    {
        testUrl( "scm:svn:file://toto/tmp/repo", "file://toto/tmp/repo", null );
    }

    public void testLegalHttpURL()
        throws Exception
    {
        testUrl( "scm:svn:http://subversion.tigris.org", "http://subversion.tigris.org", null );
    }

    public void testLegalHttpsURL()
        throws Exception
    {
        testUrl( "scm:svn:https://subversion.tigris.org", "https://subversion.tigris.org", null );
    }

    public void testLegalSvnURL()
        throws Exception
    {
        testUrl( "scm:svn:svn://subversion.tigris.org", "svn://subversion.tigris.org", null );
    }

    public void testLegalSvnPlusUsernameURL()
        throws Exception
    {
        testUrl( "scm:svn:svn://username@subversion.tigris.org", "svn://subversion.tigris.org", "username" );
    }

    public void testLegalSvnPlusSshURL()
        throws Exception
    {
        testUrl( "scm:svn:svn+ssh://subversion.tigris.org", "svn+ssh://subversion.tigris.org", null );
    }

    public void testLegalSvnPlusSshPlusUsernameURL()
        throws Exception
    {
        // This is a change from the command line svn implementation. JavaSVN will strip the username
        // from the url because it is not needed.
        testUrl( "scm:svn:svn+ssh://username@subversion.tigris.org", "svn+ssh://subversion.tigris.org", "username" );
    }

    // ----------------------------------------------------------------------
    // Testing illegal URLs
    // ----------------------------------------------------------------------

    public void testIllegalFileUrl()
        throws Exception
    {
        testIllegalUrl( "file:/tmp/svn" );
    }

    // ----------------------------------------------------------------------
    // Testing provider from path
    // ----------------------------------------------------------------------

    public void testSvnFromPath()
        throws Exception
    {
        SvnJavaScmProvider provider = new SvnJavaScmProvider();

        provider.makeProviderScmRepository( new File( getBasedir() ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testUrl( String scmUrl, String expectedUrl, String expectedUser )
        throws Exception
    {
        ScmRepository repository = scmManager.makeScmRepository( scmUrl );

        assertNotNull( "ScmManager.makeScmRepository() returned null", repository );

        assertNotNull( "The provider repository was null.", repository.getProviderRepository() );

        assertTrue( "The SCM Repository isn't a " + SvnScmProviderRepository.class.getName() + ".",
                    repository.getProviderRepository() instanceof SvnScmProviderRepository );

        SvnScmProviderRepository providerRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        assertEquals( "url is incorrect", expectedUrl, providerRepository.getUrl() );

        assertEquals( "User is incorrect", expectedUser, providerRepository.getUser() );
    }

    private void testIllegalUrl( String url )
        throws Exception
    {
        try
        {
            scmManager.makeScmRepository( "scm:svn:" + url );

            fail( "Expected a ScmRepositoryException while testing the url '" + url + "'." );
        }
        catch ( ScmRepositoryException e )
        {
            // Success
            assertTrue( true );
        }
    }
}
