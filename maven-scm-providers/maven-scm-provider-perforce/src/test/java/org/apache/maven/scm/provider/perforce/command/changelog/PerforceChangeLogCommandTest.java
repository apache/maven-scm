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
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

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
        testCommandLine( "scm:perforce://depot/projects/pathname", cmdPrefix + " filelog -t -l ..." );
    }

    public void testGetCommandLineWithHost()
        throws Exception
    {
        testCommandLine( "scm:perforce:a:username@//depot/projects/pathname",
                         cmdPrefix + " -p a -u username filelog -t -l ..." );
    }

    public void testGetCommandLineWithHostAndPort()
        throws Exception
    {
        System.setProperty( PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY, "foo" );
        testCommandLine( "scm:perforce:myhost:1234:username@//depot/projects/pathname",
                         cmdPrefix + " -p myhost:1234 -u username -c foo filelog -t -l ..." );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, String commandLine )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        PerforceScmProviderRepository repo = (PerforceScmProviderRepository) repository.getProviderRepository();

        Commandline cl = PerforceChangeLogCommand.createCommandLine( repo, workingDirectory, System.getProperty(
            PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY ) );

        assertCommandLine( commandLine, null, cl );
    }
}
