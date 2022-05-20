package org.apache.maven.scm.provider.git.gitexe.command.remove;

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
import org.apache.maven.scm.util.ConsumerUtils;
import org.codehaus.plexus.PlexusTestCase;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitRemoveConsumerTest
    extends PlexusTestCase
{
    
    public void testConsumerRemovedFile() 
    {
        GitRemoveConsumer consumer = new GitRemoveConsumer();
        
        consumer.consumeLine( "rm 'project.xml'" );
        
        List<ScmFile> changedFiles = consumer.getRemovedFiles();
        
        assertNotNull( changedFiles );
        assertEquals( 1, changedFiles.size() );
    }
    
    public void testLog1Consumer()
    throws Exception
    {
        GitRemoveConsumer consumer = new GitRemoveConsumer();

        File f = getTestFile( "/src/test/resources/git/remove/gitrm.gitlog" );

        ConsumerUtils.consumeFile( f, consumer );

        List<ScmFile> changedFiles = consumer.getRemovedFiles();
        
        assertEquals( 2, changedFiles.size() );

        testScmFile( (ScmFile) changedFiles.get( 0 ), "src/main/java/Application.java", ScmFileStatus.DELETED );
        testScmFile( (ScmFile) changedFiles.get( 1 ), "src/test/java/Test.java" , ScmFileStatus.DELETED );
    }
 
    public void testEmptyLogConsumer()
    throws Exception
    {
        GitRemoveConsumer consumer = new GitRemoveConsumer();

        File f = getTestFile( "/src/test/resources/git/remove/gitrm-empty.gitlog" );

        ConsumerUtils.consumeFile( f, consumer );

        List<ScmFile> changedFiles = consumer.getRemovedFiles();
        
        assertEquals( 0, changedFiles.size() );
   }    
    
    private void testScmFile( ScmFile fileToTest, String expectedFilePath, ScmFileStatus expectedStatus )
    {
        assertEquals( expectedFilePath, fileToTest.getPath() );
        assertEquals( expectedStatus, fileToTest.getStatus() );
    }
 
}
