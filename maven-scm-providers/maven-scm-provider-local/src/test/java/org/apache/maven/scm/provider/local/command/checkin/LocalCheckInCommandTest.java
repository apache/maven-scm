package org.apache.maven.scm.provider.local.command.checkin;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.local.AbstractLocalScmTest;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LocalCheckInCommandTest
    extends AbstractLocalScmTest
{
    private final static String module = "test-repo";

    private final File repositoryRoot;

    private final File workingDirectory;

    public LocalCheckInCommandTest()
    {
        repositoryRoot = getTestFile( "target/local-scm-test/repository" );

        workingDirectory = getTestFile( "target/local-scm-test/checked-out" );
    }

    public void testCheckInWithTag()
    	throws Exception
    {
        ScmManager scmManager = getScmManager();

        try
        {
            scmManager.checkOut( makeScmRepository( "scm:local:src/test/repository:" + module ), getScmFileSet(), "my-tag" );

            fail( "Expected ScmException" );
        }
        catch( ScmException ex )
        {
            // expected
        }
    }

    public void testTestScmWithRelativePath()
        throws Exception
    {
        ciTest( "scm:local:target/local-scm-test/repository:" + module );
    }

    public void testTestScmWithAbsolutePath()
        throws Exception
    {
        ciTest( "scm:local|" + repositoryRoot.getAbsolutePath() + "|" + module );
    }

    private void ciTest( String scmUrl )
        throws Exception
    {
        // ----------------------------------------------------------------------
        // Initialize directories
        // ----------------------------------------------------------------------

        FileUtils.deleteDirectory( repositoryRoot );

        FileUtils.deleteDirectory( workingDirectory );

        assertTrue( repositoryRoot.mkdirs() );

        assertTrue( workingDirectory.mkdirs() );

        // ----------------------------------------------------------------------
        // Make the repository
        // ----------------------------------------------------------------------

        File moduleRoot = new File( repositoryRoot, module );

        makeFile( moduleRoot, "/pom.xml" );

        makeFile( moduleRoot, "/src/main/java/Application.java" );

        // ----------------------------------------------------------------------
        // Make the working directory
        // ----------------------------------------------------------------------

        makeFile( workingDirectory, "/pom.xml", "changed pom.xml" );

        makeFile( workingDirectory, "/readme.txt" );

        makeFile( workingDirectory, "/src/main/java/Application.java" );

        makeFile( workingDirectory, "/src/test/java/Test.java" );

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ScmManager scmManager = (ScmManager) lookup( ScmManager.ROLE );

        ScmRepository repository = scmManager.makeScmRepository( scmUrl );

        CheckInScmResult result = scmManager.checkIn( repository, new ScmFileSet( workingDirectory ), null,
                                                      "o Message." );

        // ----------------------------------------------------------------------
        // Assert the repository
        // ----------------------------------------------------------------------

        assertNotNull( result );

        assertTrue( "The command wasn't sucessfully executed.", result.isSuccess() );

        assertNull( "The provider message wasn't null", result.getProviderMessage() );

        assertNull( "The command output wasn't null.", result.getCommandOutput() );

        assertNotNull( "The checked in files list was null.", result.getCheckedInFiles() );

        assertTrue( "The checked in files list didn't contain the expected number of elements. Expected: 3, was: " + result.getCheckedInFiles().size(),
                    result.getCheckedInFiles().size() == 3 );
    }
}
