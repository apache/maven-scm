package org.apache.maven.scm.provider.perforce.command.checkout;

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
public class PerforceCheckOutCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
        testCommandLine( "scm:perforce://depot/projects/pathname", "p4 -ctest-test-maven sync -f @somelabel" );
    }

    public void testGetCommandLineWithHost()
        throws Exception
    {
        testCommandLine( "scm:perforce:a:username@//depot/projects/pathname",
                         "p4 -H a -u username -ctest-test-maven sync -f @somelabel" );
    }

    public void testGetCommandLineWithHostAndPort()
        throws Exception
    {
        testCommandLine( "scm:perforce:myhost:1234:username@//depot/projects/pathname",
                         "p4 -H myhost:1234 -u username -ctest-test-maven sync -f @somelabel" );
    }

    public void testClean()
    {
        String generated = PerforceScmProvider.clean( "p4 -u mr -P mypass -cclient sync ..." );
        assertEquals( "p4 -u mr -P ****** -cclient sync ...", generated );
        assertEquals( "p4 sync ...", PerforceScmProvider.clean( "p4 sync ..." ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/perforce-checkout-command-test" );
        workingDirectory.mkdirs();

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );
        PerforceScmProviderRepository svnRepository = (PerforceScmProviderRepository) repository
            .getProviderRepository();
        Commandline cl = PerforceCheckOutCommand.createCommandLine( svnRepository, workingDirectory, "somelabel",
                                                                    "test-test-maven" );

        assertEquals( commandLine, cl.toString() );
    }
}
