package org.apache.maven.scm.provider.perforce.command.checkout;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.util.ConsumerUtils;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class PerforceCheckOutConsumerTest
    extends ScmTestCase
{
    public void testGoodParse()
        throws Exception
    {
        File testFile = getTestFile( "src/test/resources/perforce/checkout_good.txt" );

        PerforceCheckOutConsumer consumer = new PerforceCheckOutConsumer( "test-test-maven", "//depot/modules" );
        consumer.consumeLine( "Client test-test-maven saved." );
        assertEquals( "", consumer.getOutput() );
        assertTrue( consumer.isSuccess() );

        ConsumerUtils.consumeFile( testFile, consumer );

        assertTrue( consumer.getOutput(), consumer.isSuccess() );
        assertEquals( "", consumer.getOutput() );
        assertEquals( 4, consumer.getCheckedout().size() );
        ScmFile file = (ScmFile) consumer.getCheckedout().get( 0 );
        assertEquals( "cordoba/runtime-ear/.j2ee", file.getPath() );
        assertEquals( ScmFileStatus.DELETED, file.getStatus() );
    }

    public void testBadParse()
        throws Exception
    {
        File testFile = getTestFile( "src/test/resources/perforce/checkout_bad.txt" );

        PerforceCheckOutConsumer consumer = new PerforceCheckOutConsumer( "test-test-maven", "//depot/modules" );
        consumer.consumeLine( "Something bad happened." );
        assertFalse( consumer.isSuccess() );

        consumer = new PerforceCheckOutConsumer( "test-test-maven", "" );
        consumer.consumeLine( "Client test-test-maven saved." );
        assertTrue( consumer.isSuccess() );

        ConsumerUtils.consumeFile( testFile, consumer );

        assertFalse( consumer.getOutput(), consumer.isSuccess() );
        assertTrue( consumer.getOutput(), consumer.getOutput().length() > 0 );
        assertContains( consumer.getOutput(), "Invalid" );
        assertContains( consumer.getOutput(), "somelabel" );
    }

    private void assertContains( String block, String element )
    {
        if ( block.indexOf( element ) == -1 )
        {
            fail( "Block '" + block + "' does not contain element '" + element + "'" );
        }
    }
}
