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
        assertNotNull( repo.getOutgoingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 1, repo.getOutgoingChangeSetAliases().size() );
        assertEquals( "Change Set Alias is incorrect!", new Integer(1008), repo.getOutgoingChangeSetAliases().get(0));

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
    
    public void testConsumerWithMultipleChangeSets()
    {
    	statusConsumer.consumeLine( "Workspace: (1000) \"GPDBWorkspace\" <-> (1001) \"GPDBStream\"" );
    	statusConsumer.consumeLine( "  Component: (1002) \"GPDB\"" );
    	statusConsumer.consumeLine( "    Baseline: (1003) 49 \"GPDB-MAN-1.0.50\"" );
    	statusConsumer.consumeLine( "    Unresolved:" );
    	statusConsumer.consumeLine( "      a-- /GPDB/GPDBEAR/pom.xml.releaseBackup" );
    	statusConsumer.consumeLine( "      a-- /GPDB/GPDBResources/pom.xml.releaseBackup" );
    	statusConsumer.consumeLine( "      a-- /GPDB/GPDBWeb/pom.xml.releaseBackup" );
    	statusConsumer.consumeLine( "      a-- /GPDB/pom.xml.releaseBackup" );
    	statusConsumer.consumeLine( "    Outgoing:" );
    	statusConsumer.consumeLine( "      Change sets:" );
    	statusConsumer.consumeLine( "        (1012) *--@  \"Release the next release of GPDB.\" - "
    			+ "\"[maven-release-plugin] rollback the release of GPDB-1.0.51\" 02-May-2015 09:38 PM" );
    	statusConsumer.consumeLine( "        (1011) ---@  \"Release the next release of GPDB.\" - "
    			+ "\"[maven-release-plugin] rollback the release of GPDB-1.0.51\" 02-May-2015 09:33 PM" );
    	statusConsumer.consumeLine( "        (1010) ---@  \"Release the next release of GPDB.\" - "
    			+ "\"[maven-release-plugin] prepare release GPDB-1.0.51\" 02-May-2015 09:28 PM" );
    	statusConsumer.consumeLine( "        (1009) ---@  \"Release the next release of GPDB.\" - "
    			+ "\"[maven-release-plugin] rollback the release of GPDB-1.0.51\" 02-May-2015 08:05 PM" );
    	statusConsumer.consumeLine( "        (1008) ---@  \"Release the next release of GPDB.\" - "
    			+ "\"[maven-release-plugin] prepare release GPDB-1.0.51\" 02-May-2015 08:00 PM" );
    	statusConsumer.consumeLine( "        (1007) ---@  \"[maven-release-plugin] rollback the "
    			+ "release of GPDB-1.0.51\" 02-May-2015 07:54 PM" );
    	statusConsumer.consumeLine( "        (1006) ---@  \"[maven-release-plugin] prepare "
    			+ "release GPDB-1.0.51\" 02-May-2015 09:33 PM" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "GPDBWorkspace", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 1000, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "GPDBStream", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 1001, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "GPDB", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "GPDB-MAN-1.0.50", repo.getBaseline() );

        // Test the stream parsing and isPushChanges bits.
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( false );
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( true );
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );


        assertNotNull( repo.getOutgoingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 7, repo.getOutgoingChangeSetAliases().size() );
        assertEquals( "Change Set Alias [0] is incorrect!", new Integer(1012), repo.getOutgoingChangeSetAliases().get(0));
        assertEquals( "Change Set Alias [1] is incorrect!", new Integer(1011), repo.getOutgoingChangeSetAliases().get(1));
        assertEquals( "Change Set Alias [2] is incorrect!", new Integer(1010), repo.getOutgoingChangeSetAliases().get(2));
        assertEquals( "Change Set Alias [3] is incorrect!", new Integer(1009), repo.getOutgoingChangeSetAliases().get(3));
        assertEquals( "Change Set Alias [4] is incorrect!", new Integer(1008), repo.getOutgoingChangeSetAliases().get(4));
        assertEquals( "Change Set Alias [5] is incorrect!", new Integer(1007), repo.getOutgoingChangeSetAliases().get(5));
        assertEquals( "Change Set Alias [6] is incorrect!", new Integer(1006), repo.getOutgoingChangeSetAliases().get(6));
    }

    public void testConsumerWithMultipleChangeSetsAndWorkItems()
    {
    	statusConsumer.consumeLine( "Workspace: (1000) \"GPDBWorkspace\" <-> (1001) \"GPDBStream\"" );
    	statusConsumer.consumeLine( "  Component: (1002) \"GPDB\"" );
    	statusConsumer.consumeLine( "    Baseline: (1003) 49 \"GPDB-MAN-1.0.50\"" );
    	statusConsumer.consumeLine( "    Unresolved:" );
    	statusConsumer.consumeLine( "      a-- /GPDB/GPDBEAR/pom.xml.releaseBackup" );
    	statusConsumer.consumeLine( "      a-- /GPDB/GPDBResources/pom.xml.releaseBackup" );
    	statusConsumer.consumeLine( "      a-- /GPDB/GPDBWeb/pom.xml.releaseBackup" );
    	statusConsumer.consumeLine( "      a-- /GPDB/pom.xml.releaseBackup" );
    	statusConsumer.consumeLine( "    Outgoing:" );
    	statusConsumer.consumeLine( "      Change sets:" );
    	statusConsumer.consumeLine( "        (1012) *--@  62 \"Release the next release of GPDB.\" - "
    			+ "\"[maven-release-plugin] rollback the release of GPDB-1.0.51\" 02-May-2015 09:38 PM" );
    	statusConsumer.consumeLine( "        (1011) ---@  62 \"Release the next release of GPDB.\" - "
    			+ "\"[maven-release-plugin] rollback the release of GPDB-1.0.51\" 02-May-2015 09:33 PM" );
    	statusConsumer.consumeLine( "        (1010) ---@  62 \"Release the next release of GPDB.\" - "
    			+ "\"[maven-release-plugin] prepare release GPDB-1.0.51\" 02-May-2015 09:28 PM" );
    	statusConsumer.consumeLine( "        (1009) ---@  62 \"Release the next release of GPDB.\" - "
    			+ "\"[maven-release-plugin] rollback the release of GPDB-1.0.51\" 02-May-2015 08:05 PM" );
    	statusConsumer.consumeLine( "        (1008) ---@  62 \"Release the next release of GPDB.\" - "
    			+ "\"[maven-release-plugin] prepare release GPDB-1.0.51\" 02-May-2015 08:00 PM" );
    	statusConsumer.consumeLine( "        (1007) ---@  \"[maven-release-plugin] rollback the "
    			+ "release of GPDB-1.0.51\" 02-May-2015 07:54 PM" );
    	statusConsumer.consumeLine( "        (1006) ---@  \"[maven-release-plugin] prepare "
    			+ "release GPDB-1.0.51\" 02-May-2015 09:33 PM" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "GPDBWorkspace", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 1000, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "GPDBStream", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 1001, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "GPDB", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "GPDB-MAN-1.0.50", repo.getBaseline() );

        // Test the stream parsing and isPushChanges bits.
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( false );
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( true );
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );

        assertNotNull( repo.getOutgoingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 7, repo.getOutgoingChangeSetAliases().size() );
        assertEquals( "Change Set Alias [0] is incorrect!", new Integer(1012), repo.getOutgoingChangeSetAliases().get(0));
        assertEquals( "Change Set Alias [1] is incorrect!", new Integer(1011), repo.getOutgoingChangeSetAliases().get(1));
        assertEquals( "Change Set Alias [2] is incorrect!", new Integer(1010), repo.getOutgoingChangeSetAliases().get(2));
        assertEquals( "Change Set Alias [3] is incorrect!", new Integer(1009), repo.getOutgoingChangeSetAliases().get(3));
        assertEquals( "Change Set Alias [4] is incorrect!", new Integer(1008), repo.getOutgoingChangeSetAliases().get(4));
        assertEquals( "Change Set Alias [5] is incorrect!", new Integer(1007), repo.getOutgoingChangeSetAliases().get(5));
        assertEquals( "Change Set Alias [6] is incorrect!", new Integer(1006), repo.getOutgoingChangeSetAliases().get(6));
    }
    
    public void testConsumerUnresolvedIncomingOutgoing()
    {
    	statusConsumer.consumeLine( "Workspace: (1756) \"Scott's GPDBWorkspace\" <-> (1157) \"GPDBStream\"" );
    	statusConsumer.consumeLine( "  Component: (1158) \"GPDB\"" );
    	statusConsumer.consumeLine( "    Baseline: (1718) 25 \"GPDB-1.0.25\"" );
    	statusConsumer.consumeLine( "    Unresolved:" );
    	statusConsumer.consumeLine( "      -c- /GPDB/pom.xml" );
    	statusConsumer.consumeLine( "    Outgoing:" );
    	statusConsumer.consumeLine( "      Change sets:" );
    	statusConsumer.consumeLine( "        (2389) *--@  \"<No comment>\" 23-May-2015 07:09 PM" );
    	statusConsumer.consumeLine( "    Incoming:" );
    	statusConsumer.consumeLine( "      Change sets:" );
    	statusConsumer.consumeLine( "        (2385) ---$ Deb 62 \"Release the next release of GPDB.\""
    	    + " - \"[maven-release-plugin] prepare for next development itera...\" 02-May-2015 11:01 PM" );
    	statusConsumer.consumeLine( "    Baselines:" );
    	statusConsumer.consumeLine( "      (2386) 52 \"GPDB-1.0.53\"" );
    	statusConsumer.consumeLine( "      (2387) 51 \"GPDB-1.0.52\"" );
    	statusConsumer.consumeLine( "      (2388) 50 \"GPDB-1.0.51\"" );
    	statusConsumer.consumeLine( "      (2369) 49 \"GPDB-MAN-1.0.50\"" );
    	statusConsumer.consumeLine( "      (2362) 48 \"GPDB-1.0.50\"" );
    	statusConsumer.consumeLine( "      (2357) 47 \"GPDB-1.0.49\"" );
    	statusConsumer.consumeLine( "      (2352) 46 \"GPDB-1.0.48\"" );
    	statusConsumer.consumeLine( "      (2347) 45 \"GPDB-1.0.47\"" );
    	statusConsumer.consumeLine( "      (2292) 44 \"GPDB-1.0.46\"" );
    	statusConsumer.consumeLine( "      (2285) 42 \"GPDB-1.0.42\"" );
    	statusConsumer.consumeLine( "      (2276) 41 \"GPDB-1.0.41\"" );
    	statusConsumer.consumeLine( "      (2259) 40 \"GPDB-1.0.40\"" );
    	statusConsumer.consumeLine( "      (2250) 39 \"GPDB-1.0.39\"" );
    	statusConsumer.consumeLine( "      (2241) 38 \"GPDB-1.0.38\"" );
    	statusConsumer.consumeLine( "      (2232) 37 \"GPDB-1.0.37\"" );
    	statusConsumer.consumeLine( "      (2222) 36 \"GPDB-1.0.36\"" );
    	statusConsumer.consumeLine( "      (2212) 35 \"GPDB-1.0.35\"" );
    	statusConsumer.consumeLine( "      (2202) 34 \"GPDB-1.0.34\"" );
    	statusConsumer.consumeLine( "      (2191) 33 \"GPDB-1.0.33\"" );
    	statusConsumer.consumeLine( "      (2181) 32 \"GPDB-1.0.32\"" );
    	statusConsumer.consumeLine( "      (2171) 31 \"GPDB-1.0.31\"" );
    	statusConsumer.consumeLine( "      (2160) 30 \"GPDB-1.0.30\"" );
    	statusConsumer.consumeLine( "      (2147) 29 \"GPDB-1.0.29\"" );
    	statusConsumer.consumeLine( "      (2079) 28 \"GPDB-1.0.28\"" );
    	statusConsumer.consumeLine( "      (1851) 27 \"GPDB-1.0.27\"" );
    	statusConsumer.consumeLine( "      (1807) 26 \"GPDB-1.0.26\"" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "Scott's GPDBWorkspace", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 1756, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "GPDBStream", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 1157, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "GPDB", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "GPDB-1.0.25", repo.getBaseline() );

        // Test the stream parsing and isPushChanges bits.
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( false );
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( true );
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );

        assertNotNull( repo.getOutgoingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 1, repo.getOutgoingChangeSetAliases().size() );
        assertEquals( "Change Set Alias [0] is incorrect!", new Integer(2389), repo.getOutgoingChangeSetAliases().get(0));

        assertNotNull( repo.getIncomingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 1, repo.getIncomingChangeSetAliases().size() );
        assertEquals( "Change Set Alias [0] is incorrect!", new Integer(2385), repo.getIncomingChangeSetAliases().get(0));
    }
    
    public void testCollision()
    {
    	statusConsumer.consumeLine( "Workspace: (8551) \"myNewWorkspace\" <-> (8552) \"stream19_test_max_results_1256765247692134\"" );
    	statusConsumer.consumeLine( "  Component: (8553) \"Flux Capacitor\"" );
    	statusConsumer.consumeLine( "    Baseline: (8554) 1 \"Initial Baseline\"" );
    	statusConsumer.consumeLine( "    Outgoing:" );
    	statusConsumer.consumeLine( "      Change sets:" );
    	statusConsumer.consumeLine( "        (8617) -#@ \"Update from November planning meeting\"" );
    	statusConsumer.consumeLine( "          Changes:" );
    	statusConsumer.consumeLine( "            -#-c /flux.capacitor/requirements.txt" );
    	statusConsumer.consumeLine( "    Incoming:" );
    	statusConsumer.consumeLine( "      Change sets:" );
    	statusConsumer.consumeLine( "        (8616) -#$ \"Results of initial trials\"" );
    	statusConsumer.consumeLine( "          Changes:" );
    	statusConsumer.consumeLine( "            -#-c /flux.capacitor/requirements.txt" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "myNewWorkspace", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 8551, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "stream19_test_max_results_1256765247692134", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 8552, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "Flux Capacitor", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "Initial Baseline", repo.getBaseline() );

        // Test the stream parsing and isPushChanges bits.
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( false );
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( true );
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );

        assertNotNull( repo.getOutgoingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 1, repo.getOutgoingChangeSetAliases().size() );
        assertEquals( "Change Set Alias [0] is incorrect!", new Integer(8617), repo.getOutgoingChangeSetAliases().get(0));

        assertNotNull( repo.getIncomingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 1, repo.getIncomingChangeSetAliases().size() );
        assertEquals( "Change Set Alias [0] is incorrect!", new Integer(8616), repo.getIncomingChangeSetAliases().get(0));

