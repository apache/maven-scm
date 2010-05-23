package org.apache.maven.scm.provider.accurev.command.blame;

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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;

import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.cli.AccuRevJUnitUtil;
import org.apache.maven.scm.provider.accurev.command.AccuRevTckUtil;
import org.apache.maven.scm.tck.command.blame.BlameCommandTckTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith( JUnit4.class )
public class AccuRevBlameCommandTckTest
    extends BlameCommandTckTest
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
    public void testBlameCommand()
        throws Exception
    {

        super.testBlameCommand();
    }

    @Override
    protected void verifyResult( BlameScmResult result )
    {

        assertThat( result, is( not( nullValue() ) ) );
        assertThat( result.getLines().size(), is( 1 ) );

    }

    @Override
    @Before
    public void setUp()
        throws Exception
    {

        super.setUp();
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
