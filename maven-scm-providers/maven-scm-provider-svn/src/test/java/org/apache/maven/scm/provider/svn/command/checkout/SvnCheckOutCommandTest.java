package org.apache.maven.scm.provider.svn.command.checkout;

/*
 * Copyright 2003-2004 The Apache Software Foundation.
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
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnCheckOutCommandTest
    extends ScmTestCase
{
    public void testCommandLineWithoutTag()
        throws Exception
    {
        testCommandLine( getScmManager(), "scm:svn:http://foo.com/svn/trunk", null,
                         "svn checkout --non-interactive http://foo.com/svn/trunk" );
    }

    public void testCommandLineWithEmptyTag()
        throws Exception
    {
        testCommandLine( getScmManager(), "scm:svn:http://foo.com/svn/trunk", "",
                         "svn checkout --non-interactive -r  http://foo.com/svn/trunk" );
    }

    public void testCommandLineWithTag()
        throws Exception
    {
        testCommandLine( getScmManager(), "scm:svn:http://foo.com/svn/trunk", "10",
                         "svn checkout --non-interactive -r 10 http://foo.com/svn/trunk" );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private static void testCommandLine( ScmManager scmManager, String scmUrl, String tag, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/svn-update-command-test" );

        ScmRepository repository = scmManager.makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl = SvnCheckOutCommand.createCommandLine( svnRepository, workingDirectory, tag );

        assertEquals( commandLine, cl.toString() );
    }
}
