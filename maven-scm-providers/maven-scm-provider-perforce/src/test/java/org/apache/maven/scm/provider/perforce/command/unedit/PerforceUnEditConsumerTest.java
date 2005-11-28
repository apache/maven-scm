package org.apache.maven.scm.provider.perforce.command.unedit;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.maven.scm.ScmTestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @version $Id: PerforceChangeLogConsumerTest.java 331276 2005-11-07 15:04:54Z
 *          evenisse $
 */
public class PerforceUnEditConsumerTest
    extends ScmTestCase
{
    public void testGoodParse()
        throws Exception
    {
        File testFile = getTestFile( "src/test/resources/perforce/unedit_good.txt" );

        PerforceUnEditConsumer consumer = new PerforceUnEditConsumer();

        FileInputStream fis = new FileInputStream( testFile );
        BufferedReader in = new BufferedReader( new InputStreamReader( fis ) );
        String s = in.readLine();
        while ( s != null )
        {
            consumer.consumeLine( s );
            s = in.readLine();
        }

        assertTrue( consumer.isSuccess() );
        List edits = consumer.getEdits();
        assertEquals( "Wrong number of entries returned", 2, edits.size() );
        String entry = (String) edits.get( 0 );
        assertTrue( entry.startsWith( "//" ) );
        assertTrue( entry.endsWith( ".classpath" ) );
    }

    public void testBadParse()
        throws Exception
    {
        File testFile = getTestFile( "src/test/resources/perforce/unedit_bad.txt" );

        PerforceUnEditConsumer consumer = new PerforceUnEditConsumer();

        FileInputStream fis = new FileInputStream( testFile );
        BufferedReader in = new BufferedReader( new InputStreamReader( fis ) );
        String s = in.readLine();
        while ( s != null )
        {
            consumer.consumeLine( s );
            s = in.readLine();
        }

        assertFalse( consumer.isSuccess() );
        assertEquals( 96, consumer.getOutput().length() );
    }
}
