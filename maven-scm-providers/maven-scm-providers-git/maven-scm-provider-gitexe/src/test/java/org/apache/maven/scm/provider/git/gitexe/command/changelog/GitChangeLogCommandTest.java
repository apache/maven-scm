package org.apache.maven.scm.provider.git.gitexe.command.changelog;

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
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class GitChangeLogCommandTest
    extends ScmTestCase
{
    public void testCommandLineNoDates()
        throws Exception
    {
        testCommandLine( "scm:git:http://foo.com/git", null, null, null, "git log" );
    }

    public void testCommandLineWithDates()
        throws Exception
    {
        Date startDate = getDate( 2003, Calendar.SEPTEMBER, 10, GMT_TIME_ZONE );
        Date endDate = getDate( 2007, Calendar.OCTOBER, 10, GMT_TIME_ZONE );

        testCommandLine( "scm:git:http://foo.com/git", null, startDate, endDate,
                         "git log \"--since=2003-09-10 00:00:00 +0000\" \"--until=2007-10-10 00:00:00 +0000\"" );
    }

    public void testCommandLineStartDateOnly()
        throws Exception
    {
        Date startDate = getDate( 2003, Calendar.SEPTEMBER, 10, 1, 1, 1, GMT_TIME_ZONE );

        testCommandLine( "scm:git:http://foo.com/git", null, startDate, null,
                         "git log \"--since=2003-09-10 01:01:01 +0000\"" );
    }

    public void testCommandLineDateFormat()
        throws Exception
    {
        Date startDate = getDate( 2003, Calendar.SEPTEMBER, 10, 1, 1, 1, GMT_TIME_ZONE );
        Date endDate = getDate( 2005, Calendar.NOVEMBER, 13, 23, 23, 23, GMT_TIME_ZONE );

        testCommandLine( "scm:git:http://foo.com/git", null, startDate, endDate,
                         "git log \"--since=2003-09-10 01:01:01 +0000\" \"--until=2005-11-13 23:23:23 +0000\"" );
    }

    public void testCommandLineEndDateOnly()
        throws Exception
    {
        Date endDate = getDate( 2003, Calendar.NOVEMBER, 10, GMT_TIME_ZONE );

        // Only specifying end date should print no dates at all
        testCommandLine( "scm:git:http://foo.com/git", null, null, endDate,
                         "git log \"--until=2003-11-10 00:00:00 +0000\"" );
    }

    public void testCommandLineWithBranchNoDates()
        throws Exception
    {
        testCommandLine( "scm:git:http://foo.com/git", new ScmBranch( "my-test-branch" ), null, null, "git log" );
    }


    public void testCommandLineWithStartVersion()
        throws Exception
    {
        testCommandLine( "scm:git:http://foo.com/git", new ScmRevision( "1" ), null, "git log --since=1" );
    }

    public void testCommandLineWithStartVersionAndEndVersion()
        throws Exception
    {
        testCommandLine( "scm:git:http://foo.com/git", new ScmRevision( "1" ), new ScmRevision( "10" ),
                         "git log --since=1 --until=10" );
    }

    public void testCommandLineWithStartVersionAndEndVersionEquals()
        throws Exception
    {
        testCommandLine( "scm:git:http://foo.com/git", new ScmRevision( "1" ), new ScmRevision( "1" ),
                         "git log --since=1 --until=1" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, ScmBranch branch, Date startDate, Date endDate, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/git-update-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        GitScmProviderRepository gitRepository = (GitScmProviderRepository) repository.getProviderRepository();

        Commandline cl = GitChangeLogCommand.createCommandLine( gitRepository, workingDirectory, branch, startDate,
                                                                endDate, null, null );

        assertCommandLine( commandLine, workingDirectory, cl );
    }

    private void testCommandLine( String scmUrl, ScmVersion startVersion, ScmVersion endVersion, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/git-update-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        GitScmProviderRepository gitRepository = (GitScmProviderRepository) repository.getProviderRepository();

        Commandline cl = GitChangeLogCommand.createCommandLine( gitRepository, workingDirectory, null, null, null,
                                                                startVersion, endVersion );

        assertCommandLine( commandLine, workingDirectory, cl );
    }
}
