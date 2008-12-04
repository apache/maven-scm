package org.apache.maven.scm.provider.perforce.command.login;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.log.ScmLogger;
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
    private static final File workingDirectory = getTestFile( "target/perforce-login-command-test" );

    private static final String cmdPrefix = "p4 -d " + workingDirectory.getAbsolutePath();

    public void testGetCommandLine()
        throws Exception
    {
        testCommandLine( cmdPrefix + " login" );
    }

    /**
     * This test requires P4 installed
     * 
     * @throws Exception
     */
    public void disabledTestLoginWithoutPassword()
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( "scm:perforce://depot/projects/pathname" );
        PerforceScmProviderRepository scmRepository =
            (PerforceScmProviderRepository) repository.getProviderRepository();
        ScmFileSet fileSet = new ScmFileSet( new File( "." ) );

        PerforceLoginCommand command = new PerforceLoginCommand();
        ScmLogger logger = new DefaultLog();
        command.setLogger( logger );

        command.executeLoginCommand( scmRepository, fileSet, null );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String commandLine )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( "scm:perforce://depot/projects/pathname" );
        PerforceScmProviderRepository scmRepository = (PerforceScmProviderRepository) repository
            .getProviderRepository();
        //CommandParameters params = new CommandParameters();
        Commandline cl = PerforceLoginCommand.createCommandLine( scmRepository, workingDirectory );

        assertCommandLine( commandLine, null, cl );
    }
}
