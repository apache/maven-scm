package org.apache.maven.scm.provider.jazz.repository;

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

import java.util.List;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.manager.NoSuchScmProviderException;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzScmProviderRepositoryTest
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
    // "scm:jazz:[username[;password]@]http[s]://server_name[:port]/contextRoot:repositoryWorkspace";
    // ----------------------------------------------------------------------

    public void testLegalFullHttpURI()
        throws Exception
    {
        testUrl( "scm:jazz:username;password@http://server_name:9443/contextRoot:repositoryWorkspace", "http://server_name:9443/contextRoot", "username", "password", "server_name", 9443, "repositoryWorkspace" );
    }

    public void testLegalHttpURI()
        throws Exception
    {
        testUrl( "scm:jazz:http://server_name:9443/contextRoot:repositoryWorkspace", "http://server_name:9443/contextRoot", null, null, "server_name", 9443, "repositoryWorkspace" );
    }

    public void testLegalFullHttpURIWithLongPath()
        throws Exception
    {
        testUrl( "scm:jazz:username;password@http://server_name:9443/some/long/contextRoot:repositoryWorkspace", "http://server_name:9443/some/long/contextRoot", "username", "password", "server_name", 9443, "repositoryWorkspace" );
    }

    public void testLegalHttpURIWithLongPath()
        throws Exception
    {
        testUrl( "scm:jazz:http://server_name:9443/some/long/contextRoot:repositoryWorkspace", "http://server_name:9443/some/long/contextRoot", null, null, "server_name", 9443, "repositoryWorkspace" );
    }
    
    public void testLegalFullHttpURIWithShortPath()
        throws Exception
    {
        testUrl( "scm:jazz:username;password@http://server_name:9443/:repositoryWorkspace", "http://server_name:9443/", "username", "password", "server_name", 9443, "repositoryWorkspace" );
    }

    public void testLegalHttpURIWithShortPathPath()
        throws Exception
    {
        testUrl( "scm:jazz:http://server_name:9443/:repositoryWorkspace", "http://server_name:9443/", null, null, "server_name", 9443, "repositoryWorkspace" );
    }

    
    public void testLegalFullHttpURIWithShortPathAndNoPort()
        throws Exception
    {
        testUrl( "scm:jazz:username;password@http://server_name/:repositoryWorkspace", "http://server_name/", "username", "password", "server_name", 0, "repositoryWorkspace" );
    }

    public void testLegalHttpURIWithShortPathPathAndNoPort()
        throws Exception
    {
        testUrl( "scm:jazz:http://server_name/:repositoryWorkspace", "http://server_name/", null, null, "server_name", 0, "repositoryWorkspace" );
    }
    
    public void testLegalHttpURIWithUser()
        throws Exception
    {
        testUrl( "scm:jazz:username@http://server_name:9443/contextRoot:repositoryWorkspace", "http://server_name:9443/contextRoot", "username", null, "server_name", 9443, "repositoryWorkspace" );
    }
    
    public void testLegalHttpURIWithUserAndPassword()
        throws Exception
    {
        testUrl( "scm:jazz:username;password@http://server_name:9443/contextRoot:repositoryWorkspace", "http://server_name:9443/contextRoot", "username", "password", "server_name", 9443, "repositoryWorkspace" );
    }

    public void testLegalFullHttpsURI()
        throws Exception
    {
        testUrl( "scm:jazz:username;password@https://server_name:9443/contextRoot:repositoryWorkspace", "https://server_name:9443/contextRoot", "username", "password", "server_name", 9443, "repositoryWorkspace" );
    }

    public void testLegalHttpsURI()
        throws Exception
    {
        testUrl( "scm:jazz:https://server_name:9443/contextRoot:repositoryWorkspace", "https://server_name:9443/contextRoot", null, null, "server_name", 9443, "repositoryWorkspace" );
    }
    
    public void testLegalHttpsURINoPort()
        throws Exception
    {
        testUrl( "scm:jazz:https://server_name/contextRoot:repositoryWorkspace", "https://server_name/contextRoot", null, null, "server_name", 0, "repositoryWorkspace" );
    }

    public void testLegalHttpsURIWithUser()
        throws Exception
    {
        testUrl( "scm:jazz:username@https://server_name:9443/contextRoot:repositoryWorkspace", "https://server_name:9443/contextRoot", "username", null, "server_name", 9443, "repositoryWorkspace" );
    }
    
    public void testLegalHttpsURIWithUserAndPassword()
        throws Exception
    {
        testUrl( "scm:jazz:username;password@https://server_name:9443/contextRoot:repositoryWorkspace", "https://server_name:9443/contextRoot", "username", "password", "server_name", 9443, "repositoryWorkspace" );
    }

    public void testLegalFullHttpURIWithSpaces()
        throws Exception
    {
        testUrl( "scm:jazz:username;password@http://server_name:9443/contextRoot:repository Workspace", "http://server_name:9443/contextRoot", "username", "password", "server_name", 9443, "repository Workspace" );
    }

    public void testLegalFullHttpsURIWithSpaces()
        throws Exception
    {
        testUrl( "scm:jazz:username;password@https://server_name:9443/contextRoot:repository Workspace", "https://server_name:9443/contextRoot", "username", "password", "server_name", 9443, "repository Workspace" );
    }

    public void testLegalFullHttpsURIWithSpacesAndQuote()
        throws Exception
    {
        testUrl( "scm:jazz:username;password@https://server_name:9443/contextRoot:Dave's Repository Workspace", "https://server_name:9443/contextRoot", "username", "password", "server_name", 9443, "Dave's Repository Workspace" );
    }

    // ----------------------------------------------------------------------
    // Testing illegal URLs
    // "scm:jazz:[username[;password]@]http[s]://server_name[:port]/contextRoot:repositoryWorkspace";
    // Something missing or broken in the above.
    // ----------------------------------------------------------------------

    public void testIllegalFullHttpsURIWithMissingPathOrWorkspace()
        throws Exception
    {
        testBrokenUrl( "scm:jazz:username;password@https://server_name:9443/contextRootOrWorkspaceIsMissing" );
    }

    public void testIllegalFullHttpsURIWithMissingPathOrWorkspaceAndPort()
        throws Exception
    {
        testBrokenUrl( "scm:jazz:username;password@https://server_name/contextRootOrWorkspaceIsMissing" );
    }

    public void testIllegalFullHttpsURIWithMissingPathOrWorkspaceAndUsernameAndPasswordAndPort()
        throws Exception
    {
        testBrokenUrl( "scm:jazz:https://server_name/contextRootOrWorkspaceIsMissing" );
    }

    public void testIllegalFullHttpsURIWithMissingPortButWithColon()
        throws Exception
    {
        testBrokenUrl( "scm:jazz:username;password@https://server_name:/contextRoot:repositoryWorkspace" );
    }

    public void testIllegalFullHttpsURIWithMissingPortAndPathOrWorkspace()
        throws Exception
    {
        testBrokenUrl( "scm:jazz:username;password@https://server_name/contextRootOrWorkspaceIsMissing" );
    }

    public void testIllegalWrongProtocolURI()
        throws Exception
    {
        testBrokenUrl( "scm:jazz:username;password@ssh://server_name/contextRoot/repositoryWorkspace" );
    }

    public void testIllegalFullHttpsURIWithBadPort()
        throws Exception
    {
        testBrokenUrl( "scm:jazz:username;password@https://server_name:xxxx/contextRoot:repositoryWorkspace" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testUrl( String scmUrl, String expectedrepositoryURI, String expectedUser, String expectedPassword,
                          String expectedHost, int expectedPort, String expectedRepositoryWorkspace )
        throws Exception
    {
        // The messages are the lines returned from the ScmRepositoryException when thrown on a failure.
        List<String> messages = scmManager.validateScmRepository( scmUrl );
        assertEquals( "Excepected zero messages back from URL Validation, but got: " + messages.size() + " messages. Contents = " + messages, 0, messages.size() );
        
        // Get an instance of the JazzScmProviderRepository, parsing the URL as we go.
        ScmRepository repository = scmManager.makeScmRepository( scmUrl );

        assertNotNull( "ScmManager.makeScmRepository() returned null!", repository );

        assertNotNull( "The provider repository was null!", repository.getProviderRepository() );

        assertTrue( "The SCM Repository isn't a " + JazzScmProviderRepository.class.getName() + "!",
                    repository.getProviderRepository() instanceof JazzScmProviderRepository );

        // Now that we have it, query the rest of the jazz specific values. 
        JazzScmProviderRepository providerRepository = (JazzScmProviderRepository) repository.getProviderRepository();

        assertEquals( "The URI is incorrect!", expectedrepositoryURI, providerRepository.getRepositoryURI() );

        assertEquals( "The URI string is incorrect!", "jazz:" + expectedrepositoryURI + ":" + expectedRepositoryWorkspace, repository.toString() );

        assertEquals( "The user is incorrect!", expectedUser, providerRepository.getUser() );

        assertEquals( "The password is incorrect!", expectedPassword, providerRepository.getPassword() );

        assertEquals( "The host is incorrect!", expectedHost,
                      ( (JazzScmProviderRepository) repository.getProviderRepository() ).getHost() );

        if (expectedPort > 0)
        {
            assertEquals( "The port is incorrect!", expectedPort, providerRepository.getPort() );
        }

        assertEquals( "The RepositoryWorkspace is incorrect!", expectedRepositoryWorkspace, providerRepository.getRepositoryWorkspace() );
    }

    private void testBrokenUrl( String scmUrl )
    {
        try
        {
            ScmRepository repository = scmManager.makeScmRepository( scmUrl );
            fail( "The expected ScmRepositoryException did not occur! " + repository );
        }
        catch (ScmRepositoryException expected)
        {
            // This is the expected behaviour, so we do nothing.
        }
        catch ( NoSuchScmProviderException unexpected )
        {
            fail( "Unexpected failure! " + unexpected.getMessage() );
        }
    }
}
