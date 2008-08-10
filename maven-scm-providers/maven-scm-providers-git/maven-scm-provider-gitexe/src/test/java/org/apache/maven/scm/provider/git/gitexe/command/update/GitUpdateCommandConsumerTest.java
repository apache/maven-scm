package org.apache.maven.scm.provider.git.gitexe.command.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.ReaderFactory;

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

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 10 août 2008
 * @version $Id$
 */
public class GitUpdateCommandConsumerTest
    extends PlexusTestCase
{
    public void testUpToDate()
        throws Exception
    {

        GitUpdateCommandConsumer consumer = buildGitUpdateCommandConsumer( "/src/test/resources/git/update/git-update-up-to-date.out" );

        List changedFiles = consumer.getUpdatedFiles();

        assertEquals( 0, changedFiles.size() );

    }
    
    public void testTwoModified()
        throws Exception
    {

        GitUpdateCommandConsumer consumer = buildGitUpdateCommandConsumer( "/src/test/resources/git/update/git-update.out" );

        List changedFiles = consumer.getUpdatedFiles();

        assertEquals( 2, changedFiles.size() );
        
        assertScmFile( (ScmFile) changedFiles.get( 0 ), "README", ScmFileStatus.UPDATED );
        
        assertScmFile( (ScmFile) changedFiles.get( 1 ), "pom.xml", ScmFileStatus.UPDATED );

    }    
    
    public void testAddDeleteFile()
        throws Exception
    {

        GitUpdateCommandConsumer consumer = buildGitUpdateCommandConsumer( "/src/test/resources/git/update/git-update-add-delete.out" );

        List changedFiles = consumer.getUpdatedFiles();

        assertEquals( 3, changedFiles.size() );

        assertScmFile( (ScmFile) changedFiles.get( 0 ), "README", ScmFileStatus.DELETED );
        
        assertScmFile( (ScmFile) changedFiles.get( 1 ), "pom.xml", ScmFileStatus.UPDATED );

        assertScmFile( (ScmFile) changedFiles.get( 2 ), "test.txt", ScmFileStatus.ADDED );

    }        
    
    public void testOneUpdate()
        throws Exception
    {
        assertOneUpdate( "/src/test/resources/git/update/git-update-one.out" );
    }
    
    public void testOneUpdateOtherFormat()
        throws Exception
    {
        assertOneUpdate( "/src/test/resources/git/update/git-update-one-other-format.out" );
    }
    
    
    
    // utils methods

    private void assertOneUpdate( String fileName )
        throws Exception
    {
        GitUpdateCommandConsumer consumer = buildGitUpdateCommandConsumer( fileName );
        List changedFiles = consumer.getUpdatedFiles();

        assertEquals( 1, changedFiles.size() );

        assertScmFile( (ScmFile) changedFiles.get( 0 ), "pom.xml", ScmFileStatus.UPDATED );
    }
    
    private GitUpdateCommandConsumer buildGitUpdateCommandConsumer( String fileName )
        throws Exception
    {
        GitUpdateCommandConsumer consumer = new GitUpdateCommandConsumer( new DefaultLog(), null );

        BufferedReader r = getGitLogBufferedReader( fileName );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            //System.out.println(" line " + line );
            consumer.consumeLine( line );
        }
        return consumer;
    }

    private BufferedReader getGitLogBufferedReader( String fileName )
        throws Exception
    {
        File f = getTestFile( fileName );
        Reader reader = ReaderFactory.newReader( f, "UTF-8" );
        return new BufferedReader( reader );
    }

    private void assertScmFile( ScmFile file, String fileName, ScmFileStatus status )
    {
        assertEquals( fileName, file.getPath() );
        assertEquals( status, file.getStatus() );
    }

}
