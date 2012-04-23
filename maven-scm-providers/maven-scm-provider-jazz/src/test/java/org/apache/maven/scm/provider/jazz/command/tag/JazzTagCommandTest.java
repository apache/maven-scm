package org.apache.maven.scm.provider.jazz.command.tag;

import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.provider.jazz.JazzScmTestCase;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

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

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzTagCommandTest
    extends JazzScmTestCase
{
    private JazzScmProviderRepository repo;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        repo = getScmProviderRepository();

        // Simulate the output of the parsing of the "scm status" command.
        // IE, fill in the workspace and stream details
        // Only needed for tests that require "pushChanges" type operations.
        repo.setWorkspace( "Dave's Repository Workspace" );
        repo.setFlowTarget( "Dave's Stream" );
    }

    public void testCreateTagCreateSnapshotCommand()
        throws Exception
    {
        ScmTagParameters scmTagParameters = new ScmTagParameters( "My Tag Message" );
        Commandline cmd = new JazzTagCommand().createTagCreateSnapshotCommand( repo, getScmFileSet(), "My_Tag_Name",
                                                                               scmTagParameters ).getCommandline();
        String expected =
            "scm create snapshot --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword --name My_Tag_Name --description \"My Tag Message\" \"Dave's Repository Workspace\"";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testCreateTagCreateWorkspaceCommand()
        throws Exception
    {
        Commandline cmd = new JazzTagCommand().createTagCreateWorkspaceCommand( repo, getScmFileSet(),
                                                                                "My_Snapshot_Name" ).getCommandline();
        String expected =
            "scm create workspace --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword My_Snapshot_Name --snapshot My_Snapshot_Name";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testCreateTagDeliverCommand()
        throws Exception
    {
        Commandline cmd =
            new JazzTagCommand().createTagDeliverCommand( repo, getScmFileSet(), "My_Tag_Name" ).getCommandline();
        String expected =
            "scm deliver --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword --source My_Tag_Name --target \"Dave's Stream\"";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testCreateTagSnapshotPromoteCommand()
        throws Exception
    {
        Commandline cmd = new JazzTagCommand().createTagSnapshotPromoteCommand( repo, getScmFileSet(),
                                                                                "My_Snapshot_Name" ).getCommandline();
        String expected =
            "scm snapshot promote --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword \"Dave's Stream\" My_Snapshot_Name";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }
}
