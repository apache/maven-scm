package org.apache.maven.scm.provider.cvslib.command.update;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.cvslib.AbstractCvsScmTest;
import org.apache.maven.scm.provider.cvslib.CvsScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsUpdateCommandTest
    extends AbstractCvsScmTest
{
    private File repository;

    private File workingDirectory;

    private File assertionDirectory;

    public void setUp()
        throws Exception
    {
        super.setUp();

        repository = getTestFile( "target/update-test/repository" );

        workingDirectory = getTestFile( "target/update-test/working-directory" );

        assertionDirectory = getTestFile( "target/update-test/assertion-directory" );

        CvsScmTestUtils.initRepo( repository, workingDirectory, assertionDirectory );
    }

    protected String getModule()
    {
        return "test-repo/update";
    }

    /**
     * @todo merge into tck
     */
    public void testCvsUpdate()
        throws Exception
    {
        ScmManager scmManager = getScmManager();

        String scmUrl = "scm:cvs:local:" + repository.getAbsolutePath() + ":" + getModule();

        // Check out the repo to a workding directory where files will be modified and committed
        String arguments =
            "-f -d " + repository.getAbsolutePath() + " " + "co -d " + workingDirectory.getName() + " " + getModule();

        CvsScmTestUtils.executeCVS( workingDirectory.getParentFile(), arguments );

        // Check out the repo to a assertion directory where the command will be used
        arguments =
            "-f -d " + repository.getAbsolutePath() + " " + "co -d " + assertionDirectory.getName() + " " + getModule();

        CvsScmTestUtils.executeCVS( assertionDirectory.getParentFile(), arguments );

        // A new check out should return 0 updated files.
        ScmRepository scmRepository = scmManager.makeScmRepository( scmUrl );

        UpdateScmResult result = scmManager.getProviderByRepository( scmRepository ).update( scmRepository,
                                                                                             new ScmFileSet(
                                                                                                 assertionDirectory ),
                                                                                             null );

        assertNotNull( result );

        if ( !result.isSuccess() )
        {
            System.out.println( "result.providerMessage: " + result.getProviderMessage() );

            System.out.println( "result.commandOutput: " + result.getCommandOutput() );

            fail( "Command failed" );
        }

        assertNull( result.getProviderMessage() );

        assertNull( result.getCommandOutput() );

        assertNotNull( result.getUpdatedFiles() );

        assertEquals( 0, result.getUpdatedFiles().size() );

        // Modifing a file
        File fooJava = new File( workingDirectory, "Foo.java" );

        String content = FileUtils.fileRead( fooJava );

        FileWriter writer = new FileWriter( fooJava );

        writer.write( content + System.getProperty( "line.separator" ) );
        writer.write( "extra line" );

        writer.close();

        // Adding a new file
        writer = new FileWriter( new File( workingDirectory, "New.txt" ) );

        writer.write( "new file" );

        writer.close();

        arguments = "-f -d " + repository.getAbsolutePath() + " add New.txt";

        CvsScmTestUtils.executeCVS( workingDirectory, arguments );

        // Committing
        arguments = "-f -d " + repository.getAbsolutePath() + " commit -m .";

        CvsScmTestUtils.executeCVS( workingDirectory, arguments );

        // Check the updated files
        result = scmManager.getProviderByRepository( scmRepository ).update( scmRepository,
                                                                             new ScmFileSet( assertionDirectory ),
                                                                             null );

        assertNotNull( result );

        if ( !result.isSuccess() )
        {
            System.out.println( "result.providerMessage: " + result.getProviderMessage() );

            System.out.println( "result.commandOutput: " + result.getCommandOutput() );

            fail( "Command failed" );
        }

        assertNull( result.getProviderMessage() );

        assertNull( result.getCommandOutput() );

        assertNotNull( result.getUpdatedFiles() );

        assertEquals( 2, result.getUpdatedFiles().size() );

        ScmFile file1 = (ScmFile) result.getUpdatedFiles().get( 0 );

        assertPath( "Foo.java", file1.getPath() );

        assertEquals( ScmFileStatus.UPDATED, file1.getStatus() );

        ScmFile file2 = (ScmFile) result.getUpdatedFiles().get( 1 );

        assertPath( "New.txt", file2.getPath() );

        assertEquals( ScmFileStatus.UPDATED, file2.getStatus() );
    }
}
