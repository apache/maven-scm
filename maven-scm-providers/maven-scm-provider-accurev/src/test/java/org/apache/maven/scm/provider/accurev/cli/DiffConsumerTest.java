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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.accurev.FileDifference;
import org.junit.Test;

public class DiffConsumerTest
{

    @Test
    public void testDiffXML()
        throws Exception
    {
        List<FileDifference> differences = new ArrayList<FileDifference>();
        XppStreamConsumer consumer = new DiffConsumer( new DefaultLog(), differences );
        AccuRevJUnitUtil.consume( "/diff-vvt.xml", consumer );

        assertThat( differences.size(), is( 3 ) );
        assertThat( differences, hasItem( new FileDifference( 8L, "/tcktests/src/main/java/Application.java", "2/3",
                                                              null, null ) ) );
        assertThat( differences, hasItem( new FileDifference( 9L, "/tcktests/hello-world.txt", "2/4",
                                                              "/tcktests/hello.world", "6/1" ) ) );

    }
}
