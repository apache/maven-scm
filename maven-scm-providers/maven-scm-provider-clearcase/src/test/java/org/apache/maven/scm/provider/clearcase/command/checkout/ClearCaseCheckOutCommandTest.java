package org.apache.maven.scm.provider.clearcase.command.checkout;

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

import org.apache.maven.scm.ScmTestCase;

import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ClearCaseCheckOutCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
        String branch = null;

        testCommandLine( branch,
                         "cleartool co" );
    }

    public void testGetCommandLineWithBranch()
        throws Exception
    {
        String branch = "myBranch";

        testCommandLine( branch,
                         "cleartool co -branch myBranch" );
    }


    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String branch, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/clearcase-checkout-command-test" );

        Commandline cl = ClearCaseCheckOutCommand.createCommandLine( workingDirectory, branch );

        assertEquals( commandLine, cl.toString() );
    }
}
