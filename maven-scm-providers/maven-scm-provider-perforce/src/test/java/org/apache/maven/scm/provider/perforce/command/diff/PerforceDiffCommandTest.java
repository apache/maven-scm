package org.apache.maven.scm.provider.perforce.command.diff;

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
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class PerforceDiffCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
        testCommandLine( "somelabel", null, "p4 diff2 -u ...@somelabel ...@head" );
    }

    public void testGetCommandLineWithRevs()
        throws Exception
    {
        testCommandLine( "somelabel", "someend", "p4 diff2 -u ...@somelabel ...@someend" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String startRev, String endRev, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/perforce-diff-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( "scm:perforce://depot/projects/pathname" );
        PerforceScmProviderRepository svnRepository = (PerforceScmProviderRepository) repository
            .getProviderRepository();
        Commandline cl = PerforceDiffCommand.createCommandLine( svnRepository, workingDirectory, startRev, endRev );

        assertEquals( commandLine, cl.toString() );
    }
}
