
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.junit.Test;

/**
 * @author Evgeny Mandrikov
 * @author Grant Gardner
 */
public class AnnotateConsumerTest extends ScmTestCase {

    @Test
    public void testParse() throws Exception {

        List<BlameLine> consumedLines = new ArrayList<BlameLine>();

        AnnotateConsumer consumer = new AnnotateConsumer(consumedLines, new DefaultLog());

        AccuRevJUnitUtil.consume("/annotate.txt", consumer);

        Assert.assertEquals(12, consumer.getLines().size());

        BlameLine line1 = (BlameLine) consumer.getLines().get(0);
        Assert.assertEquals("2", line1.getRevision());
        Assert.assertEquals("godin", line1.getAuthor());
        assertThat(line1.getDate(), is(AccuRev.ACCUREV_TIME_SPEC.parse("2008/10/26 16:26:44")));

        BlameLine line12 = (BlameLine) consumer.getLines().get(11);
        Assert.assertEquals("1", line12.getRevision());
        Assert.assertEquals("godin", line12.getAuthor());
        assertThat(line12.getDate(), is(AccuRev.ACCUREV_TIME_SPEC.parse("2008/10/17 11:41:50")));

    }

}
