package org.apache.maven.scm.provider.git.gitexe.command.checkin;

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
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;

/**
 * @author Bertrand Paquet
 */
public class GitCheckInCommandNoBranchTest
    extends ScmTestCase
{

    private File workingDirectory;

    public void setUp()
        throws Exception
    {
        super.setUp();

        workingDirectory = new File( "target/checkin-nobranch" );
    }

    public void testCheckinNoBranch()
        throws Exception
    {
        if ( !ScmTestCase.isSystemCmd( "git" ) )
        {
            System.out.println( "skip test which git native executable in path" );
            return;
        }
        File repo_orig = new File( "src/test/resources/repository_no_branch" );
        File repo = getTestFile( "target/git_copy" );
        FileUtils.deleteDirectory( repo );
        FileUtils.copyDirectoryStructure( repo_orig, repo );

        ScmRepository scmRepository = getScmManager().makeScmRepository( "scm:git:" + repo.toPath().toUri().toASCIIString() );

        CheckOutScmResult checkOutScmResult = checkoutRepo( scmRepository );

        // Add a default user to the config
        GitScmTestUtils.setDefaultUser( workingDirectory );

        assertEquals( 0, checkOutScmResult.getCheckedOutFiles().size() );

        File f = new File( workingDirectory.getAbsolutePath() + File.separator + "pom.xml" );
        FileUtils.fileWrite( f.getAbsolutePath(), "toto" );

        ScmFileSet scmFileSet = new ScmFileSet( workingDirectory, new File( "pom.xml" ) );
        AddScmResult addResult = getScmManager().add( scmRepository, scmFileSet );
        assertResultIsSuccess( addResult );

        CheckInScmResult checkInScmResult = getScmManager().checkIn( scmRepository, scmFileSet, "commit" );
        assertResultIsSuccess( checkInScmResult );
        assertEquals( 1, checkInScmResult.getCheckedInFiles().size() );
        assertEquals( "pom.xml", checkInScmResult.getCheckedInFiles().get( 0 ).getPath() );

        checkOutScmResult = checkoutRepo( scmRepository );
        assertResultIsSuccess( checkOutScmResult );
        assertEquals( 1, checkOutScmResult.getCheckedOutFiles().size() );
        assertEquals( "pom.xml", checkOutScmResult.getCheckedOutFiles().get( 0 ).getPath() );
    }

    protected CheckOutScmResult checkoutRepo( ScmRepository scmRepository )
        throws Exception
    {
        FileUtils.deleteDirectory( workingDirectory );

        CheckOutScmResult result =
            getScmManager().checkOut( scmRepository, new ScmFileSet( workingDirectory ), (ScmVersion) null );

        assertResultIsSuccess( result );
        return result;
    }
}
