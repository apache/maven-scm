package org.apache.maven.scm.provider.cvslib.command.checkin;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;
import org.apache.maven.scm.provider.cvslib.CvsScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsCheckInCommandTest
	extends AbstractCvsScmTest
{
    private File repository;

    private File workingDirectory;

    private File assertionDirectory;

    protected String getModule()
    {
        return "test-repo/check-in";
    }

    public void setUp()
    	throws Exception
    {
        super.setUp();

        repository = getTestFile( "target/check-in-test/repository" );

        workingDirectory = getTestFile( "target/check-in-test/working-directory" );

        assertionDirectory = getTestFile( "target/check-in-test/assertion-directory" );

        CvsScmTestUtils.initRepo( repository, workingDirectory, assertionDirectory );
    }

    public void testCheckInWithoutTag()
		throws Exception
	{
        // Check out a version
        String arguments = "-d " + repository.getAbsolutePath() + " co " + getModule();

        CvsScmTestUtils.executeCVS( workingDirectory, arguments );

        // Make sure that the correct files was checked out
        File fooJava = new File( workingDirectory, "test-repo/check-in/Foo.java" );

        File readmeTxt = new File( workingDirectory, "test-repo/check-in/Readme.txt" );

        assertTrue( fooJava.canRead() );

        assertTrue( readmeTxt.canRead() );

        String fooJavaContent = FileUtils.fileRead( fooJava );

        assertTrue( fooJavaContent.indexOf( "Revision 1.1" ) > 0 );

        assertTrue( fooJavaContent.indexOf( "Revision 1.2" ) == -1 );

        // Change the files
        changeFooJava( fooJava );

        changeReadmeTxt( readmeTxt );

        ScmManager scmManager = getScmManager();

        ScmRepository scmRepository = makeScmRepository( "scm:cvs|local|" + repository.getAbsolutePath() + "|" + getModule() );

        // Check in the files
        CheckInScmResult result = scmManager.checkIn( scmRepository, new ScmFileSet( workingDirectory ), null,
                                                      "Commit message" );

        assertNotNull( result );

        assertTrue( result.isSuccess() );

        List files = result.getCheckedInFiles();

        assertNotNull( files );

        assertEquals( 2, files.size() );

        ScmFile file1 = (ScmFile) files.get( 0 );

        assertEquals( ScmFileStatus.CHECKED_IN, file1.getStatus() );

        assertPath( "/test-repo/check-in/Foo.java", file1.getPath() );

        ScmFile file2 = (ScmFile) files.get( 1 );

        assertEquals( ScmFileStatus.CHECKED_IN, file2.getStatus() );

        assertPath( "/test-repo/check-in/Readme.txt", file2.getPath() );

        assertNull( result.getProviderMessage() );

        assertNull( result.getCommandOutput() );
	}

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void changeFooJava( File fooJava )
    	throws Exception
    {
        String fooJavaContent = FileUtils.fileRead( fooJava );

        int startIndex = fooJavaContent.indexOf( '{' );

        int endIndex = fooJavaContent.indexOf( '}' );

        FileWriter output = new FileWriter( fooJava );

        PrintWriter printer = new PrintWriter( output );

        printer.println( fooJavaContent.substring( 0, startIndex + 1 ) );

        printer.println( "    public void foo()" );
        printer.println( "    {" );
        printer.println( "        int i = 10;" );
        printer.println( "    }" );

        printer.println( fooJavaContent.substring( endIndex ) );

        printer.close();

        output.close();
    }

    private void changeReadmeTxt( File readmeTxt )
    	throws Exception
    {
        FileWriter output = new FileWriter( readmeTxt );

        output.write( "changed file" );

        output.close();
    }
}
