package org.apache.maven.scm.provider.perforce.command.changelog;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PerforceChangeLogCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
        testCommandLine( "scm:perforce://depot/projects/pathname", "p4 filelog -t -l ..." );
    }

    public void testGetCommandLineWithHost()
        throws Exception
    {
        testCommandLine( "scm:perforce:a:username@//depot/projects/pathname", "p4 -H a -u username filelog -t -l ..." );
    }

    public void testGetCommandLineWithHostAndPort()
        throws Exception
    {
        System.setProperty( PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY, "foo" );
        testCommandLine( "scm:perforce:myhost:1234:username@//depot/projects/pathname",
                         "p4 -H myhost:1234 -u username -c foo filelog -t -l ..." );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/perforce-changelog-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        PerforceScmProviderRepository repo = (PerforceScmProviderRepository) repository.getProviderRepository();

        Commandline cl = PerforceChangeLogCommand.createCommandLine( repo, workingDirectory,
                System.getProperty( PerforceScmProvider.DEFAULT_CLIENTSPEC_PROPERTY ) );

        assertEquals( commandLine, cl.toString() );
    }
}
