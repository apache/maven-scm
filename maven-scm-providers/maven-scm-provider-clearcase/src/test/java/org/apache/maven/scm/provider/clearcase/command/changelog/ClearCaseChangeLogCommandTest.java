package org.apache.maven.scm.provider.clearcase.command.changelog;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.Date;

import org.apache.maven.scm.ScmTestCase;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ClearCaseChangeLogCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
        String tag = null;

        Date startDate = null;

        Date endDate = null;

        testCommandLine( tag, startDate, endDate,
                         "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%-8.8u\\n\" -recurse -nco" );
    }

    public void testGetCommandLineWithTag()
        throws Exception
    {
        String tag = "myBranch";

        Date startDate = null;

        Date endDate = null;

        testCommandLine( tag, startDate, endDate,
                         "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%-8.8u\\n\" -recurse -nco -branch myBranch" );
    }

    public void testGetCommandLineWithStartDate()
        throws Exception
    {
        String tag = null;

        Date startDate = getDate( 2003, 8, 10, GMT_TIME_ZONE );

        Date endDate = null;

        testCommandLine( tag, startDate, endDate,
                         "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%-8.8u\\n\" -recurse -nco -since 10-Sep-2003" );
    }

    public void testGetCommandLineWithTagAndStartDate()
        throws Exception
    {
        String tag = "myBranch";

        Date startDate = getDate( 2003, 8, 10, GMT_TIME_ZONE );

        Date endDate = null;

        testCommandLine( tag, startDate, endDate,
                         "cleartool lshistory -fmt \"NAME:%En\\nDATE:%Nd\\nCOMM:%-12.12o - %o - %c - Activity: %[activity]p\\nUSER:%-8.8u\\n\" -recurse -nco -since 10-Sep-2003 -branch myBranch" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String tag, Date startDate, Date endDate, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/clearcare-changelog-command-test" );

        Commandline cl = ClearCaseChangeLogCommand.createCommandLine( workingDirectory, tag, startDate );

        assertEquals( commandLine, cl.toString() );
    }
}
