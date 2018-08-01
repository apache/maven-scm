package org.apache.maven.scm.provider.perforce.command.tag;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.util.ConsumerUtils;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 */
public class PerforceTagConsumerTest
    extends ScmTestCase
{
    public void testGoodParse()
        throws Exception
    {
        File testFile = getTestFile( "src/test/resources/perforce/tag_good.txt" );

        PerforceTagConsumer consumer = new PerforceTagConsumer();

        ConsumerUtils.consumeFile( testFile, consumer );

        assertEquals( "", consumer.getOutput() );
        assertTrue( consumer.isSuccess() );
        List<ScmFile> results = consumer.getTagged();
        assertEquals( "Wrong number of entries returned", 2, results.size() );
        String entry = results.get( 0 ).getPath();
        assertTrue( entry.endsWith( "pom.xml" ) );
    }

    public void testBadParse()
        throws Exception
    {
        File testFile = getTestFile( "src/test/resources/perforce/tag_bad.txt" );

        PerforceTagConsumer consumer = new PerforceTagConsumer();

        ConsumerUtils.consumeFile( testFile, consumer );

        assertFalse( consumer.isSuccess() );
        assertTrue(
            consumer.getOutput().startsWith( "Label 'maven-scm-tes' unknown - use 'label' command to create it." ) );
    }
}
