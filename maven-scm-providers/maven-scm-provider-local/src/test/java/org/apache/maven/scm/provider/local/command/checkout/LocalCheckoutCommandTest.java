package org.apache.maven.scm.provider.local.command.checkout;

/* ====================================================================
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
 * ====================================================================
 */

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.local.AbstractLocalScmTest;
import org.apache.maven.scm.repository.ScmRepository;

import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LocalCheckoutCommandTest
    extends AbstractLocalScmTest
{
    protected String getModule()
    {
        return "test-repo/checkout";
    }

    public void testCheckOutWithTag()
    	throws Exception
    {
        ScmManager scmManager = getScmManager();

        try
        {
            scmManager.checkOut( makeScmRepository( "scm:local:src/test/repositories:test-repo" ), getWorkingDirectory(), "my-tag" );

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
        coTest( "scm:local:src/test/repositories:test-repo" );
    }

    public void testTestScmWithAbsolutePath()
        throws Exception
    {
        coTest( "scm:local:" + getTestPath( "src/test/repositories" ) + ":test-repo" );
    }

    private void coTest( String scmUrl )
        throws Exception
    {
        ScmManager scmManager = (ScmManager) lookup( ScmManager.ROLE );

        ScmRepository repository = scmManager.makeScmRepository( scmUrl );

        File workingDirectory = getTestFile( "target/local-scm-test" );

        FileUtils.deleteDirectory( workingDirectory );

        assertTrue( workingDirectory.mkdir() );

        CheckOutScmResult result = scmManager.checkOut( repository, workingDirectory, null );

        // Assert the result
        assertNotNull( result );

        assertTrue( result.isSuccess() );

        assertNotNull( result.getCheckedOutFiles() );

        assertEquals( 2, result.getCheckedOutFiles().size() );

        ScmFile file1 = (ScmFile) result.getCheckedOutFiles().get( 0 );

        assertEquals( "/test-repo/readme.txt", file1.getPath( ) );

        assertEquals( ScmFileStatus.CHECKED_OUT, file1.getStatus() );

        ScmFile file2 = (ScmFile) result.getCheckedOutFiles().get( 1 );

        assertEquals( "/test-repo/src/main/java/Test.java", file2.getPath( ) );

        assertEquals( ScmFileStatus.CHECKED_OUT, file2.getStatus() );

        // Assert that the files actually is there
        File coDir = new File( workingDirectory, "test-repo" );

        assertTrue( coDir.isDirectory() );

        assertTrue( new File( coDir, "src/main/java/Test.java" ).isFile() );

        assertTrue( new File( coDir, "readme.txt" ).isFile() );
    }
}
