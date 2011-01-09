package org.apache.maven.scm.provider.perforce.command.changelog;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class PerforceDescribeConsumerTest
    extends ScmTestCase
{
    public void testParse()
        throws Exception
    {
        File testFile = getTestFile( "src/test/resources/perforce/perforcedescribelog.txt" );

        PerforceDescribeConsumer consumer =
            new PerforceDescribeConsumer( "//depot/test", null, new DefaultLog() );

        FileInputStream fis = new FileInputStream( testFile );
        BufferedReader in = new BufferedReader( new InputStreamReader( fis ) );
        String s = in.readLine();
        while ( s != null )
        {
            consumer.consumeLine( s );
            s = in.readLine();
        }

        List<ChangeSet> entries = new ArrayList<ChangeSet>( consumer.getModifications() );
        assertEquals( "Wrong number of entries returned", 7, entries.size() );
        ChangeSet entry = entries.get(0);
        assertEquals( "mcronin", entry.getAuthor() );
        assertEquals( "Wrong number of files returned", 3, entry.getFiles().size() );
        assertEquals( "demo/demo.c", ( (ChangeFile) entry.getFiles().get( 0 ) ).getName() );
        assertEquals( "2003-08-07", entry.getDateFormatted() );
        assertEquals( "17:21:57", entry.getTimeFormatted() );
        entry = entries.get(6);
        assertEquals( "jim", entry.getAuthor() );
        assertEquals( "Wrong number of files returned", 1, entry.getFiles().size() );
        assertEquals( "junk/linefeed.txt", ( (ChangeFile) entry.getFiles().get( 0 ) ).getName() );
    }
}
