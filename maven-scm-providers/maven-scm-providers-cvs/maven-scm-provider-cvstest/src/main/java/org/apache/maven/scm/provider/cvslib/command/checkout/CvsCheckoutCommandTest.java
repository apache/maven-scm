package org.apache.maven.scm.provider.cvslib.command.checkout;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;
import org.apache.maven.scm.provider.cvslib.CvsScmTestUtils;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class CvsCheckoutCommandTest
    extends AbstractCvsScmTest
{
    /** {@inheritDoc} */
    protected String getModule()
    {
        return "test-repo/checkout";
    }

    /**
     * @todo move this test to the TCK.
     */
    public void testCheckOutWithoutTag()
        throws Exception
    {
        if ( !isSystemCmd( CvsScmTestUtils.CVS_COMMAND_LINE ) )
        {
            System.err.println( "'" + CvsScmTestUtils.CVS_COMMAND_LINE + "' is not a system command. Ignored "
                + getName() + "." );
            return;
        }

        ScmManager scmManager = getScmManager();

        CheckOutScmResult result = scmManager.checkOut( getScmRepository(), getScmFileSet() );

        if ( !result.isSuccess() )
        {
            fail( result.getProviderMessage() + "\n" + result.getCommandOutput() + "\n" + result.getCommandLine() );
        }

        List<ScmFile> files = result.getCheckedOutFiles();

        assertNotNull( files );

        assertEquals( 3, files.size() );

        assertCheckedOutFile( files, 0, "/Foo.java", ScmFileStatus.UPDATED );

        assertCheckedOutFile( files, 1, "/Readme.txt", ScmFileStatus.UPDATED );

        assertCheckedOutFile( files, 2, "/src/java/org/apache/maven/MavenUtils.java", ScmFileStatus.UPDATED );
    }

    /**
     * @todo move this test to the TCK - checkout with "revision", then have one for tag as well.
     */
    public void testCheckOutWithTag()
        throws Exception
    {
        if ( !isSystemCmd( CvsScmTestUtils.CVS_COMMAND_LINE ) )
        {
            System.err.println( "'" + CvsScmTestUtils.CVS_COMMAND_LINE + "' is not a system command. Ignored "
                + getName() + "." );
            return;
        }

        ScmManager scmManager = getScmManager();

        @SuppressWarnings( "deprecation" )
        CheckOutScmResult result = scmManager.getProviderByRepository( getScmRepository() ).checkOut(
            getScmRepository(), getScmFileSet(), "MAVEN_1_0" );

        if ( !result.isSuccess() )
        {
            fail( result.getProviderMessage() + "\n" + result.getCommandOutput() );
        }

        List<ScmFile> files = result.getCheckedOutFiles();

        assertNotNull( files );

        assertEquals( 1, files.size() );

        File mavenUtils =
            assertCheckedOutFile( files, 0, "/src/java/org/apache/maven/MavenUtils.java", ScmFileStatus.UPDATED );

        assertBetween( 38403, 39511, mavenUtils.length() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private File assertCheckedOutFile( List<ScmFile> files, int i, String fileName, ScmFileStatus status )
        throws Exception
    {
        File file = new File( getWorkingDirectory(), fileName );

        assertTrue( file.getAbsolutePath() + " file doesn't exist.", file.exists() );

        ScmFile coFile = files.get( i );

        assertSame( status, coFile.getStatus() );

        assertPath( fileName, coFile.getPath() );

        return file;
    }
}
