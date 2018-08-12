package org.apache.maven.scm.provider.git.gitexe.command;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Test;

/**
 * @author mfriedenhagen
 */
public class GitCommandLineUtilsAddTargetTest
{

    /**
     * Test of addTarget method, of class GitCommandLineUtils on Non-Windows
     * systems.
     */
    @Test
    public void testAddTargetNonWindows()
    {
        assumeTrue( !runsOnWindows() );
        final File workingDir = new File( "/prj" );
        final List<File> filesToAdd = Arrays.asList( new File( "/prj/pom.xml" ), new File( "/prj/mod1/pom.xml" ) );
        final String expectedArguments = "[add, pom.xml, mod1/pom.xml]";
        check( workingDir, filesToAdd, expectedArguments );
    }

    /**
     * Test of addTarget method, of class GitCommandLineUtils on Windows.
     */
    @Test
    public void testAddTargetWindows()
    {
        assumeTrue( runsOnWindows() );
        final File workingDir = new File( "C:\\prj" );
        // Note that the second file has a lowercase drive letter, see
        // https://jira.codehaus.org/browse/SCM-667
        final List<File> filesToAdd = Arrays.asList( new File( "C:\\prj\\pom.xml" ),
            new File( "c:\\prj\\mod1\\pom.xml" ) );
        final String expectedArguments = "[add, pom.xml, mod1/pom.xml]";
        check( workingDir, filesToAdd, expectedArguments );
    }

    private void check( final File workingDir, final List<File> filesToAdd, final String expectedArguments )
    {
        final Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( workingDir, "add" );
        GitCommandLineUtils.addTarget( cl, filesToAdd );
        final String arguments = Arrays.toString( cl.getArguments() );
        assertEquals( 3, cl.getArguments().length );
        assertEquals( expectedArguments, arguments );
    }

    @Test
    public void testPasswordAnonymous()
        throws Exception
    {

        String commandLine = "git push https://user:password@foo.com/git/trunk refs/tags/my-tag-1";

        final Commandline cl = GitCommandLineUtils.getBaseGitCommandLine( new File( "." ), commandLine );

        String[] commandLineArgs = cl.getShellCommandline();

        //
        for ( int i = 0; i < commandLineArgs.length; i++ )
        {
            assertFalse( MessageFormat.format( "The target log message should not contain <{0}> but it contains <{1}>",
                AnonymousCommandLine.PASSWORD_PLACE_HOLDER, commandLineArgs[i] ),
                commandLineArgs[i].contains( AnonymousCommandLine.PASSWORD_PLACE_HOLDER ) );
        }

        final String scmUrlFakeForTest = "https://user:".concat( AnonymousCommandLine.PASSWORD_PLACE_HOLDER ).concat(
            "@foo.com/git/trunk" );

        assertTrue( MessageFormat.format( "The target log message should contain <{0}> but it contains <{1}>",
            scmUrlFakeForTest, cl.toString() ), cl.toString().contains( scmUrlFakeForTest ) );
    }

    private boolean runsOnWindows()
    {
        return Os.isFamily( Os.FAMILY_WINDOWS );
    }
}
