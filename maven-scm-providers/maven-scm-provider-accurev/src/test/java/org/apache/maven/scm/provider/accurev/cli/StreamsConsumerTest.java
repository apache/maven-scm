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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.accurev.Stream;
import org.junit.Test;

public class StreamsConsumerTest
{

    @Test
    public void testname()
        throws Exception
    {

        List<Stream> streams = new ArrayList<Stream>();
        XppStreamConsumer consumer = new StreamsConsumer( new DefaultLog(), streams );
        AccuRevJUnitUtil.consume( "/showstreams.xml", consumer );

        assertThat( streams.size(), is( 5 ) );

        Stream s = streams.get( 2 );
        /*
         * <stream name="mvnscm_1275484086_initRepo_ggardner" basis="mvnscm_1275484086_tckTests" basisStreamNumber="2"
         * depotName="mvnscm_1275484086" streamNumber="3" isDynamic="false" type="workspace" startTime="1275484091"
         * hidden="true"/>
         */
        assertThat( s.getBasis(), is( "mvnscm_1275484086_tckTests" ) );
        assertThat( s.getId(), is( 3L ) );
        assertThat( s.getBasisId(), is( 2L ) );
        assertThat( s.getName(), is( "mvnscm_1275484086_initRepo_ggardner" ) );
        assertThat( s.getStartDate(), is( new Date( 1275484091L * 1000L ) ) );
        assertThat( s.getStreamType(), is( "workspace" ) );
        assertThat( s.isWorkspace(), is( true ) );
    }
}
