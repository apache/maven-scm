package org.apache.maven.scm.provider.svn.svnexe.command.checkout;

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
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnCheckOutCommandTest
    extends ScmTestCase
{
    private File workingDirectory;

    private boolean recursive;

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void setUp()
        throws Exception
    {
        super.setUp();

        recursive = true;
        workingDirectory = getTestFile( "target/svn-checkout-command-test" );
        if ( workingDirectory != null && workingDirectory.isDirectory() )
        {
            FileUtils.deleteDirectory( workingDirectory );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public void testCommandLineWithoutRevision()
        throws Exception
    {
        testCommandLine( getScmManager(), "scm:svn:http://foo.com/svn/trunk", null,
                         "svn --non-interactive checkout http://foo.com/svn/trunk "
                             + workingDirectory.getAbsolutePath() );
    }

    public void testCommandLineWithEmptyRevision()
        throws Exception
    {
        testCommandLine( getScmManager(), "scm:svn:http://foo.com/svn/trunk", "",
                         "svn --non-interactive checkout http://foo.com/svn/trunk "
                             + workingDirectory.getAbsolutePath() );
    }

    public void testCommandLineWithRevision()
        throws Exception
    {
        testCommandLine( getScmManager(), "scm:svn:http://foo.com/svn/trunk", "10",
                         "svn --non-interactive checkout -r 10 http://foo.com/svn/trunk "
                             + workingDirectory.getAbsolutePath() );
    }

    public void testRecursiveCheckOutCommandLine()
        throws Exception
    {
        recursive = false;
        testCommandLine( getScmManager(), "scm:svn:http://foo.com/svn/trunk", "10",
                         "svn --non-interactive checkout -N -r 10 http://foo.com/svn/trunk "
                             + workingDirectory.getAbsolutePath() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( ScmManager scmManager, String scmUrl, String revision, String commandLine )
        throws Exception
    {
        ScmRepository repository = scmManager.makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl =
            cl =
                SvnCheckOutCommand.createCommandLine( svnRepository, workingDirectory,
                                                      new ScmRevision( revision ), svnRepository.getUrl(),
                                                      recursive );

        assertCommandLine( commandLine, workingDirectory.getParentFile(), cl );
    }
}
