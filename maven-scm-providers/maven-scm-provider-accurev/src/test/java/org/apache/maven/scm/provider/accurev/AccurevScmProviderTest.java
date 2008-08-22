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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import junit.framework.TestCase;
import org.apache.maven.scm.repository.ScmRepositoryException;

public class AccurevScmProviderTest extends TestCase
{
    public void testMakeProviderScmRepository() throws ScmRepositoryException
    {
        AccuRevScmProvider provider = new AccuRevScmProvider();

        AccuRevScmProviderRepository repository = (AccuRevScmProviderRepository) provider.makeProviderScmRepository(
            "user|passwd@testhost.com|5060|TestDepot|TestStream|TestWorkspace", '|' );
        assertEquals( "user", repository.getUser() );
        assertEquals( "passwd", repository.getPassword() );
        assertEquals( "testhost.com", repository.getHost() );
        assertEquals( 5060, repository.getPort() );
        assertEquals( "TestDepot", repository.getDepot() );
        assertEquals( "TestStream", repository.getStreamName() );
        assertEquals( "TestWorkspace", repository.getWorkspaceName() );

    }

    public void testMakeProviderScmRepositoryColumnDelimeter() throws ScmRepositoryException
    {
        AccuRevScmProvider provider = new AccuRevScmProvider();

        AccuRevScmProviderRepository repository = (AccuRevScmProviderRepository) provider.makeProviderScmRepository(
            "user:passwd@testhost.com:5060:TestDepot:TestStream:TestWorkspace", ':' );
        assertEquals( "user", repository.getUser() );
        assertEquals( "passwd", repository.getPassword() );
        assertEquals( "testhost.com", repository.getHost() );
        assertEquals( 5060, repository.getPort() );
        assertEquals( "TestDepot", repository.getDepot() );
        assertEquals( "TestStream", repository.getStreamName() );
        assertEquals( "TestWorkspace", repository.getWorkspaceName() );

    }

    public void testMakeProviderScmRepositoryNoPassword() throws ScmRepositoryException
    {
        AccuRevScmProvider provider = new AccuRevScmProvider();

        AccuRevScmProviderRepository repository = (AccuRevScmProviderRepository) provider.makeProviderScmRepository(
            "user@testhost.com:5060:TestDepot:TestStream:TestWorkspace", ':' );
        assertEquals( "user", repository.getUser() );
        assertNull( repository.getPassword() );
        assertEquals( "testhost.com", repository.getHost() );
        assertEquals( 5060, repository.getPort() );
        assertEquals( "TestDepot", repository.getDepot() );
        assertEquals( "TestStream", repository.getStreamName() );
        assertEquals( "TestWorkspace", repository.getWorkspaceName() );

    }

    public void testDefaultCheckoutMethod() throws ScmRepositoryException
    {
        AccuRevScmProvider provider = new AccuRevScmProvider();

        AccuRevScmProviderRepository repository = (AccuRevScmProviderRepository) provider.makeProviderScmRepository(
            "TestDepot:TestStream:TestWorkspace", ':' );
        assertEquals( "pop", repository.getCheckoutMethod() );

        assertEquals( "TestDepot", repository.getDepot() );
        assertEquals( "TestStream", repository.getStreamName() );
        assertEquals( "TestWorkspace", repository.getWorkspaceName() );

    }

    public void testMakeProviderScmRepositoryWithoutAccountInfo() throws ScmRepositoryException
    {
        AccuRevScmProvider provider = new AccuRevScmProvider();

        AccuRevScmProviderRepository repository = (AccuRevScmProviderRepository) provider.makeProviderScmRepository(
            "TestDepot:TestStream:TestWorkspace", ':' );
        assertEquals( null, repository.getHost() );
        assertEquals( AccuRevScmProviderRepository.DEFAULT_PORT, repository.getPort() );
        assertEquals( "TestDepot", repository.getDepot() );
        assertEquals( "TestStream", repository.getStreamName() );
        assertEquals( "TestWorkspace", repository.getWorkspaceName() );

    }

    public void testMakeProviderScmRepositoryWithoutWorkspace() throws ScmRepositoryException
    {
        AccuRevScmProvider provider = new AccuRevScmProvider();

        AccuRevScmProviderRepository repository = (AccuRevScmProviderRepository) provider.makeProviderScmRepository(
            "TestDepot:TestStream", ':' );
        assertEquals( null, repository.getHost() );
        assertEquals( AccuRevScmProviderRepository.DEFAULT_PORT, repository.getPort() );
        assertEquals( "TestDepot", repository.getDepot() );
        assertEquals( "TestStream", repository.getStreamName() );
        assertEquals( null, repository.getWorkspaceName() );
    }

    public void testMakeProviderScmRepositoryWithoutStream()
    {
        AccuRevScmProvider provider = new AccuRevScmProvider();

        try
        {
            provider.makeProviderScmRepository( "TestDepot", ':' );
            assertFalse( "Stream name should be required", true );
        }
        catch ( ScmRepositoryException e )
        {
            //good
        }
    }

    public void testMakeProviderScmRepositoryWithParams() throws ScmRepositoryException
    {
        AccuRevScmProvider provider = new AccuRevScmProvider();

        AccuRevScmProviderRepository rep = (AccuRevScmProviderRepository) provider
            .makeProviderScmRepository( "TestDepot:Stream?param=value&param2=value2&param3=", ':' );
        assertEquals( "value", rep.getParams().get( "param" ) );
        assertEquals( "value2", rep.getParams().get( "param2" ) );
        assertEquals( "", rep.getParams().get( "param3" ) );
    }
}
