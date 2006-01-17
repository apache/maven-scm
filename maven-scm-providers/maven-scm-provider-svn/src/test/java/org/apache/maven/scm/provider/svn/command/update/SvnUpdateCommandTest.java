package org.apache.maven.scm.provider.svn.command.update;

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

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnUpdateCommandTest
    extends ScmTestCase
{
    public void testCommandLineWithEmptyTag()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", "", "svn --non-interactive update" );
    }

    public void testCommandLineWithWhitespaceTag()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", "  ", "svn --non-interactive update" );
    }

    public void testCommandLineWithoutTag()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", null, "svn --non-interactive update" );
    }

    public void testCommandLineTag()
        throws Exception
    {
        testCommandLine( "scm:svn:http://anonymous@foo.com/svn/trunk", "10",
                         "svn --username anonymous --non-interactive update -r 10" );
    }

    public void testCommandLineWithUsernameAndTag()
        throws Exception
    {
        testCommandLine( "scm:svn:http://anonymous@foo.com/svn/trunk", "10",
                         "svn --username anonymous --non-interactive update -r 10" );
    }

    public void testCommandLineWithRelativeURLTag()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", "branches/my-test-branch",
                         "svn --non-interactive switch http://foo.com/svn/branches/my-test-branch " +
                             getUpdateTestFile().getAbsolutePath() );
    }

    public void testCommandLineWithAbsoluteURLTag()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", "http://foo.com/svn/branches/my-test-branch",
                         "svn --non-interactive switch http://foo.com/svn/branches/my-test-branch " +
                             getUpdateTestFile().getAbsolutePath() );
    }

    public void testCommandLineWithNonDeterminantBase()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/some-project", "branches/my-test-branch",
                         "svn --non-interactive switch http://foo.com/svn/some-project/branches/my-test-branch " +
                             getUpdateTestFile().getAbsolutePath() );
    }

    public void testCommandLineWithNonDeterminantBaseTrailingSlash()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/some-project/", "branches/my-test-branch",
                         "svn --non-interactive switch http://foo.com/svn/some-project/branches/my-test-branch " +
                             getUpdateTestFile().getAbsolutePath() );
    }

    public void testCommandLineWithBranchSameAsBase()
        throws Exception
    {
        testCommandLine( "scm:svn:http://foo.com/svn/tags/my-tag", "tags/my-tag",
                         "svn --non-interactive switch http://foo.com/svn/tags/my-tag " +
                             getUpdateTestFile().getAbsolutePath() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private File getUpdateTestFile()
    {
        return getTestFile( "target/svn-update-command-test" );
    }

    private SvnScmProviderRepository getSvnRepository( String scmUrl )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        return (SvnScmProviderRepository) repository.getProviderRepository();
    }

    private void testCommandLine( String scmUrl, String tag, String commandLine )
        throws Exception
    {
        File workingDirectory = getUpdateTestFile();

        Commandline cl = SvnUpdateCommand.createCommandLine( getSvnRepository( scmUrl ), workingDirectory, tag );

        assertEquals( commandLine, cl.toString() );
    }
}
