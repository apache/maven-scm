package org.apache.maven.scm.provider.accurev.cli;

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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.WorkSpace;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith( JUnit4.class )
public class WorkSpaceConsumerTest
    extends ScmTestCase
{

    private ScmLogger logger;

    @Override
    protected InputStream getCustomConfiguration()
        throws Exception
    {
        return AccuRevJUnitUtil.getPlexusConfiguration();
    }

    @Before
    public void setup()
        throws Exception
    {
        setUp();
        logger = AccuRevJUnitUtil.getLogger( getContainer() );
    }

    @Test
    public void testConsumeShowWorkSpaces()
        throws IOException
    {

        Map<String, WorkSpace> wsMap = new HashMap<String, WorkSpace>();
        XppStreamConsumer consumer = new WorkSpaceConsumer( logger, wsMap );
        AccuRevJUnitUtil.consume( "/showworkspaces.xml", consumer );

        WorkSpace ws = wsMap.get( "maventst_ggardner" );
        assertThat( ws, notNullValue() );
        assertThat( ws.getTransactionId(), is( 49L ) );

    }

    @Test
    public void testConsumeShowRefTrees()
        throws IOException
    {

        Map<String, WorkSpace> wsMap = new HashMap<String, WorkSpace>();
        XppStreamConsumer consumer = new WorkSpaceConsumer( logger, wsMap );
        AccuRevJUnitUtil.consume( "/showrefs.xml", consumer );

        WorkSpace ws = wsMap.get( "maven-scm-INT-reftree" );
        assertThat( ws, notNullValue() );
        assertThat( ws.getTransactionId(), is( 12L ) );

    }

}
