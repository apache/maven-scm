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
package org.apache.maven.scm.provider.svn.svnexe.command.info;

import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SvnInfoConsumerTest {

    @Test
    public void testParseDate() {
        TemporalAccessor date = SvnInfoConsumer.parseDate("2024-01-19 16:33:05 +0100 (Fr, 19 Jan 2024");
        assertEquals(2024, date.get(ChronoField.YEAR));
    }
}
