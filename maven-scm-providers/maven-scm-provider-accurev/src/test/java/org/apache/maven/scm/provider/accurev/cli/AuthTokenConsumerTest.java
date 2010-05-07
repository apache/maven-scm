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
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class AuthTokenConsumerTest
{

    @Test
    public void testConsumeLineWithPasswordPrefix()
    {
        AuthTokenConsumer consumer = new AuthTokenConsumer();
        consumer.consumeLine( "Password:  abcdef123456" );
        assertThat( consumer.getAuthToken(), is( "abcdef123456" ) );

        consumer.consumeLine( "A different line" );
        assertThat( consumer.getAuthToken(), is( "abcdef123456" ) );
    }

    @Test
    public void testConsumeLineWithoutPasswordPrefix()
    {
        AuthTokenConsumer consumer = new AuthTokenConsumer();
        consumer.consumeLine( "abcdef123456" );
        assertThat( consumer.getAuthToken(), is( "abcdef123456" ) );

        consumer.consumeLine( "A different line" );
        assertThat( consumer.getAuthToken(), is( "abcdef123456" ) );
    }

}
