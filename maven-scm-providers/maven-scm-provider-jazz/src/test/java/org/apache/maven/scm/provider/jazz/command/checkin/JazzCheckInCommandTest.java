package org.apache.maven.scm.provider.jazz.command.checkin;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.log.DefaultLog;
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
public class JazzCheckInCommandTest
    extends JazzScmTestCase
{
    private JazzScmProviderRepository repo;

    private JazzCheckInConsumer checkinConsumer;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        repo = getScmProviderRepository();
        
        checkinConsumer = new JazzCheckInConsumer( repo, new DefaultLog() );
    }

    public void testCreateCreateChangesetCommand()
        throws Exception
    {
        JazzScmProviderRepository repo = getScmProviderRepository();
        Commandline cmd = new JazzCheckInCommand().createCreateChangesetCommand( repo, getScmFileSet(), "This is my comment." ).getCommandline();
        String expected = "scm create changeset --username myUserName --password myPassword \"This is my comment.\"";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }
    
    public void testCreateCheckInCommandCheckingInSpecificFiles()
        throws Exception
    {
        JazzScmProviderRepository repo = getScmProviderRepository();
        Commandline cmd = new JazzCheckInCommand().createCheckInCommand( repo, getScmFileSet() ).getCommandline();
        String expected = "scm checkin --username myUserName --password myPassword " + getFiles();
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testCreateCheckInCommandCheckingInLocalChanges()
        throws Exception
    {
        JazzScmProviderRepository repo = getScmProviderRepository();
        Commandline cmd = new JazzCheckInCommand().createCheckInCommand( repo, new ScmFileSet( getWorkingDirectory() ) ).getCommandline();
        String expected = "scm checkin --username myUserName --password myPassword .";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }
    
    public void testCheckInConsumerWithFiles()
        throws Exception
    {
        checkinConsumer.consumeLine( "Committing..." );
        checkinConsumer.consumeLine( "Workspace: (1903) \"MavenSCMTestWorkspace_1332908068770\" <-> (1903) \"MavenSCMTestWorkspace_1332908068770\"" );
        checkinConsumer.consumeLine( "  Component: (1768) \"MavenSCMTestComponent\"" );
        checkinConsumer.consumeLine( "    Outgoing:" );
        checkinConsumer.consumeLine( "      Change sets:" );
        checkinConsumer.consumeLine( "        (1907)  *--@  \"Commit message\"" );
        checkinConsumer.consumeLine( "          Changes:" );
        checkinConsumer.consumeLine( "            --a-- \\src\\main\\java\\Me.java" );
        checkinConsumer.consumeLine( "            --a-- \\src\\main\\java\\Me1.java" );
        checkinConsumer.consumeLine( "            --a-- \\src\\main\\java\\Me2.java" );
        
        assertEquals( "Wrong number of files parsed!", 3, checkinConsumer.getFiles().size() );
        assertEquals( "Parsing error for file1!", "src\\main\\java\\Me.java", checkinConsumer.getFiles().get( 0 ).getPath() );
        assertEquals( "Parsing error for file2!", "src\\main\\java\\Me1.java", checkinConsumer.getFiles().get( 1 ).getPath() );
        assertEquals( "Parsing error for file3!", "src\\main\\java\\Me2.java", checkinConsumer.getFiles().get( 2 ).getPath() );
    }

    public void testCheckInConsumerWithOutFiles()
        throws Exception
    {
        checkinConsumer.consumeLine( "Committing..." );
        checkinConsumer.consumeLine( "Workspace: (1004) \"Release Repository Workspace\" <-> (1005) \"Maven Release Plugin Stream\"" );
        checkinConsumer.consumeLine( "  Component: (1006) \"Release Component\"" );
        checkinConsumer.consumeLine( "    Outgoing:" );
        checkinConsumer.consumeLine( "      Change sets:" );
        checkinConsumer.consumeLine( "        (1008) --@ <No comment>" );
    
        assertEquals( "Wrong number of files parsed!", 0, checkinConsumer.getFiles().size() );
    }
}
