package org.apache.maven.scm.provider.svn.command.changelog;

/*
 * Copyright 2003-2005 The Apache Software Foundation.
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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnChangeLogCommandTest
    extends ScmTestCase
{
    public void testCommandLineNoDates()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", null, null, null,
                         "svn --non-interactive log -v http://foo.com/svn/trunk" );
    }

    public void testCommandLineWithDates()
        throws Exception
    {
        Date startDate = getDate( 2003, Calendar.SEPTEMBER, 10, GMT_TIME_ZONE );
        Date endDate = getDate( 2003, Calendar.OCTOBER, 10, GMT_TIME_ZONE );

        testCommandLine( "scm:svn:http://foo.com/svn/trunk", null, startDate, endDate,
                         "svn --non-interactive log -v -r \"{2003-09-10 00:00:00 +0000}:{2003-10-10 00:00:00 +0000}\" http://foo.com/svn/trunk" );
    }

    public void testCommandLineStartDateOnly()
        throws Exception
    {
        Date startDate = getDate( 2003, Calendar.SEPTEMBER, 10, 1, 1, 1, GMT_TIME_ZONE );

        testCommandLine( "scm:svn:http://foo.com/svn/trunk", null, startDate, null,
                         "svn --non-interactive log -v -r \"{2003-09-10 01:01:01 +0000}:HEAD\" http://foo.com/svn/trunk" );
    }

    public void testCommandLineDateFormat()
        throws Exception
    {
        Date startDate = getDate( 2003, Calendar.SEPTEMBER, 10, 1, 1, 1, GMT_TIME_ZONE );
        Date endDate = getDate( 2005, Calendar.NOVEMBER, 13, 23, 23, 23, GMT_TIME_ZONE );

        testCommandLine( "scm:svn:http://foo.com/svn/trunk", null, startDate, endDate,
                         "svn --non-interactive log -v -r \"{2003-09-10 01:01:01 +0000}:{2005-11-13 23:23:23 +0000}\" http://foo.com/svn/trunk" );
    }

    public void testCommandLineEndDateOnly()
        throws Exception
    {
        Date endDate = getDate( 2003, Calendar.NOVEMBER, 10, GMT_TIME_ZONE );

        // Only specifying end date should print no dates at all
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", null, null, endDate,
                         "svn --non-interactive log -v http://foo.com/svn/trunk" );
    }

    public void testCommandLineWithBranchNoDates()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", "my-test-branch", null, null,
                         "svn --non-interactive log -v http://foo.com/svn/branches/my-test-branch http://foo.com/svn/trunk" );
    }

    public void testCommandLineWithBranchStartDateOnly()
        throws Exception
    {
        Date startDate = getDate( 2003, Calendar.SEPTEMBER, 10, 1, 1, 1, GMT_TIME_ZONE );

        testCommandLine( "scm:svn:http://foo.com/svn/trunk",
                         "my-test-branch",
                         startDate,
                         null,
                         "svn --non-interactive log -v -r \"{2003-09-10 01:01:01 +0000}:HEAD\" http://foo.com/svn/branches/my-test-branch http://foo.com/svn/trunk" );
    }

    public void testCommandLineWithBranchEndDateOnly()
        throws Exception
    {
        Date endDate = getDate( 2003, Calendar.OCTOBER, 10, 1, 1, 1, GMT_TIME_ZONE );

        // Only specifying end date should print no dates at all
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", "my-test-branch", null, endDate,
                         "svn --non-interactive log -v http://foo.com/svn/branches/my-test-branch http://foo.com/svn/trunk" );
    }

    public void testCommandLineWithBranchBothDates()
        throws Exception
    {
        Date startDate = getDate( 2003, Calendar.SEPTEMBER, 10, GMT_TIME_ZONE );
        Date endDate = getDate( 2003, Calendar.OCTOBER, 10, GMT_TIME_ZONE );

        testCommandLine( "scm:svn:http://foo.com/svn/trunk",
                         "my-test-branch",
                         startDate,
                         endDate,
                         "svn --non-interactive log -v -r \"{2003-09-10 00:00:00 +0000}:{2003-10-10 00:00:00 +0000}\" http://foo.com/svn/branches/my-test-branch http://foo.com/svn/trunk" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, String branch, Date startDate, Date endDate, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/svn-update-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl = SvnChangeLogCommand.createCommandLine( svnRepository, workingDirectory, branch, startDate,
                                                                endDate );

        assertEquals( commandLine, cl.toString() );
    }
}
