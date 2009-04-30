package org.apache.maven.scm.provider.perforce.command.changelog;

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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PerforceChangeLogCommandTest
    extends ScmTestCase
{
    private static final File workingDirectory = getTestFile( "target/perforce-changelog-command-test" );

    private static final String cmdPrefix = "p4 -d " + workingDirectory.getAbsolutePath();

    public void testGetCommandLine()
        throws Exception
    {
        testCommandLine( "scm:perforce://depot/projects/pathname", cmdPrefix + " changes -t ..." );
    }

    public void testGetCommandLineWithHost()
        throws Exception
    {
        testCommandLine( "scm:perforce:a:username@//depot/projects/pathname",
                         cmdPrefix + " -p a -u username changes -t ..." );
    }

    public void testGetCommandLineWithHostAndPort()
        throws Exception
    {
        System.setProperty( PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY, "foo" );
        testCommandLine( "scm:perforce:myhost:1234:username@//depot/projects/pathname",
                         cmdPrefix + " -p myhost:1234 -u username -c foo changes -t ..." );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, String commandLine )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        PerforceScmProviderRepository repo = (PerforceScmProviderRepository) repository.getProviderRepository();

        Commandline cl = PerforceChangeLogCommand.createCommandLine( repo, workingDirectory,
            System.getProperty( PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY ),
             null, null, null, null, null );

        assertCommandLine( commandLine, null, cl );
    }

    public void testGetCommandLineWithStartAndEndDates()
        throws Exception
    {
        System.setProperty( PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY, "foo" );
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        testCommandLineDates( cmdPrefix + " -c foo changes -t ...@2008/07/15:00:00:00,2008/07/16:00:00:00", sdf.parse("2008/07/15 00:00:00"), sdf.parse("2008/07/16 00:00:00") );
    }

    public void testGetCommandLineWithStartAndEndChangelists()
        throws Exception
    {
        System.setProperty( PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY, "foo" );
        testCommandLineRevs( cmdPrefix + " -c foo changes -t ...@123456,234567", new ScmRevision( "123456" ), new ScmRevision( "234567" ) );
    }

    private void testCommandLineRevs( String commandLine, ScmVersion version1, ScmVersion version2 )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( "scm:perforce://depot/projects/pathname");

        PerforceScmProviderRepository repo = (PerforceScmProviderRepository) repository.getProviderRepository();

        Commandline cl = PerforceChangeLogCommand.createCommandLine( repo, workingDirectory, System.getProperty(
            PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY ), null, null, null, version1, version2 );

        assertCommandLine( commandLine, null, cl );
    }

    private void testCommandLineDates( String commandLine, Date date1, Date date2 )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( "scm:perforce://depot/projects/pathname");

        PerforceScmProviderRepository repo = (PerforceScmProviderRepository) repository.getProviderRepository();

        Commandline cl = PerforceChangeLogCommand.createCommandLine( repo, workingDirectory, System.getProperty(
            PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY ), null, date1, date2, null, null );

        assertCommandLine( commandLine, null, cl );
    }
}
