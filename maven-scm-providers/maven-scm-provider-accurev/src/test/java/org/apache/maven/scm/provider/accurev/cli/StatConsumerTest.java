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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogger;
import org.junit.Test;

public class StatConsumerTest
{

    @Test
    public void testIgnored()
        throws Exception
    {
        // At this stage we do not actually need to know the status apart from a file/directory
        // being ignored.
        assertNull( consume( "/stat.ignored.xml" ) );
    }

    @Test
    public void testBacked()
        throws Exception
    {
        assertNotNull( consume( "/stat.backed.xml" ) );
    };

    @Test
    public void testNoWorkspace()
        throws Exception
    {
        // We don't care about files outside a workspace
        consume( "/stat.noworkspace.xml" );
    }

    private String consume( String resource )
        throws IOException
    {
        ScmLogger logger = new DefaultLog();
        StatConsumer consumer = new StatConsumer( logger );

        AccuRevJUnitUtil.consume( resource, consumer );
        return consumer.getStatus();
    }

}
