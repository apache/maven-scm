package org.apache.maven.scm.provider.accurev.command.tag;

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

import java.io.File;
import java.io.InputStream;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.cli.AccuRevJUnitUtil;
import org.apache.maven.scm.provider.accurev.command.AccuRevTckUtil;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.tag.TagCommandTckTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith( JUnit4.class )
public class AccuRevTagCommandTckTest
    extends TagCommandTckTest
{

    private AccuRevTckUtil accurevTckTestUtil = new AccuRevTckUtil();

    @Override
    protected InputStream getCustomConfiguration()
        throws Exception

    {
        return AccuRevJUnitUtil.getPlexusConfiguration();
    }

    @Override
    @Test
    public void testTagCommandTest()
        throws Exception
    {
        super.testTagCommandTest();
    }

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    @Test
    public void testReleasePluginStyleTagThenCheckout()
        throws Exception
    {

        String tag = "test-tag";

        makeFile( getWorkingCopy(), ".acignore", "target/*\ntarget\n" );

        ScmRepository scmRepository = getScmRepository();

        addToWorkingTree( getWorkingCopy(), new File( ".acignore" ), scmRepository );

        CheckInScmResult checkinResult = getScmManager().checkIn( scmRepository, new ScmFileSet( getWorkingCopy() ),
                                                                  "add acignore" );

        assertResultIsSuccess( checkinResult );

        TagScmResult tagResult = getScmManager().getProviderByUrl( getScmUrl() )
            .tag( scmRepository, new ScmFileSet( getWorkingCopy() ), tag );

        assertResultIsSuccess( tagResult );

        scmRepository.getProviderRepository().setPersistCheckout( false );

        CheckOutScmResult checkoutResult = getScmManager().checkOut(
                                                                     scmRepository,
                                                                     new ScmFileSet( new File( getWorkingCopy(),
                                                                                               "target/checkout" ) ),
                                                                     new ScmTag( tag ) );

        assertResultIsSuccess( checkoutResult );

    }

    @Override
    protected File getWorkingCopy()
    {
        return accurevTckTestUtil.getWorkingCopy();
    }

    @Override
    protected File getAssertionCopy()
    {
        return accurevTckTestUtil.getAssertionCopy();
    }

    @Override
    public String getScmUrl()
        throws Exception
    {
        return accurevTckTestUtil.getScmUrl();
    }

    @Override
    public void initRepo()
        throws Exception
    {
        accurevTckTestUtil.initRepo( getContainer() );
        System.setProperty( AccuRevScmProviderRepository.TAG_PREFIX, accurevTckTestUtil.getDepotName() + "_" );
    }

    @Override
    @After
    public void tearDown()
        throws Exception
    {
        try
        {
            accurevTckTestUtil.tearDown();
            accurevTckTestUtil.removeWorkSpace( getWorkingCopy() );
            accurevTckTestUtil.removeWorkSpace( getAssertionCopy() );

        }
        finally
        {
            System.clearProperty( AccuRevScmProviderRepository.TAG_PREFIX );
            super.tearDown();
        }
    }
}
