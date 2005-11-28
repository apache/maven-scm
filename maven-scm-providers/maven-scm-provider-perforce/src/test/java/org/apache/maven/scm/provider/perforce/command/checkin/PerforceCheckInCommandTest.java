package org.apache.maven.scm.provider.perforce.command.checkin;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PerforceCheckInCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
        testCommandLine( "scm:perforce://depot/projects/pathname", "p4 submit -i" );
    }

    public void testGetCommandLineWithHost()
        throws Exception
    {
        testCommandLine( "scm:perforce:a:username@//depot/projects/pathname",
                         "p4 -H a -u username submit -i" );
    }

    public void testGetCommandLineWithHostAndPort()
        throws Exception
    {
        testCommandLine( "scm:perforce:myhost:1234:username@//depot/projects/pathname",
                         "p4 -H myhost:1234 -u username submit -i" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/perforce-checkin-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );
        PerforceScmProviderRepository svnRepository = (PerforceScmProviderRepository) repository.getProviderRepository();
        ScmFileSet files = new ScmFileSet( new File("."), new File[] { new File("foo.xml"), new File("bar.xml") } );
        Commandline cl = PerforceCheckInCommand.createCommandLine( svnRepository, workingDirectory, files );

        assertEquals( commandLine, cl.toString() );
        
        String change = PerforceCheckInCommand.createChangeListSpecification( svnRepository, files, "Test checkin" );
        assertContains( change, "//depot/projects/pathname/foo.xml" );
        assertContains( change, "//depot/projects/pathname/bar.xml" );
        assertContains( change, "Test checkin" );
    }
    
    private void assertContains(String block, String element) {
        if (block.indexOf(element) == -1) {
            fail("Block '" + block + "' does not contain element '" + element + "'");
        }
    }
}
