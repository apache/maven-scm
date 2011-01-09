package org.apache.maven.scm.provider.git.command.diff;

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
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitDiffConsumerTest
    extends PlexusTestCase
{

    public void testEmptyLogConsumer()
        throws Exception
    {
        GitDiffConsumer consumer = new GitDiffConsumer( new DefaultLog(), null );

        File f = getTestFile( "/src/test/resources/git/diff/git-diff-empty.log" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List<ScmFile> changedFiles = consumer.getChangedFiles();

        assertEquals( 0, changedFiles.size() );
    }

    public void testLog1Consumer()
        throws Exception
    {
        GitDiffConsumer consumer = new GitDiffConsumer( new DefaultLog(), null );

        File f = getTestFile( "src/test/resources/git/diff/git-diff1.log" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List<ScmFile> changedFiles = consumer.getChangedFiles();

        assertEquals( 1, changedFiles.size() );

        testScmFile( (ScmFile) changedFiles.get( 0 ), "olamy.test", ScmFileStatus.MODIFIED );

        Map<String,CharSequence> differences = consumer.getDifferences();
        assertNotNull( differences );

        StringBuilder readmeDiffs = new StringBuilder( differences.get( "olamy.test" ) );
        assertNotNull( readmeDiffs );
        assertTrue( readmeDiffs.indexOf( "+new line" ) >= 0 );
    }

    public void testLog2Consumer()
        throws Exception
    {
        GitDiffConsumer consumer = new GitDiffConsumer( new DefaultLog(), null );

        File f = getTestFile( "src/test/resources/git/diff/git-diff2.log" );

        BufferedReader r = new BufferedReader( new FileReader( f ) );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            consumer.consumeLine( line );
        }

        List<ScmFile> changedFiles = consumer.getChangedFiles();

        assertEquals( 2, changedFiles.size() );

        testScmFile( changedFiles.get( 0 ), "pom.xml", ScmFileStatus.MODIFIED );

        testScmFile( changedFiles.get( 1 ), "test.txt", ScmFileStatus.MODIFIED );

        Map<String,CharSequence> differences = consumer.getDifferences();
        assertNotNull( differences );

        StringBuilder addDiffs = new StringBuilder( differences.get( "pom.xml" ) );
        assertNotNull( addDiffs );
        assertTrue( addDiffs.indexOf( "+  <!-- test -->" ) >= 0 );

        addDiffs = new StringBuilder( differences.get( "test.txt" ) );
        assertNotNull( addDiffs );
        assertTrue( addDiffs.indexOf( "+maven-scm git provider works fine :-)" ) >= 0 );
    }

    private void testScmFile( ScmFile fileToTest, String expectedFilePath, ScmFileStatus expectedStatus )
    {
        assertEquals( expectedFilePath, fileToTest.getPath() );
        assertEquals( expectedStatus, fileToTest.getStatus() );
    }
 
}