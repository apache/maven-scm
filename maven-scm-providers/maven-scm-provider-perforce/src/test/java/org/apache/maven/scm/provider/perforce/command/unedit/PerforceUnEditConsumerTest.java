package org.apache.maven.scm.provider.perforce.command.unedit;

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
public class PerforceUnEditConsumerTest
    extends ScmTestCase
{
    public void testGoodParse()
        throws Exception
    {
        File testFile = getTestFile( "src/test/resources/perforce/unedit_good.txt" );

        PerforceUnEditConsumer consumer = new PerforceUnEditConsumer();

        ConsumerUtils.consumeFile( testFile, consumer );

        assertTrue( consumer.isSuccess() );
        List<ScmFile> edits = consumer.getEdits();
        assertEquals( "Wrong number of entries returned", 2, edits.size() );
        String entry = edits.get( 0 ).getPath();
        assertTrue( entry.startsWith( "//" ) );
        assertTrue( entry.endsWith( ".classpath" ) );
    }

    public void testBadParse()
        throws Exception
    {
        File testFile = getTestFile( "src/test/resources/perforce/unedit_bad.txt" );

        PerforceUnEditConsumer consumer = new PerforceUnEditConsumer();

        ConsumerUtils.consumeFile( testFile, consumer );

        assertFalse( consumer.isSuccess() );
        assertTrue( consumer.getOutput().indexOf( ".classpath - file(s) not opened on this client." ) == 0 );
        assertTrue( consumer.getOutput().indexOf( ".project - file(s) not opened on this client." ) > 0 );
    }
}