//        // Test the ScmFile and ScmFileStatus bits. (Needs a bit of work here)
//        List<ScmFile> changedFiles = statusConsumer.getChangedFiles();
//        assertNotNull( changedFiles );
//        assertEquals( 1, changedFiles.size() );
//        assertTrue( changedFiles.contains( new ScmFile( "/flux.capacitor/requirements.txt", ScmFileStatus.CONFLICT ) ) );
    }
    
    public void testIncoming()
    {
    	statusConsumer.consumeLine( "Workspace: (1026) \"DEV-build-POC-Builder\" <-> (1011) \"DEV-build-Management-Release plugin POC\"" );
    	statusConsumer.consumeLine( "  Component: (1095) \"FW-Maven-ReleasePlugin-POC\"" );
    	statusConsumer.consumeLine( "    Baseline: (1103) 2 \"release_poc-build-0.0.3\"" );
    	statusConsumer.consumeLine( "    Incoming:" );
    	statusConsumer.consumeLine( "      Change sets:" );
    	statusConsumer.consumeLine( "        (1106) ---$  28383 \"Detemine more efficient way to perform releases using mav...\" - \"Updated SCM settings\" 05-May-2015 10:26 AM" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "DEV-build-POC-Builder", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 1026, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "DEV-build-Management-Release plugin POC", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 1011, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "FW-Maven-ReleasePlugin-POC", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "release_poc-build-0.0.3", repo.getBaseline() );

        // Test the stream parsing and isPushChanges bits.
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( false );
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( true );
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );

        assertNotNull( repo.getIncomingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 1, repo.getIncomingChangeSetAliases().size() );
        assertEquals( "Change Set Alias [0] is incorrect!", new Integer(1106), repo.getIncomingChangeSetAliases().get(0));
    }
    
    public void testMultipleIncoming()
    {
    	statusConsumer.consumeLine( "Workspace: (1000) \"Chris-Blah-Workspace\" <-> (1001) \"CHRIS_Blah_Stream\"" );
    	statusConsumer.consumeLine( "  Component: (1002) \"Data_Models\"" );
    	statusConsumer.consumeLine( "    Baseline: (1003) 465 \"CHRIS_BLAH_20150516_190700_1\"" );
    	statusConsumer.consumeLine( "    Unresolved:" );
    	statusConsumer.consumeLine( "      -c- /Data Model/.settings/org.eclipse.core.resources.prefs" );
    	statusConsumer.consumeLine( "    Incoming:" );
    	statusConsumer.consumeLine( "      Change sets:" );
    	statusConsumer.consumeLine( "        (1004) ---$ chrisgwarp 1573 \"Manage Work Order Business Spec\" - \"PROJ-1001 - Added Hist...\" 15-May-2015 12:53 PM" );
    	statusConsumer.consumeLine( "        (1005) ---$ chrisgwarp 1573 \"Manage Work Order Business Spec\" - \"PROJ-1001 - Removed Error Code 020...\" 14-May-2015 05:59 PM" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "Chris-Blah-Workspace", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 1000, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "CHRIS_Blah_Stream", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 1001, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "Data_Models", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "CHRIS_BLAH_20150516_190700_1", repo.getBaseline() );

        // Test the stream parsing and isPushChanges bits.
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( false );
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( true );
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );

        assertNotNull( repo.getIncomingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 2, repo.getIncomingChangeSetAliases().size() );
        assertEquals( "Change Set Alias [0] is incorrect!", new Integer(1004), repo.getIncomingChangeSetAliases().get(0));
        assertEquals( "Change Set Alias [1] is incorrect!", new Integer(1005), repo.getIncomingChangeSetAliases().get(1));
    }
    
    public void testIncomingOutgoing()
    {
    	statusConsumer.consumeLine( "Workspace: (8551) \"myNewWorkspace\" <-> (8552) \"stream19_test_max_results_1256765247692134\"" );
    	statusConsumer.consumeLine( "  Component: (8553) \"Flux Capacitor\"" );
    	statusConsumer.consumeLine( "    Baseline: (8554) 1 \"Initial Baseline\"" );
    	statusConsumer.consumeLine( "    Outgoing:" );
    	statusConsumer.consumeLine( "      Change sets:" );
    	statusConsumer.consumeLine( "        (8556) ---@" );
    	statusConsumer.consumeLine( "          Changes:" );
    	statusConsumer.consumeLine( "            ---c- /flux.capacitor/requirements.txt" );
    	statusConsumer.consumeLine( "    Incoming:" );
    	statusConsumer.consumeLine( "      Change sets:" );
    	statusConsumer.consumeLine( "        (8615) ---$ \"Initial layout\"" );
    	statusConsumer.consumeLine( "          Changes:" );
    	statusConsumer.consumeLine( "            ---c- /flux.capacitor/diagrams/design.cad" );

        // Test the additional collected data, Workspace, Component, Baseline.
        assertEquals( "Workspace is incorrect!", "myNewWorkspace", repo.getWorkspace() );
        assertEquals( "Workspace Alias is incorrect!", 8551, repo.getWorkspaceAlias() );
        assertEquals( "Flow Target is incorrect!", "stream19_test_max_results_1256765247692134", repo.getFlowTarget() );
        assertEquals( "Flow Target Alias is incorrect!", 8552, repo.getFlowTargetAlias() );
        assertEquals( "Component is incorrect!", "Flux Capacitor", repo.getComponent() );
        assertEquals( "Baseline is incorrect!", "Initial Baseline", repo.getBaseline() );

        // Test the stream parsing and isPushChanges bits.
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( false );
        assertFalse( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );
        repo.setPushChanges( true );
        assertTrue( "isPushChangesAndHaveFlowTargets is incorrect!", repo.isPushChangesAndHaveFlowTargets() );

        assertNotNull( repo.getOutgoingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 1, repo.getOutgoingChangeSetAliases().size() );
        assertEquals( "Change Set Alias [0] is incorrect!", new Integer(8556), repo.getOutgoingChangeSetAliases().get(0));

        assertNotNull( repo.getIncomingChangeSetAliases() );
        assertEquals( "Change Set Alias length is incorrect!", 1, repo.getIncomingChangeSetAliases().size() );
        assertEquals( "Change Set Alias [0] is incorrect!", new Integer(8615), repo.getIncomingChangeSetAliases().get(0));
    }
    
    public void testServerUnreachable1()
    {
    	statusConsumer.consumeLine( "Workspace: (1000) \"GPDBWorkspace\" (This workspace is unreachable.)" );
    	statusConsumer.consumeLine( "  Could not log in to https://rtc:9444/jazz/ as user Deb: CRJAZ2384E Cannot" );
    	statusConsumer.consumeLine( "  connect to the repository at URL \"https://rtc:9444/jazz\", see the nested" );
    	statusConsumer.consumeLine( "  exception for more details. For more details, open the help system and search" );
    	statusConsumer.consumeLine( "  for CRJAZ2384E." );
    	
    }
    
    public void testServerUnreachable2()
    {
    	statusConsumer.consumeLine( "Workspace: (1000) \"Chris-Blah-Workspace\" (This workspace is unreachable.)" );
    	statusConsumer.consumeLine( "  Could not determine the URI required to connect to the repository. The UUID of" );
    	statusConsumer.consumeLine( "  the repository is _Bzjnksdkmfsaklmz-5uTdf. If you know the repository URI run" );
    	statusConsumer.consumeLine( "  'login' command providing the repository URI. If not, please contact your" );
    	statusConsumer.consumeLine( "  administrator." );
    	
    }
    
    public void testServerUnreachable3()
    {
        statusConsumer.consumeLine( "Workspace: (----) \"Chris-Project-XXXXXX-Workspace\" (This workspace is unreachable.)" );
        statusConsumer.consumeLine( "  Could not determine the URI required to connect to the repository. The UUID of" );
        statusConsumer.consumeLine( "  the repository is _ZdjnafkjnEmkEW5-4HuDag. If you know the repository URI run" );
        statusConsumer.consumeLine( "  'login' command providing the repository URI. If not, please contact your" );
        statusConsumer.consumeLine( "  administrator." );

    }
}
