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
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitChangeLogConsumerTest
    extends PlexusTestCase
{
    
    public void testConsumer1()
    throws Exception
    {
        GitChangeLogConsumer consumer = new GitChangeLogConsumer( new DefaultLog(), null );

        File f = getTestFile( "/src/test/resources/git/changelog/gitwhatchanged.gitlog" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List modifications = consumer.getModifications();
        
        assertEquals( 6, modifications.size() );

        for ( Iterator i = modifications.iterator(); i.hasNext(); )
        {
            ChangeSet entry = (ChangeSet) i.next();
            
            assertEquals( "Mark Struberg <struberg@yahoo.de>", entry.getAuthor() );

            assertNotNull( entry.getDate() );
            
            assertTrue( entry.getComment() != null && entry.getComment().length() > 0 );
            
            assertNotNull( entry.getFiles() );
            assertFalse( entry.getFiles().isEmpty() );
        }    
        
        ChangeSet entry = (ChangeSet) modifications.get( 3 );
        
        assertEquals( "Mark Struberg <struberg@yahoo.de>", entry.getAuthor() );

        assertNotNull( entry.getDate() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z" );
        sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        
        assertEquals( "2007-11-24 00:10:42 +0000", sdf.format( entry.getDate() ) );
        
        assertEquals( "/ added" , entry.getComment() );
        
        assertNotNull( entry.getFiles() );
        ChangeFile cf = (ChangeFile) entry.getFiles().get( 0 );
        assertEquals( "readme.txt", cf.getName()  );
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
        
        List modifications = consumer.getModifications();
        
        for ( Iterator i = modifications.iterator(); i.hasNext(); )
        {
            ChangeSet entry = (ChangeSet) i.next();
            
            assertEquals( "Mark Struberg <struberg@yahoo.de>", entry.getAuthor() );

            assertNotNull( entry.getDate() );
            
            assertTrue( entry.getComment() != null && entry.getComment().length() > 0 );
            
            assertNotNull( entry.getFiles() );
            assertFalse( entry.getFiles().isEmpty() );
        }    
        
        assertEquals( 8, modifications.size() );

        ChangeSet entry = (ChangeSet) modifications.get( 4 );
        
        assertEquals( "Mark Struberg <struberg@yahoo.de>", entry.getAuthor() );

        assertNotNull( entry.getDate() );
        SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss Z" );
        sdf.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
        
        assertEquals( "2007-11-27 13:05:36 +0000", sdf.format( entry.getDate() ) );

        assertEquals( "fixed a GitCommandLineUtil and provice first version of the checkin command." , entry.getComment() );
        
        assertNotNull( entry.getFiles() );
        
        assertEquals( 10, entry.getFiles().size() );
        
        ChangeFile cf = (ChangeFile) entry.getFiles().get( 0 );
        assertEquals( "maven-scm-provider-gitexe/src/main/java/org/apache/maven/scm/provider/git/gitexe/command/GitCommandLineUtils.java", cf.getName()  );
        assertTrue( cf.getRevision() != null && cf.getRevision().length() > 0 );
    }
 
}
