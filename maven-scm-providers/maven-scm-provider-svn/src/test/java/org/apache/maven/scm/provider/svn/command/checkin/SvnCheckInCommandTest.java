package org.apache.maven.scm.provider.svn.command.checkin;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SvnCheckInCommandTest
    extends ScmTestCase
{
    private File messageFile;

    private String messageFileString;

    public void setUp()
        throws Exception
    {
        super.setUp();

        messageFile = new File( "commit-message" );

        messageFileString = "--file \"" + messageFile.getAbsolutePath() + "\"";
    }

    public void testCommandLineWithEmptyTag()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk",
                         "svn commit --non-interactive " + messageFileString );
    }

    public void testCommandLineWithoutTag()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk",
                         "svn commit --non-interactive " + messageFileString );
    }

    public void testCommandLineTag()
        throws Exception
    {
        testCommandLine( "scm:svn:anonymous@http://foo.com/svn/trunk",
                         "svn commit --non-interactive --username anonymous " + messageFileString );
    }

    public void testCommandLineWithUsernameAndTag()
        throws Exception
    {
        testCommandLine( "scm:svn:anonymous@http://foo.com/svn/trunk",
                         "svn commit --non-interactive --username anonymous " + messageFileString );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/svn-checkin-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl = SvnCheckInCommand.createCommandLine( svnRepository, workingDirectory, messageFile );

        assertEquals( commandLine, cl.toString() );
    }
}
