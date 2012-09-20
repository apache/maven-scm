package org.apache.maven.scm.provider.cvslib.repository;

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

import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class CvsScmProviderRepositoryTest
    extends AbstractCvsScmTest
{
    private ScmManager scmManager;

    public void setUp()
        throws Exception
    {
        super.setUp();

        scmManager = getScmManager();
    }

    public void testParseConnectionFromPath()
        throws Exception
    {
        TestCvsScmProvider provider = new TestCvsScmProvider();

        CvsScmProviderRepository repo = (CvsScmProviderRepository) provider
            .makeProviderScmRepository( getTestFile( "src/test/resources/checkoutdir" ) );

        assertEquals( "ext", repo.getTransport() );

        assertEquals( "evenisse", repo.getUser() );

        assertEquals( "cvs.surefire.codehaus.org", repo.getHost() );

        assertEquals( "/home/projects/surefire/scm", repo.getPath() );

        assertEquals( "surefire", repo.getModule() );
    }

    public void testParseRemotePserverConnection()
        throws Exception
    {
        String url = "pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven";

        CvsScmProviderRepository repo = testUrl( url, url );

        assertEquals( "pserver", repo.getTransport() );

        assertEquals( "anoncvs", repo.getUser() );

        assertEquals( "cvs.apache.org", repo.getHost() );

        assertEquals( "/home/cvspublic", repo.getPath() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:/home/cvspublic", repo.getCvsRoot() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:2401/home/cvspublic", repo.getCvsRootForCvsPass() );
    }

    public void testParseRemotePserverConnectionWithUsernameDefinedInScmRepository()
        throws Exception
    {
        String url = "pserver:cvs.apache.org:/home/cvspublic:maven";

        CvsScmProviderRepository repo = testUrl( url, url.replaceFirst( ":cvs", "@cvs" ) );

        repo.setUser( "myusername" );

        assertEquals( "pserver", repo.getTransport() );

        assertEquals( "myusername", repo.getUser() );

        assertEquals( "cvs.apache.org", repo.getHost() );

        assertEquals( "/home/cvspublic", repo.getPath() );

        assertEquals( ":pserver:myusername@cvs.apache.org:/home/cvspublic", repo.getCvsRoot() );

        assertEquals( ":pserver:myusername@cvs.apache.org:2401/home/cvspublic", repo.getCvsRootForCvsPass() );
    }

    public void testParseRemotePserverConnectionWithoutUsername()
        throws Exception
    {
        String url = "pserver:cvs.apache.org:/home/cvspublic:maven";

        CvsScmProviderRepository repo = testUrl( url, url.replaceFirst( ":cvs", "@cvs" ) );

        try
        {
            repo.getCvsRoot();

            fail( "username isn't defined." );
        }
        catch ( Exception e )
        {
        }
    }

    public void testParseRemotePserverConnection2()
        throws Exception
    {
        String url = "pserver:anoncvs:@cvs.apache.org:/home/cvspublic:maven";

        CvsScmProviderRepository repo = testUrl( url, url.replaceFirst( ":@", "@" ) );

        assertEquals( "pserver", repo.getTransport() );

        assertEquals( "anoncvs", repo.getUser() );

        assertEquals( "cvs.apache.org", repo.getHost() );

        assertEquals( "/home/cvspublic", repo.getPath() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:/home/cvspublic", repo.getCvsRoot() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:2401/home/cvspublic", repo.getCvsRootForCvsPass() );
    }

    public void testParseRemotePserverConnectionWithPort()
        throws Exception
    {
        String url = "pserver:anoncvs:@cvs.apache.org:2401:/home/cvspublic:maven";

        CvsScmProviderRepository repo = testUrl( url, url.replaceFirst( ":2401", "" ).replaceFirst( ":@", "@" ) );

        assertEquals( "pserver", repo.getTransport() );

        assertEquals( "anoncvs", repo.getUser() );

        assertEquals( "", repo.getPassword() );

        assertEquals( "cvs.apache.org", repo.getHost() );

        assertEquals( "/home/cvspublic", repo.getPath() );

        assertEquals( 2401, repo.getPort() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:/home/cvspublic", repo.getCvsRoot() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:2401/home/cvspublic", repo.getCvsRootForCvsPass() );
    }

    public void testParseRemotePserverConnectionWithPassword()
        throws Exception
    {
        String url = "pserver:anoncvs:mypassword@cvs.apache.org:/home/cvspublic:maven";

        CvsScmProviderRepository repo = testUrl( url, url.replaceFirst( ":mypassword", "" ) );

        assertEquals( "pserver", repo.getTransport() );

        assertEquals( "anoncvs", repo.getUser() );

        assertEquals( "mypassword", repo.getPassword() );

        assertEquals( "cvs.apache.org", repo.getHost() );

        assertEquals( "/home/cvspublic", repo.getPath() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:/home/cvspublic", repo.getCvsRoot() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:2401/home/cvspublic", repo.getCvsRootForCvsPass() );
    }

    public void testParseRemotePserverConnectionWithPortAndPassword()
        throws Exception
    {
        String url = "pserver:anoncvs:mypassword@cvs.apache.org:2402:/home/cvspublic:maven";

        CvsScmProviderRepository repo =
            testUrl( url, url.replaceFirst( ":mypassword", "" ).replaceFirst( ":2402:", ":2402" ) );

        assertEquals( "pserver", repo.getTransport() );

        assertEquals( "anoncvs", repo.getUser() );

        assertEquals( "mypassword", repo.getPassword() );

        assertEquals( "cvs.apache.org", repo.getHost() );

        assertEquals( "/home/cvspublic", repo.getPath() );

        assertEquals( 2402, repo.getPort() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:2402/home/cvspublic", repo.getCvsRoot() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:2402/home/cvspublic", repo.getCvsRootForCvsPass() );
    }

    public void testParseRemotePserverConnectionWithBarsAsDelimiter()
        throws Exception
    {
        String url = "pserver|anoncvs@cvs.apache.org|/home/cvspublic|maven";

        String urlResult = "pserver:anoncvs@cvs.apache.org:/home/cvspublic:maven";

        CvsScmProviderRepository repo = testUrl( url, urlResult, '|' );

        assertEquals( "pserver", repo.getTransport() );

        assertEquals( "anoncvs", repo.getUser() );

        assertEquals( "cvs.apache.org", repo.getHost() );

        assertEquals( "/home/cvspublic", repo.getPath() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:/home/cvspublic", repo.getCvsRoot() );

        assertEquals( ":pserver:anoncvs@cvs.apache.org:2401/home/cvspublic", repo.getCvsRootForCvsPass() );
    }

    public void testIllegalRepository()
        throws Exception
    {
        testIllegalUrl( "pserver:cvs.apache.org:/home/cvspublic:maven" );
    }

    public void testParseLocalConnection()
        throws Exception
    {
        String url = "local:/home/cvspublic:maven";

        CvsScmProviderRepository repo = testUrl( url, url );

        assertEquals( "local", repo.getTransport() );

        assertNull( repo.getUser() );

        assertNull( repo.getHost() );

        assertEquals( "/home/cvspublic", repo.getPath() );

        assertEquals( ":local:/home/cvspublic", repo.getCvsRoot() );
    }

    /**
     * Test SCM-387
     *
     * @throws Exception
     */
    public void testGetCvsRootForCvsPass()
        throws Exception
    {
        String url = "scm:cvs|local|c:/cvsroot|test";

        ScmRepository repository = scmManager.makeScmRepository( url );
        CvsScmProviderRepository repo = (CvsScmProviderRepository) repository.getProviderRepository();
        assertEquals( ":local:c:/cvsroot", repo.getCvsRootForCvsPass() );
    }

    /**
     * Test SCM-376
     *
     * @throws Exception
     */
    public void testSCM376()
        throws Exception
    {
        String url = "scm:cvs:pserver:continuum@gnllx002.ebit.be:2401:/data01/cvsroot_bkh:VCG_BKH/uBaseBkh";

        ScmRepository repository = scmManager.makeScmRepository( url );
        
        assertNotNull( repository );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    /**
     * @param expectedUrl url that should be printed in the toString method
     */
    private CvsScmProviderRepository testUrl( String url, String expectedUrl, char delimiter, int nbErrorMessages )
        throws Exception
    {
        assertEquals( nbErrorMessages, scmManager.validateScmRepository( "scm:cvs" + delimiter + url ).size() );

        ScmRepository repository = scmManager.makeScmRepository( "scm:cvs" + delimiter + url );

        assertNotNull( "ScmManager.makeScmRepository() returned null", repository );

        assertNotNull( "The provider repository was null.", repository.getProviderRepository() );

        assertTrue( "The SCM Repository isn't a " + CvsScmProviderRepository.class.getName() + ".", repository
            .getProviderRepository() instanceof CvsScmProviderRepository );

        assertEquals( "cvs:" + expectedUrl, repository.toString() );

        return (CvsScmProviderRepository) repository.getProviderRepository();
    }

    private CvsScmProviderRepository testUrl( String url, String urlResult )
        throws Exception
    {
        return testUrl( url, urlResult, ':', 0 );
    }

    private CvsScmProviderRepository testUrl( String url, String urlResult, char delimiter )
        throws Exception
    {
        return testUrl( url, urlResult, delimiter, 0 );
    }

    private void testIllegalUrl( String url )
        throws Exception
    {
        try
        {
            testUrl( "scm:cvs:" + url, null, ':', 1 );

            fail( "Expected a ScmRepositoryException while testing the url '" + url + "'." );
        }
        catch ( ScmRepositoryException e )
        {
            // expected
        }
    }

}
