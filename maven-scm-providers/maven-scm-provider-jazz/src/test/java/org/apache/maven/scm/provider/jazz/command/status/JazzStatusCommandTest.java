package org.apache.maven.scm.provider.jazz.command.status;

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
import org.apache.maven.scm.provider.jazz.JazzScmTestCase;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.List;

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzStatusCommandTest
    extends JazzScmTestCase
{
    private JazzScmProviderRepository repo;

    private JazzStatusConsumer statusConsumer;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        repo = getScmProviderRepository();
        statusConsumer = new JazzStatusConsumer( repo, new DefaultLog() );
    }

    public void testCreateStatusCommand()
        throws Exception
    {
        Commandline cmd = new JazzStatusCommand().createStatusCommand( repo, getScmFileSet() ).getCommandline();
        String expected = "scm status --username myUserName --password myPassword --wide";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testConsumer()
    {
        statusConsumer.consumeLine(
            "Workspace: (1000) \"Dave's Repository Workspace\" <-> (1001) \"SCM Plugin Stream\"" );
        statusConsumer.consumeLine( "  Component: (1002) \"SCM Plugins\"" );
        statusConsumer.consumeLine( "    Baseline: (1003) 1 \"Initial Baseline\"" );
        statusConsumer.consumeLine( "    Unresolved:" );
        statusConsumer.consumeLine(
            "      d- /status-test-project/src/main/java/org/apache/maven/scm/provider/jazz/JazzScmProvider.java" );
        statusConsumer.consumeLine(
            "      a- /status-test-project/src/main/java/org/apache/maven/scm/provider/jazz/JazzScmProviderRenamed.java" );
        statusConsumer.consumeLine(
            "      d- /status-test-project/src/main/java/org/apache/maven/scm/provider/jazz/DeletedFile.java" );
        statusConsumer.consumeLine(
            "      a- /status-test-project/src/main/java/org/apache/maven/scm/provider/jazz/AddedFile.java" );
        statusConsumer.consumeLine(
            "      -c /status-test-project/src/main/java/org/apache/maven/scm/provider/jazz/ModifiedFile.java" );
        statusConsumer.consumeLine( "    Outgoing:" );
        statusConsumer.consumeLine( "      Change sets:" );
        statusConsumer.consumeLine( "        (1008) --@ <No comment>" );
        statusConsumer.consumeLine( "" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "Dave's Repository Workspace", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 1000, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "SCM Plugin Stream", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 1001, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "SCM Plugins", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "Initial Baseline", repo.getBaseline() );
        assertEquals( "Change Set Alias is incorrect!", 1008, repo.getChangeSetAlias());

        // Test the stream parsing and isPushChanges bits.
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );

        // Test the ScmFile and ScmFileStatus bits.
        List<ScmFile> changedFiles = statusConsumer.getChangedFiles();
        assertNotNull( changedFiles );
        assertEquals( 5, changedFiles.size() );
        assertTrue( changedFiles.contains(
            new ScmFile( "/status-test-project/src/main/java/org/apache/maven/scm/provider/jazz/JazzScmProvider.java",
                         ScmFileStatus.DELETED ) ) );
        assertTrue( changedFiles.contains( new ScmFile(
            "/status-test-project/src/main/java/org/apache/maven/scm/provider/jazz/JazzScmProviderRenamed.java",
            ScmFileStatus.ADDED ) ) );
        assertTrue( changedFiles.contains(
            new ScmFile( "/status-test-project/src/main/java/org/apache/maven/scm/provider/jazz/DeletedFile.java",
                         ScmFileStatus.DELETED ) ) );
        assertTrue( changedFiles.contains(
            new ScmFile( "/status-test-project/src/main/java/org/apache/maven/scm/provider/jazz/AddedFile.java",
                         ScmFileStatus.ADDED ) ) );
        assertTrue( changedFiles.contains(
            new ScmFile( "/status-test-project/src/main/java/org/apache/maven/scm/provider/jazz/ModifiedFile.java",
                         ScmFileStatus.MODIFIED ) ) );
    }

    public void testConsumerWithStream()
    {
        statusConsumer.consumeLine( "Workspace: (1156) \"GPDBWorkspace\" <-> (1157) \"GPDBStream\"" );
        statusConsumer.consumeLine( "  Component: (1158) \"GPDB\" <-> (1157) \"GPDBStream\"" );
        statusConsumer.consumeLine( "    Baseline: (1159) 1 \"Initial Baseline\"" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "GPDBWorkspace", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 1156, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "GPDBStream", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 1157, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "GPDB", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "Initial Baseline", repo.getBaseline() );

        // Test the stream parsing and isPushChanges bits.
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( false );
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( true );
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
    }

    public void testConsumerWithOutStream()
    {
        statusConsumer.consumeLine( "Workspace: (1156) \"GPDBWorkspace\" <-> (1156) \"GPDBWorkspace\"" );
        statusConsumer.consumeLine( "  Component: (1158) \"GPDB\"" );
        statusConsumer.consumeLine( "    Baseline: (1159) 1 \"Initial Baseline\"" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "GPDBWorkspace", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 1156, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "GPDBWorkspace", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 1156, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "GPDB", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "Initial Baseline", repo.getBaseline() );

        // Test the stream parsing and isPushChanges bits.
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( false );
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( true );
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
    }

    public void testConsumerWithAdditionalInfo()
    {
        statusConsumer.consumeLine(
            "Workspace: (1000) \"MavenStream Workspace\" <-> (1005) \"MavenStream Workspace\"" );
        statusConsumer.consumeLine(
            "  Component: (1002) \"FireDragon\" <-> (1005) \"MavenR3Stream Workspace\" (outgoing addition)" );
        statusConsumer.consumeLine( "    Baseline: (1003) 1 \"Initial Baseline\"" );
        statusConsumer.consumeLine( "    Unresolved:" );
        statusConsumer.consumeLine( "      a-- /FireDragon/.project" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "MavenStream Workspace", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 1000, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "MavenStream Workspace", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 1005, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "FireDragon", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "Initial Baseline", repo.getBaseline() );
    }
}
