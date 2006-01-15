package org.apache.maven.scm.provider.perforce.command.login;

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

import org.apache.maven.scm.CommandParameters;
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
public class PerforceLoginCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
        testCommandLine( "p4 login" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/perforce-login-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( "scm:perforce://depot/projects/pathname" );
        PerforceScmProviderRepository svnRepository = (PerforceScmProviderRepository) repository
            .getProviderRepository();
        CommandParameters params = new CommandParameters();
        Commandline cl = PerforceLoginCommand.createCommandLine( svnRepository, workingDirectory, params );

        assertEquals( commandLine, cl.toString() );
    }
}
