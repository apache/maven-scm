package org.apache.maven.scm.provider.accurev.util;

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

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.junit.Test;

public class QuotedPropertyParserTest
{

    @Test
    public void testname()
        throws Exception
    {

        Map<String, String> result =
            QuotedPropertyParser.parse( "param1=one&\"\"param2='2'&\"myK\"ey='my&\"value'&param3=3" );

        assertThat( result, hasEntry( "param1", "one" ) );
        assertThat( result, hasEntry( "param2", "2" ) );
        assertThat( result, hasEntry( "myKey", "my&\"value" ) );
        assertThat( result, hasEntry( "param3", "3" ) );
        assertThat( "3 entries", result.size(), is( 4 ) );

    }
}
