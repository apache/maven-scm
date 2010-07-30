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

import static org.apache.maven.scm.provider.accurev.VersionMatcher.version;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.accurev.Transaction;
import org.junit.Test;

public class HistoryConsumerTest
{

    @Test
    public void testConsumeStreamHistory()
        throws IOException
    {
        List<Transaction> transactions = new ArrayList<Transaction>();
        XppStreamConsumer consumer = new HistoryConsumer( new DefaultLog(), transactions );
        AccuRevJUnitUtil.consume( "/streamHistory.xml", consumer );

        assertThat( transactions.size(), is( 4 ) );
        Transaction t = transactions.get( 0 );
        assertThat( t.getTranType(), is( "promote" ) );
        assertThat( t.getWhen(), is( new Date( 1233782838000L ) ) );
        assertThat( t.getAuthor(), is( "ggardner" ) );
        assertThat( t.getId(), is( 50L ) );
        assertThat( t.getVersions().size(), is( 2 ) );

        assertThat( t.getVersions(),
                    hasItem( version( 8L, "/./tcktests/src/main/java/Application.java", "1/1", "2/3" ) ) );

        t = transactions.get( 1 );
        assertThat( t.getComment(), is( "hpromoting" ) );

    }

}
