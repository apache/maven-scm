package org.apache.maven.scm.provider.git.gitexe.command.checkout;

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

import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class GitCheckOutCommandTest
    extends ScmTestCase
{
    private File workingDirectory;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void setUp()
        throws Exception
    {
        super.setUp();

        workingDirectory = getTestFile( "target/git-checkout-command-test" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void testCommandLineWithBranch()
        throws Exception
    {
        testCommandLine( getScmManager(), "scm:git:http://foo.com/git", "mybranch", "git checkout mybranch" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( ScmManager scmManager, String scmUrl, String revision, String commandLine )
        throws Exception
    {
        ScmRepository repository = scmManager.makeScmRepository( scmUrl );

        GitScmProviderRepository gitRepository = (GitScmProviderRepository) repository.getProviderRepository();

        Commandline cl =
            GitCheckOutCommand.createCommandLine( gitRepository, workingDirectory, new ScmRevision( revision ) );

        assertCommandLine( commandLine, workingDirectory, cl );
    }
}
