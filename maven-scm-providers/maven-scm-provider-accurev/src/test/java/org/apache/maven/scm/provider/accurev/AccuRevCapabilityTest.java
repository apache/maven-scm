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
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AccuRevCapabilityTest
{
    @Test
    public void testDiffBetweenStreams()
        throws Exception
    {
        assertThat( AccuRevCapability.DIFF_BETWEEN_STREAMS.isSupported( "4.7.1" ), is( false ) );
        assertThat( AccuRevCapability.DIFF_BETWEEN_STREAMS.isSupported( "4.7.2" ), is( true ) );
        assertThat( AccuRevCapability.DIFF_BETWEEN_STREAMS.isSupported( "4.7.2a" ), is( true ) );
        assertThat( AccuRevCapability.DIFF_BETWEEN_STREAMS.isSupported( "4.7.4" ), is( true ) );
        assertThat( AccuRevCapability.DIFF_BETWEEN_STREAMS.isSupported( "5.0.1" ), is( true ) );
    }

    @Test
    public void testPopToTransaction()
        throws Exception
    {
        assertThat( AccuRevCapability.POPULATE_TO_TRANSACTION.isSupported( "4.7.4b" ), is( false ) );
        assertThat( AccuRevCapability.POPULATE_TO_TRANSACTION.isSupported( "4.9.0c" ), is( true ) );
        assertThat( AccuRevCapability.POPULATE_TO_TRANSACTION.isSupported( "5.0.1" ), is( false ) );
    }
}
