package org.apache.maven.scm.provider.accurev.cli;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevStat;
import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class AccuRevCommandLineTest
    extends ScmTestCase
{

    public class AccuRevCommandLineTester
        extends AccuRevCommandLine
    {

        private BufferedReader stdinReader;

        private String response;

        public BufferedReader getStdinReader()
        {
            return stdinReader;
        }

        private ScmLogger initLog()
            throws Exception
        {

            return AccuRevJUnitUtil.getLogger( getContainer() );
        }

        public AccuRevCommandLineTester()
            throws Exception
        {
            setLogger( initLog() );
        }

        public AccuRevCommandLineTester( String host, int port )
            throws Exception
        {
            super( host, port );
            setLogger( initLog() );
        }

        @Override
        protected int executeCommandLine( Commandline cl, InputStream stdin, CommandOutputConsumer stdout,
                                          StreamConsumer stderr )
            throws CommandLineException
        {
            if ( stdin != null )
            {
                stdinReader = new BufferedReader( new InputStreamReader( stdin ) );
            }
            else
            {
                stdinReader = null;
            }
            try
            {
                if ( response != null )
                {
                    BufferedReader reader = new BufferedReader( new StringReader( response ) );
                    String line = reader.readLine();
                    while ( line != null )
                    {
                        stdout.consumeLine( line );
                        line = reader.readLine();
                    }
                }
            }
            catch ( IOException e )
            {
                throw new CommandLineException( "Unexpected error", e );
            }
            return 0;
        }

        public void setResponse( String response )
        {
            this.response = response;

        }

    }

    @Before
    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    @After
    @Override
    public void tearDown()
        throws Exception
    {
        super.tearDown();
    }

    @Override
    protected InputStream getCustomConfiguration()
        throws Exception
    {
        return AccuRevJUnitUtil.getPlexusConfiguration();
    }

    @Test
    public void testPromoteAll()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();

        assertThat( accuRevCL.promoteAll( new File( "/my/workspace" ), "cmt msg", new ArrayList<File>() ), is( true ) );
        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( new File( "/my/workspace" ).getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] { "promote", "-p", "-K", "-c", "cmt msg" } ) );

    }

    @Test
    public void testPromote()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        List<File> files = new ArrayList<File>();
        File testfile = new File( "my/test/file" );
        files.add( testfile );

        assertThat( accuRevCL.promote( new File( "/my/workspace" ), files, "cmt msg", new ArrayList<File>() ),
                    is( true ) );
        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( new File( "/my/workspace" ).getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] { "promote", "-K", "-c", "cmt msg", testfile.getPath() } ) );

    }

    @Test
    public void testLogin()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.setResponse( "Password: a124235bacc3ff" );
        accuRevCL.setExecutable( "accurev.exe" );
        accuRevCL.login( "aUser", "topSecret" );
        Commandline lastCL = accuRevCL.getCommandline();

        if ( Os.isFamily( Os.FAMILY_WINDOWS ) )
        {
            assertThat( lastCL.getArguments(), is( new String[] { "login", "-A", "aUser", "topSecret" } ) );
            assertThat( accuRevCL.getStdinReader(), is( nullValue() ) );
        }
        else
        {
            assertThat( lastCL.getArguments(), is( new String[] { "login", "-A", "aUser" } ) );
            assertThat( accuRevCL.getStdinReader().readLine(), is( "topSecret" ) );
        }

        accuRevCL.info( null );
        assertThat( lastCL.getArguments(), is( new String[] { "info", "-A", "a124235bacc3ff" } ) );

        assumeTrue( !Os.isFamily( Os.FAMILY_WINDOWS ) );

        accuRevCL.login( "anOther", "opensaysme" );
        assertThat( lastCL.getArguments(), is( new String[] { "login", "-A", "anOther" } ) );
        assertThat( accuRevCL.getStdinReader().readLine(), is( "opensaysme" ) );

        accuRevCL.login( "AUser", null );
        assertThat( lastCL.getArguments(), is( new String[] { "login", "-A", "AUser" } ) );
        assertThat( accuRevCL.getStdinReader().readLine(), is( "" ) );

    }

    @Test
    public void testPop()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester( "aHost", 5051 );
        accuRevCL.setExecutable( "accurev.exe" );
        File testfile = new File( "/my/export" );
        File projectDir = new File( "/./project/dir" );
        accuRevCL.pop( testfile, "stream/12", Collections.singleton( projectDir ), new ArrayList<File>() );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getExecutable(), is( "accurev.exe" ) );
        assertThat( lastCL.getArguments(), is( new String[] {
            "pop",
            "-H",
            "aHost:5051",
            "-v",
            "stream/12",
            "-L",
            testfile.getAbsolutePath(),
            "-R",
            projectDir.getPath() } ) );

    }

    @Test
    public void testPopWorkSpace()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.setExecutable( "accurev.exe" );

        File testFile = new File( "project/dir" );
        accuRevCL.pop( new File( "/home/workspace" ), Collections.singleton( testFile ), new ArrayList<File>() );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getExecutable(), is( "accurev.exe" ) );
        assertThat( lastCL.getWorkingDirectory(), is( new File( "/home/workspace" ).getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] { "pop", "-R", testFile.getPath() } ) );

    }

    @Test
    public void testMkws()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.setExecutable( "accurev2.exe" );
        File workspaceFile = new File( "/my/workspace/location" );
        accuRevCL.mkws( "myStream", "myWorkSpaceName", workspaceFile );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getExecutable(), is( "accurev2.exe" ) );
        assertThat( lastCL.getWorkingDirectory(), is( workspaceFile.getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] {
            "mkws",
            "-b",
            "myStream",
            "-w",
            "myWorkSpaceName",
            "-l",
            workspaceFile.getAbsolutePath() } ) );

    }

    @Test
    public void testUpdate()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        File workspaceFile = new File( "/my/ws/loc" );
        accuRevCL.update( workspaceFile, "highest", new ArrayList<File>() );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( workspaceFile.getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] { "update", "-t", "highest" } ) );

    }

    @Test
    public void testInfo()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.info( new File( "/my/base/dir" ) );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( new File( "/my/base/dir" ).getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] { "info" } ) );

    }

    @Test
    public void testRemoveWorkspace()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.rmws( "myWorkspaceName" );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getArguments(), is( new String[] { "rmws", "-s", "myWorkspaceName" } ) );

    }

    @Test
    public void testStatIgnored()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        File testFile = new File( "/my/base/dir" );
        accuRevCL.stat( testFile );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getArguments(), is( new String[] { "stat", "-fx", testFile.getAbsolutePath() } ) );

    }

    @Test
    public void testReactivate()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.reactivate( "ArANdomWorkspaceName" );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getArguments(), is( new String[] { "reactivate", "wspace", "ArANdomWorkspaceName" } ) );

    }

    @Test
    public void testReset()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        Commandline cl = accuRevCL.getCommandline();
        String[] shellCmds = cl.getShellCommandline();
        accuRevCL.reset();
        assertThat( accuRevCL.getCommandline().getShellCommandline(), is( shellCmds ) );
        assertThat( accuRevCL.getCommandline().getShell().getExecutable(), is( "accurev" ) );
    }

    @Test
    public void testAdd()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        List<File> files = new ArrayList<File>();
        File testFile = new File( "my/test/file" );
        files.add( testFile );
        assertThat( accuRevCL.add( new File( "/workspace" ), files, "my commit message", new ArrayList<File>() ),
                    is( true ) );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( new File( "/workspace" ).getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] { "add", "-c", "my commit message", testFile.getPath() } ) );

        assertThat( accuRevCL.add( new File( "/workspace" ), files, "", new ArrayList<File>() ), is( true ) );
        assertThat( lastCL.getArguments(), is( new String[] {
            "add",
            "-c",
            AccuRev.DEFAULT_ADD_MESSAGE,
            testFile.getPath() } ) );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRemove()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        List<File> files = new ArrayList<File>();
        File testFile = new File( "my/test/file" );
        files.add( testFile );
        File workspaceFile = new File( "/workspace" );
        assertThat( accuRevCL.defunct( workspaceFile, files, "my commit message", new ArrayList<File>() ), is( true ) );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( workspaceFile.getCanonicalFile() ) );
        assertThat( lastCL.getArguments(),
                    is( new String[] { "defunct", "-c", "my commit message", testFile.getPath() } ) );

        assertThat( accuRevCL.defunct( workspaceFile, files, "", new ArrayList<File>() ), is( true ) );
        assertThat( lastCL.getArguments(), is( new String[] {
            "defunct",
            "-c",
            AccuRev.DEFAULT_REMOVE_MESSAGE,
            testFile.getPath() } ) );

        assertThat( accuRevCL.defunct( workspaceFile, Collections.EMPTY_LIST, "", new ArrayList<File>() ), is( true ) );
        assertThat( lastCL.getArguments(), is( new String[] { "defunct", "-c", AccuRev.DEFAULT_REMOVE_MESSAGE, "." } ) );

        assertThat( accuRevCL.defunct( workspaceFile, null, "", new ArrayList<File>() ), is( true ) );
        assertThat( lastCL.getArguments(), is( new String[] { "defunct", "-c", AccuRev.DEFAULT_REMOVE_MESSAGE, "." } ) );

    }

    @Test
    public void testChangeWorkspace()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.chws( new File( "/my/workspace" ), "the_workspace_name_me", "a-snapshot" );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( new File( "/my/workspace" ).getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] {
            "chws",
            "-s",
            "the_workspace_name_me",
            "-b",
            "a-snapshot",
            "-l",
            "." } ) );

    }

    @Test
    public void testMkSnap()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.mksnap( "a-snapshot", "basisStream" );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getArguments(), is( new String[] {
            "mksnap",
            "-s",
            "a-snapshot",
            "-b",
            "basisStream",
            "-t",
            "now" } ) );

    }

    @Test
    public void testStatTag()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.statTag( "a-snapshot", new ArrayList<File>() );

        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getArguments(), is( new String[] { "stat", "-a", "-ffl", "-s", "a-snapshot" } ) );

    }

    @Test
    public void testStatBackingStream()
        throws Exception
    {
        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();

        File basedir = new File( "/my/workspace" );
        List<File> elements = new ArrayList<File>( 1 );
        File addedOrModifiedFile = new File( "addedOrModified/file" );
        elements.add( addedOrModifiedFile );
        List<File> memberElements = new ArrayList<File>();
        List<File> nonMemberElements = new ArrayList<File>();

        accuRevCL.statBackingStream( basedir, elements, memberElements, nonMemberElements );
        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( basedir.getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] { "stat", "-b", "-ffr", addedOrModifiedFile.getPath() } ) );

    }

    @Test
    public void testStatRecursive()
        throws Exception
    {

        File basedir = new File( "/my/workspace" );
        List<File> noFiles = new ArrayList<File>();

        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.stat( basedir, noFiles, AccuRevStat.KEPT, new ArrayList<File>() );
        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( basedir.getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] { "stat", "-ffr", "-k", "-R", "." } ) );

        noFiles.add( new File( "." ) );
        accuRevCL.stat( basedir, noFiles, AccuRevStat.DEFUNCT, new ArrayList<File>() );
        lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( basedir.getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] { "stat", "-ffr", "-D", "-R", "." } ) );
    }

    @Test
    public void testStatSpecificFilesAndDirectories()
        throws Exception
    {

        File basedir = new File( "/my/workspace" );
        List<File> files = new ArrayList<File>();
        File testDir = new File( "a/dir" );
        files.add( testDir );
        File testFile = new File( "a/dir/a.file" );
        files.add( testFile );

        AccuRevCommandLineTester accuRevCL = new AccuRevCommandLineTester();
        accuRevCL.stat( basedir, files, AccuRevStat.MISSING, new ArrayList<File>() );
        Commandline lastCL = accuRevCL.getCommandline();
        assertThat( lastCL.getWorkingDirectory(), is( basedir.getCanonicalFile() ) );
        assertThat( lastCL.getArguments(), is( new String[] {
            "stat",
            "-ffr",
            "-M",
            testDir.getPath(),
            testFile.getPath() } ) );

    }

}
