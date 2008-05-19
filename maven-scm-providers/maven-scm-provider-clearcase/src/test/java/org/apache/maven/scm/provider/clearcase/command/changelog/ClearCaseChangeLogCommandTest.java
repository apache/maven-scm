package org.apache.maven.scm.provider.clearcase.command.changelog;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.clearcase.util.ClearCaseUtil;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author <a href="mailto:m.holster@anva.nl">Mark Holster</a>
 * @version $Id$
 */
public class ClearCaseChangeLogCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
        Date startDate = null;

        Date endDate = null;

        testCommandLine( null, startDate, endDate,
                         "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%u\\n\" -recurse -nco" );
    }

    public void testGetCommandLineWithUserPattern()
        throws Exception
    {
        ClearCaseUtil.setSettingsDirectory( getTestFile( "src/test/resources/clearcase/changelog" ) );

        Date startDate = null;

        Date endDate = null;

        testCommandLine( null, startDate, endDate,
                         "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%-8.8u\\n\" -recurse -nco" );

        ClearCaseUtil.setSettingsDirectory( ClearCaseUtil.DEFAULT_SETTINGS_DIRECTORY );
    }

    public void testGetCommandLineWithTag()
        throws Exception
    {
        Date startDate = null;

        Date endDate = null;

        testCommandLine( new ScmBranch( "myBranch" ), startDate, endDate,
                         "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%u\\n\" -recurse -nco -branch myBranch" );
    }

    public void testGetCommandLineWithStartDate()
        throws Exception
    {
        Date startDate = getDate( 2003, 8, 10 );

        Date endDate = null;

        testCommandLine( null, startDate, endDate,
                         "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%u\\n\" -recurse -nco -since 10-Sep-2003" );
    }

    public void testGetCommandLineWithTagAndStartDate()
        throws Exception
    {
        Date startDate = getDate( 2003, 8, 10 );

        Date endDate = null;

        testCommandLine( new ScmBranch( "myBranch" ), startDate, endDate,
                         "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%u\\n\" -recurse -nco -since 10-Sep-2003 -branch myBranch" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( ScmBranch branch, Date startDate, Date endDate, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/clearcare-changelog-command-test" );

        Commandline cl = ClearCaseChangeLogCommand.createCommandLine( workingDirectory, branch, startDate );
        System.out.println( commandLine );
        System.out.println( cl.toString() );
        assertEquals( commandLine, cl.toString() );
    }
}
