package org.apache.maven.scm.provider.jazz.command.add;

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
public class JazzAddCommandTest
    extends JazzScmTestCase
{
    private JazzScmProviderRepository repo;

    private JazzAddConsumer addConsumer;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        repo = getScmProviderRepository();

        addConsumer = new JazzAddConsumer( repo, new DefaultLog() );
    }

    public void testCreateAddCommand()
        throws Exception
    {
        Commandline cmd = new JazzAddCommand().createAddCommand( repo, getScmFileSet() ).getCommandline();
        String expected = "scm checkin --username myUserName --password myPassword " + getFiles();
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testCheckInConsumerWithFiles()
        throws Exception
    {
        addConsumer.consumeLine( "Committing..." );
        addConsumer.consumeLine(
            "Workspace: (1903) \"MavenSCMTestWorkspace_1332908068770\" <-> (1903) \"MavenSCMTestWorkspace_1332908068770\"" );
        addConsumer.consumeLine( "  Component: (1768) \"MavenSCMTestComponent\"" );
        addConsumer.consumeLine( "    Outgoing:" );
        addConsumer.consumeLine( "      Change sets:" );
        addConsumer.consumeLine( "        (1907)  *--@  \"Commit message\"" );
        addConsumer.consumeLine( "          Changes:" );
        addConsumer.consumeLine( "            --a-- \\src\\main\\java\\Me.java" );
        addConsumer.consumeLine( "            --a-- \\src\\main\\java\\Me1.java" );
        addConsumer.consumeLine( "            --a-- \\src\\main\\java\\Me2.java" );

        assertEquals( "Wrong number of files parsed!", 3, addConsumer.getFiles().size() );
        assertEquals( "Parsing error for file1!", "src\\main\\java\\Me.java",
                      addConsumer.getFiles().get( 0 ).getPath() );
        assertEquals( "Parsing error for file2!", "src\\main\\java\\Me1.java",
                      addConsumer.getFiles().get( 1 ).getPath() );
        assertEquals( "Parsing error for file3!", "src\\main\\java\\Me2.java",
                      addConsumer.getFiles().get( 2 ).getPath() );
    }

    public void testCheckInConsumerWithOutFiles()
        throws Exception
    {
        addConsumer.consumeLine( "Committing..." );
        addConsumer.consumeLine(
            "Workspace: (1004) \"Release Repository Workspace\" <-> (1005) \"Maven Release Plugin Stream\"" );
        addConsumer.consumeLine( "  Component: (1006) \"Release Component\"" );
        addConsumer.consumeLine( "    Outgoing:" );
        addConsumer.consumeLine( "      Change sets:" );
        addConsumer.consumeLine( "        (1008) --@ <No comment>" );

        assertEquals( "Wrong number of files parsed!", 0, addConsumer.getFiles().size() );
    }
}
