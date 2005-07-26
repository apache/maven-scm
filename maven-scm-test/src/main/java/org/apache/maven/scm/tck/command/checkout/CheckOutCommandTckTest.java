package org.apache.maven.scm.tck.command.checkout;

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
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This test tests the check out command.
 *
 * A check out has to produce these files:
 *
 * <ul>
 *   <li>/pom.xml</li>
 *   <li>/readme.txt</li>
 *   <li>/src/main/java/Application.java</li>
 *   <li>/src/test/java/Test.java</li>
 * </ul>
 *
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class CheckOutCommandTckTest
    extends ScmTestCase
{
    private File workingDirectory;

    // ----------------------------------------------------------------------
    // Methods the provider test has to implement
    // ----------------------------------------------------------------------

    public abstract String getScmUrl()
        throws Exception;

    /**
     * Copy the existing checked in repository to the working directory.
     *
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

    protected void setUp()
        throws Exception
    {
        super.setUp();

        File repositoryRoot = getRepositoryRoot();

        if ( repositoryRoot.exists() )
        {
            FileUtils.deleteDirectory( repositoryRoot );
        }

        assertTrue( "Could not make the repository root directory: " + repositoryRoot.getAbsolutePath(), repositoryRoot
            .mkdirs() );

        workingDirectory = getWorkingCopy();

        if ( workingDirectory.exists() )
        {
            FileUtils.deleteDirectory( workingDirectory );
        }

        assertTrue( "Could not make the working directory: " + workingDirectory.getAbsolutePath(), workingDirectory
            .mkdirs() );

        initRepo();
    }

    public void testCheckOutCommandTest()
        throws Exception
    {
        String tag = null;

        ScmManager scmManager = getScmManager();

        ScmRepository repository = scmManager.makeScmRepository( getScmUrl() );

        CheckOutScmResult result = scmManager.getProviderByUrl( getScmUrl() )
            .checkOut( repository, new ScmFileSet( workingDirectory ), tag );

        assertResultIsSuccess( result );

        assertNull( "The provider message wasn't null", result.getProviderMessage() );

        assertNull( "The command output wasn't null", result.getCommandOutput() );

        List checkedOutFiles = result.getCheckedOutFiles();

        if ( checkedOutFiles.size() != 4 )
        {
            SortedSet files = new TreeSet( checkedOutFiles );

            int i = 0;

            for ( Iterator it = files.iterator(); it.hasNext(); i++ )
            {
                ScmFile scmFile = (ScmFile) it.next();

                System.err.println( "" + i + ": " + scmFile );
            }

            fail( "Expected 4 files in the updated files list, was " + checkedOutFiles.size() );
        }
    }
}
