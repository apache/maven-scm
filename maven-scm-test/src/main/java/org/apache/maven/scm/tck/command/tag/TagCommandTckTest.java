package org.apache.maven.scm.tck.command.tag;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.FileWriter;

/**
 * This test tests the tag command.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public abstract class TagCommandTckTest extends ScmTestCase
{
    private File workingDirectory;
    // ----------------------------------------------------------------------
    // Methods the provider test has to implement
    // ----------------------------------------------------------------------

    public abstract String getScmUrl()
        throws Exception;

    /**
     * Copy the existing checked in repository to the working directory.
     * <p/>
     * (src/test/repository/my-cvs-repository)
     *
     * @throws Exception
     */
    public abstract void initRepo()
        throws Exception;

    // ----------------------------------------------------------------------
    // Directories the test must use
    // ----------------------------------------------------------------------

    protected File getRepositoryRoot()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/repository" );
    }

    protected File getWorkingCopy()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/working-copy" );
    }

    protected File getAssertionCopy()
    {
        return PlexusTestCase.getTestFile( "target/scm-test/assertion-copy" );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        File repositoryRoot = getRepositoryRoot();

        if ( repositoryRoot.exists() )
        {
            FileUtils.deleteDirectory( repositoryRoot );
        }

        assertTrue( "Could not make the repository root directory: " + repositoryRoot.getAbsolutePath(),
                    repositoryRoot.mkdirs() );

        workingDirectory = getWorkingCopy();

        if ( workingDirectory.exists() )
        {
            FileUtils.deleteDirectory( workingDirectory );
        }

        assertTrue( "Could not make the working directory: " + workingDirectory.getAbsolutePath(),
                    workingDirectory.mkdirs() );

        File assertionDirectory = getAssertionCopy();

        if ( assertionDirectory.exists() )
        {
            FileUtils.deleteDirectory( assertionDirectory );
        }

        assertTrue( "Could not make the assertion directory: " + assertionDirectory.getAbsolutePath(),
                    assertionDirectory.mkdirs() );

        initRepo();
    }

    public void testTagCommandTest()
        throws Exception
    {
        ScmManager scmManager = getScmManager();

        ScmRepository repository = getScmRepository( scmManager );

        CheckOutScmResult checkoutResult = scmManager.checkOut( repository, new ScmFileSet( workingDirectory ), null );

        assertResultIsSuccess( checkoutResult );

        String tag = "test-tag";

        TagScmResult tagResult = scmManager.tag( repository, new ScmFileSet( workingDirectory ), tag );

        assertResultIsSuccess( tagResult );

        assertEquals( "check all 4 files tagged", 4, tagResult.getTaggedFiles().size() );

        File readmeTxt = new File( workingDirectory, "readme.txt" );

        assertEquals( "check readme.txt contents", "/readme.txt", FileUtils.fileRead( readmeTxt ) );

        changeReadmeTxt( readmeTxt );

        CheckInScmResult checkinResult = scmManager.checkIn( repository, new ScmFileSet( workingDirectory ), null,
                                                             "commit message" );

        assertResultIsSuccess( checkinResult );

        checkoutResult = scmManager.checkOut( repository, new ScmFileSet( getAssertionCopy() ), null );

        assertResultIsSuccess( checkoutResult );

        readmeTxt = new File( getAssertionCopy(), "readme.txt" );

        assertEquals( "check readme.txt contents", "changed file", FileUtils.fileRead( readmeTxt ) );

        FileUtils.deleteDirectory( getAssertionCopy() );

        assertFalse( "check previous assertion copy deleted", getAssertionCopy().exists() );

        checkoutResult = scmManager.checkOut( repository, new ScmFileSet( getAssertionCopy() ), tag );

        assertResultIsSuccess( checkoutResult );

        assertEquals( "check readme.txt contents is from tagged version", "/readme.txt",
                      FileUtils.fileRead( readmeTxt ) );
    }

    protected ScmRepository getScmRepository( ScmManager scmManager )
        throws Exception
    {
        return scmManager.makeScmRepository( getScmUrl() );
    }

    private void changeReadmeTxt( File readmeTxt )
        throws Exception
    {
        FileWriter output = new FileWriter( readmeTxt );

        output.write( "changed file" );

        output.close();
    }
}
