package org.apache.maven.scm.manager;

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

import junit.framework.TestCase;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.ScmProviderStub;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryStub;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Test for the ScmManagerStub
 *
 * @author <a href="mailto:carlos@apache.org">Carlos Sanchez</a>
 * @version $Id$
 */
public class ScmManagerStubTest
    extends TestCase
{

    private ScmManagerStub scmManagerStub;

    private List messages;

    private ScmProvider scmProvider;

    private ScmRepository scmRepository;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        messages = new ArrayList( 0 );
        scmProvider = new ScmProviderStub();
        scmRepository = new ScmRepositoryStub();

        scmManagerStub = new ScmManagerStub();
        scmManagerStub.setMessages( messages );
        scmManagerStub.setScmProvider( scmProvider );
        scmManagerStub.setScmRepository( scmRepository );
    }

    /*
     * Test method for 'org.apache.maven.scm.manager.ScmManagerStub.makeScmRepository(String)'
     */
    public void testMakeScmRepository()
        throws Exception
    {
        ScmRepository repository = scmManagerStub.makeScmRepository( "" );
        assertSame( scmRepository, repository );
    }

    /*
     * Test method for 'org.apache.maven.scm.manager.ScmManagerStub.makeProviderScmRepository(String, File)'
     */
    public void testMakeProviderScmRepository()
        throws Exception
    {
        ScmRepository repository = scmManagerStub.makeProviderScmRepository( "", new File( "" ) );
        assertSame( scmRepository, repository );
    }

    /*
     * Test method for 'org.apache.maven.scm.manager.ScmManagerStub.validateScmRepository(String)'
     */
    public void testValidateScmRepository()
    {
        List list = scmManagerStub.validateScmRepository( "" );
        assertSame( messages, list );
    }

    /*
     * Test method for 'org.apache.maven.scm.manager.ScmManagerStub.getProviderByUrl(String)'
     */
    public void testGetProviderByUrl()
        throws Exception
    {
        ScmProvider providerByUrl = scmManagerStub.getProviderByUrl( "" );
        assertSame( scmProvider, providerByUrl );
    }

    /*
     * Test method for 'org.apache.maven.scm.manager.ScmManagerStub.getProviderByType(String)'
     */
    public void testGetProviderByType()
        throws Exception
    {
        ScmProvider providerByType = scmManagerStub.getProviderByType( "" );
        assertSame( scmProvider, providerByType );
    }

    /*
     * Test method for 'org.apache.maven.scm.manager.ScmManagerStub.getProviderByRepository(ScmRepository)'
     */
    public void testGetProviderByRepository()
        throws Exception
    {
        ScmProvider providerByRepository = scmManagerStub.getProviderByRepository( new ScmRepositoryStub() );
        assertSame( scmProvider, providerByRepository );
    }

}
