package org.apache.maven.scm.provider.git.gitexe.command.changelog;

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

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitChangeLogConsumerTest
    extends PlexusTestCase
{

    public void testConsumer1()
        throws Exception
    {
        //was  Date:   Tue Nov 27 16:16:28 2007 +0100
        //iso  Date:   2007-11-24 01:13:10 +0100
        Pattern datePattern = Pattern.compile( "^Date:\\s*(.*)" );//new RE( "^Date:\\s*\\w-1\\w-1\\w-1\\s(.*)" );
        Matcher matcher = datePattern.matcher( "Date:   2007-11-24 01:13:10 +0100" );
        
        assertTrue( matcher.matches() );
        assertEquals("2007-11-24 01:13:10 +0100", matcher.group( 1 ));

        GitChangeLogConsumer consumer = new GitChangeLogConsumer( new DefaultLog(), null );

        File f = getTestFile( "/src/test/resources/git/changelog/gitwhatchanged.gitlog" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List<ChangeSet> modifications = consumer.getModifications();

        assertEquals( 6, modifications.size() );

        for ( Iterator<ChangeSet> i = modifications.iterator(); i.hasNext(); )
        {
            ChangeSet entry = i.next();

            assertEquals( "Mark Struberg <struberg@yahoo.de>", entry.getAuthor() );

            assertNotNull( entry.getDate() );

            assertTrue( entry.getComment() != null && entry.getComment().length() > 0 );

            assertNotNull( entry.getRevision() );

            assertNotNull( entry.getFiles() );
            assertFalse( entry.getFiles().isEmpty() );
        }

        ChangeSet entry = modifications.get( 3 );

        assertEquals( "Mark Struberg <struberg@yahoo.de>", entry.getAuthor() );

        assertNotNull( entry.getDate() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z" );
        sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

        assertEquals( "2007-11-24 00:10:42 +0000", sdf.format( entry.getDate() ) );

        assertEquals( "895d423689da3b36d8e9106c0ecbf3d62433978c", entry.getRevision() );

        assertEquals( "/ added", entry.getComment() );

        assertNotNull( entry.getFiles() );
        ChangeFile cf = entry.getFiles().get( 0 );
        assertEquals( "readme.txt", cf.getName() );
        assertTrue( cf.getRevision() != null && cf.getRevision().length() > 0 );
    }

    public void testConsumer2()
        throws Exception
    {
        GitChangeLogConsumer consumer = new GitChangeLogConsumer( new DefaultLog(), null );

        File f = getTestFile( "/src/test/resources/git/changelog/gitwhatchanged2.gitlog" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List<ChangeSet> modifications = consumer.getModifications();

        // must use *Linked* HashMap to have predictable toString
        final Map<ScmFileStatus, AtomicInteger> summary = new LinkedHashMap<ScmFileStatus, AtomicInteger>();

        for ( Iterator<ChangeSet> i = modifications.iterator(); i.hasNext(); )
        {
            ChangeSet entry = i.next();

            assertEquals( "Mark Struberg <struberg@yahoo.de>", entry.getAuthor() );

            assertNotNull( entry.getDate() );

            assertTrue( entry.getComment() != null && entry.getComment().length() > 0 );

            assertNotNull( entry.getRevision() );

            assertNotNull( entry.getFiles() );
            assertFalse( entry.getFiles().isEmpty() );

            for ( ChangeFile file : entry.getFiles() )
            {
                final ScmFileStatus action = file.getAction();
                if ( !summary.containsKey( action ) )
                {
                    summary.put( action, new AtomicInteger() );
                }
                summary.get( action ).incrementAndGet();
            }
        }
        Assert.assertEquals( "Action summary differs from expectations", "{modified=21, added=88, deleted=1}",
                             summary.toString() );

        assertEquals( 8, modifications.size() );

        ChangeSet entry = modifications.get( 4 );

        assertEquals( "Mark Struberg <struberg@yahoo.de>", entry.getAuthor() );

        assertNotNull( entry.getDate() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z" );
        sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

        assertEquals( "2007-11-27 13:05:36 +0000", sdf.format( entry.getDate() ) );

        assertEquals( "52733aa427041cafd760833cb068ffe897fd35db", entry.getRevision() );

        assertEquals( "fixed a GitCommandLineUtil and provice first version of the checkin command.",
                      entry.getComment() );

        assertNotNull( entry.getFiles() );

        assertEquals( 10, entry.getFiles().size() );

        ChangeFile cf = entry.getFiles().get( 0 );
        assertEquals(
            "maven-scm-provider-gitexe/src/main/java/org/apache/maven/scm/provider/git/gitexe/command/GitCommandLineUtils.java",
            cf.getName() );
        assertTrue( cf.getRevision() != null && cf.getRevision().length() > 0 );
    }

}
