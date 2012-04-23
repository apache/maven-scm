package org.apache.maven.scm.provider.jazz.command.list;

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

import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.jazz.JazzScmTestCase;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzListCommandTest
    extends JazzScmTestCase
{
    private JazzListConsumer listConsumer;

    private JazzScmProviderRepository repo;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        repo = getScmProviderRepository();

        listConsumer = new JazzListConsumer( getScmProviderRepository(), new DefaultLog() );
        
        // Simulate the output of the parsing of the "scm status" command.
        // IE, fill in the workspace and component details
        // Needed for the remote workspace and component name for the list remotefiles
        repo.setWorkspace( "Dave's Repository Workspace" );
        repo.setComponent( "Dave's Component" );
    }

    public void testCreateListCommand()
        throws Exception
    {
        Commandline cmd = new JazzListCommand().createListCommand( repo, getScmFileSet(), true, null ).getCommandline();
        String expected = "scm list remotefiles --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword \"Dave's Repository Workspace\" \"Dave's Component\"";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testConsumer()
    {
        listConsumer.consumeLine( "/" );
        listConsumer.consumeLine( "/BogusTestJazz/" );
        listConsumer.consumeLine( "/BogusTestJazz/.jazzignore" );
        listConsumer.consumeLine( "/BogusTestJazz/pom.xml" );
        listConsumer.consumeLine( "/BogusTestJazz/Readme.txt" );
        listConsumer.consumeLine( "/BogusTestJazz/src/" );
        listConsumer.consumeLine( "/BogusTestJazz/src/main/" );
        listConsumer.consumeLine( "/BogusTestJazz/src/main/resources/" );
        listConsumer.consumeLine( "/BogusTestJazz/src/main/resources/AFile.txt" );
        listConsumer.consumeLine( "/BogusTestJazz/src/main/java/" );
        listConsumer.consumeLine( "/BogusTestJazz/src/main/java/BogusTest.java" );    

        // Test the ScmFile and ScmFileStatus bits.
        List<ScmFile> changedFiles = listConsumer.getFiles();
        assertNotNull( changedFiles );
        assertEquals( 11, changedFiles.size() );
        assertTrue( changedFiles.contains( new ScmFile(
                                                        "/",
                                                        ScmFileStatus.CHECKED_IN ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
                                                       "/BogusTestJazz/",
                                                       ScmFileStatus.CHECKED_IN ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
                                                       "/BogusTestJazz/.jazzignore",
                                                       ScmFileStatus.CHECKED_IN ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
                                                       "/BogusTestJazz/pom.xml",
                                                       ScmFileStatus.CHECKED_IN ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
                                                       "/BogusTestJazz/Readme.txt",
                                                       ScmFileStatus.CHECKED_IN ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
                                                       "/BogusTestJazz/src/",
                                                       ScmFileStatus.CHECKED_IN ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
                                                       "/BogusTestJazz/src/main/",
                                                       ScmFileStatus.CHECKED_IN ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
                                                       "/BogusTestJazz/src/main/resources/",
                                                       ScmFileStatus.CHECKED_IN ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
                                                       "/BogusTestJazz/src/main/resources/AFile.txt",
                                                       ScmFileStatus.CHECKED_IN ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
                                                       "/BogusTestJazz/src/main/java/",
                                                       ScmFileStatus.CHECKED_IN ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
                                                       "/BogusTestJazz/src/main/java/BogusTest.java",
                                                       ScmFileStatus.CHECKED_IN ) ) );
    }

}
