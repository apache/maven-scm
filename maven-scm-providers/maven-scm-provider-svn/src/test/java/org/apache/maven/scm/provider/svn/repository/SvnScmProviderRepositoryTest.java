package org.apache.maven.scm.provider.svn.repository;

/*
 * Copyright 2003-2004 The Apache Software Foundation.
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
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
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

    public void testLegalFileURL()
        throws Exception
    {
        testUrl( "file:///tmp/repo" );
    }

    public void testLegalHttpURL()
        throws Exception
    {
        testUrl( "http://subversion.tigris.org" );
    }

    public void testLegalHttpsURL()
        throws Exception
    {
        testUrl( "https://subversion.tigris.org" );
    }

    public void testLegalSvnURL()
        throws Exception
    {
        testUrl( "svn://subversion.tigris.org" );
    }

    public void testLegalSvnPlusSshURL()
        throws Exception
    {
        testUrl( "svn+ssh://subversion.tigris.org" );
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
    //
    // ----------------------------------------------------------------------

    private void testUrl( String url )
        throws Exception
    {
        ScmRepository repository = scmManager.makeScmRepository( "scm:svn:" + url );

        assertNotNull( "ScmManager.makeScmRepository() returned null", repository );

        assertNotNull( "The provider repository was null.", repository.getProviderRepository() );

        assertTrue( "The SCM Repository isn't a " + SvnScmProviderRepository.class.getName() + ".", repository.getProviderRepository() instanceof SvnScmProviderRepository );
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
            // expected
        }
    }
}
