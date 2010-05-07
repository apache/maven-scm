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

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import org.junit.Test;

public class StatBackingConsumerTest
{

    @Test
    public void testConsumeLine()
    {
        Collection<File> nonMemberElements = new ArrayList<File>();
        Collection<File> memberElements = new ArrayList<File>();

        String line1 = "./hello.world  maventst/1 (2/1) (member)";
        String line2 = "./tcktests/readme.txt  (no such elem)";
        String line3 = "./src      maventst/1 (2/1) (member)";
        String line4 = "./target   (no such elem)";

        StatBackingConsumer consumer = new StatBackingConsumer( memberElements, nonMemberElements );

        consumer.consumeLine( line1 );
        consumer.consumeLine( line2 );
        consumer.consumeLine( line3 );
        consumer.consumeLine( line4 );

        assertThat( memberElements.size(), is( 2 ) );
        assertThat( nonMemberElements.size(), is( 2 ) );
        assertThat( memberElements, hasItem( new File( "./hello.world" ) ) );
        assertThat( memberElements, hasItem( new File( "./src" ) ) );
        assertThat( nonMemberElements, hasItem( new File( "./tcktests/readme.txt" ) ) );
        assertThat( nonMemberElements, hasItem( new File( "./target" ) ) );

    }
}
