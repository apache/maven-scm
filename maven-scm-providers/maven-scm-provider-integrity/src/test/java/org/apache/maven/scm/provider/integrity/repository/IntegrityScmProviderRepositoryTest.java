package org.apache.maven.scm.provider.integrity.repository;

/**
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

/**
 * IntegrityScmProviderRepositoryTest
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityScmProviderRepositoryTest.java 1.1 2011/08/29 00:30:11EDT Cletus D'Souza (dsouza) Exp  $
 */
public class IntegrityScmProviderRepositoryTest
    extends ScmTestCase
{
    private ScmManager scmManager;

    /**
     * Initialize our ScmManager object
     */
    public void setUp()
        throws Exception
    {
        super.setUp();
        scmManager = getScmManager();
    }

    /**
     * Executes a test with the bare minimum required for the Integrity Scm URL String
     *
     * @throws Exception
     */
    public void testMinimumUrlString()
        throws Exception
    {
        // Minimum url string
        testValidUrl( "scm:integrity|#/repo/project", "#/repo/project", "", 0, "", "" );
    }

    /**
     * Executes a test with only the hostname specified
     *
     * @throws Exception
     */
    public void testWithHostname()
        throws Exception
    {
        // Test with host
        testValidUrl( "scm:integrity|@localhost|#/repo/project", "#/repo/project", "localhost", 0, "", "" );
    }

    /**
     * Executes a test with only the hostname and port specified
     *
     * @throws Exception
     */
    public void testWithHostAndPort()
        throws Exception
    {
        // Test host and port
        testValidUrl( "scm:integrity|@localhost:7001|#/repo/project", "#/repo/project", "localhost", 7001, "", "" );
    }

    /**
     * Executes a test with only the username specified
     *
     * @throws Exception
     */
    public void testWithUser()
        throws Exception
    {
        // Test with user
        testValidUrl( "scm:integrity|dsouza@|#/repo/project", "#/repo/project", "", 0, "dsouza", "" );
    }

    /**
     * Executes a test with the username and password specified
     *
     * @throws Exception
     */
    public void testWithUserAndPassword()
        throws Exception
    {
        // Test with user and password
        testValidUrl( "scm:integrity|dsouza/password@|#/repo/project", "#/repo/project", "", 0, "dsouza", "password" );
    }

    /**
     * Executes a test with the username and hostname specified
     *
     * @throws Exception
     */
    public void testWithUserAndHost()
        throws Exception
    {
        // Test with user and host
        testValidUrl( "scm:integrity|dsouza@localhost|#/repo/project", "#/repo/project", "localhost", 0, "dsouza", "" );
    }

    /**
     * Executes a test with the username and hostname plus port specified
     *
     * @throws Exception
     */
    public void testWithUserAndHostPort()
        throws Exception
    {
        // Test with user and host:port
        testValidUrl( "scm:integrity|dsouza@localhost:7001|#/repo/project", "#/repo/project", "localhost", 7001,
                      "dsouza", "" );
    }

    /**
     * Executes a test with the username and password plus hostname specified
     *
     * @throws Exception
     */
    public void testWithUserPasswordAndHost()
        throws Exception
    {
        // Test with user/password and host
        testValidUrl( "scm:integrity|dsouza/password@localhost|#/repo/project", "#/repo/project", "localhost", 0,
                      "dsouza", "password" );
    }

    /**
     * Executes a test with all the components of the SCM URL string specified
     *
     * @throws Exception
     */
    public void testWithWholeURL()
        throws Exception
    {
        // Test with the complete url
        testValidUrl( "scm:integrity|dsouza/password@localhost:7001|#/repo/project", "#/repo/project", "localhost",
                      7001, "dsouza", "password" );
    }

    /**
     * Tests the various components of the Integrity SCM URL
     *
     * @param scmUrl             Integrity SCM URL
     * @param expectedConfigPath Expected Configuration Path
     * @param expectedHost       Expected Hostname
     * @param expectedPort       Expected Port
     * @param expectedUser       Expected Username
     * @param expectedPassword   Expected Password
     * @throws Exception
     */
    private void testValidUrl( String scmUrl, String expectedConfigPath, String expectedHost, int expectedPort,
                               String expectedUser, String expectedPassword )
        throws Exception
    {
        ScmRepository repo = scmManager.makeScmRepository( scmUrl );
        assertNotNull( "ScmManager.makeScmRepository() returned null", repo );
        assertNotNull( "The provider repository was null.", repo.getProviderRepository() );
        assertTrue( "The SCM Repository isn't a " + IntegrityScmProviderRepository.class.getName() + ".",
                    repo.getProviderRepository() instanceof IntegrityScmProviderRepository );
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repo.getProviderRepository();
        assertEquals( "Configration Path is incorrect", expectedConfigPath, iRepo.getConfigruationPath() );
        assertEquals( "Hostname is incorrect", expectedHost, iRepo.getHost() );
        assertEquals( "Port is incorrect", expectedPort, iRepo.getPort() );
        assertEquals( "Username is incorrect", expectedUser, iRepo.getUser() );
        assertEquals( "Password is incorrect", expectedPassword, iRepo.getPassword() );
    }
}
