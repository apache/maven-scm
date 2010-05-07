package org.apache.maven.scm.provider.accurev.command.checkout;

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

import org.apache.maven.scm.provider.accurev.cli.AccuRevJUnitUtil;
import org.apache.maven.scm.provider.accurev.command.AccuRevTckUtil;
import org.apache.maven.scm.tck.command.checkout.CheckOutCommandTckTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * TODO TckTest does not adhere to repository path return from checkout result. Raise a JIRA to fix that.
 * 
 * @author ggardner
 */
@RunWith( JUnit4.class )
public class AccuRevCheckoutCommandTckTest
    extends CheckOutCommandTckTest
{

    protected AccuRevTckUtil testUtil = new AccuRevTckUtil();

    @Override
    protected InputStream getCustomConfiguration()
        throws Exception

    {
        return AccuRevJUnitUtil.getPlexusConfiguration();
    }

    @Override
    protected File getWorkingCopy()
    {
        return testUtil.getWorkingCopy();
    }

    @Override
    protected File getAssertionCopy()
    {
        return testUtil.getAssertionCopy();
    }

    @Override
    protected File getUpdatingCopy()
    {
        return testUtil.getUpdatingCopy();
    }

    @Override
    public String getScmUrl()
        throws Exception
    {
        return testUtil.getScmUrl();
    }

    @Override
    public void initRepo()
        throws Exception
    {
        testUtil.initRepo( getContainer() );
    }

    @Override
    @Test
    public void testCheckOutCommandTest()
        throws Exception
    {
        super.testCheckOutCommandTest();
    }

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    @Override
    @After
    public void tearDown()
        throws Exception
    {
        try
        {
            testUtil.tearDown();
            testUtil.removeWorkSpace( getWorkingCopy() );

        }
        finally
        {
            super.tearDown();
        }
    }

}
