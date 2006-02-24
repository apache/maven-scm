package org.apache.maven.scm;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.util.List;

/**
 * Base class for all TcK tests.
 * <p/>
 * Basically all it does is to setup a default test enviroment
 * common for all tck tests. The default setup includes: <br>
 * 1. Delete all default locations (working copy, updating copy etc) <br>
 * 2. Initialize the repository <br>
 * 3. Check out the repository to the working copy<br>
 * <br>
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public abstract class ScmTckTestCase
    extends ScmTestCase
{
    private ScmRepository scmRepository;

    /**
     * @return A provider spesific and valid url for the repository
     */
    public abstract String getScmUrl()
        throws Exception;

    /**
     * Initialize repository at the getScmUrl() location with the files:
     * <p/>
     * <br>
     * /pom.xml <br>
     * /readme.txt <br>
     * /src/main/java/Application.java <br>
     * /src/test/java/Test.java <br>
     * <br>
     * <p/>
     * The setup is also asserting on the existence of these files. <br>
     * This should only be used by this class (thus do not call this method from derived classes)
     */
    public abstract void initRepo()
        throws Exception;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        scmRepository = null;

        FileUtils.deleteDirectory( getRepositoryRoot() );
        FileUtils.deleteDirectory( getWorkingCopy() );
        FileUtils.deleteDirectory( getUpdatingCopy() );
        FileUtils.deleteDirectory( getAssertionCopy() );

        initRepo();

        checkOut( getWorkingCopy(), getScmRepository() );

        assertFile( getWorkingCopy(), "/pom.xml" );
        assertFile( getWorkingCopy(), "/readme.txt" );
        assertFile( getWorkingCopy(), "/src/main/java/Application.java" );
        assertFile( getWorkingCopy(), "/src/test/java/Test.java" );
    }

    /**
     * Convenience method to get the ScmRepository for this provider
     */
    protected ScmRepository getScmRepository()
        throws Exception
    {
        if ( scmRepository == null )
        {
            scmRepository = getScmManager().makeScmRepository( getScmUrl() );
        }

        return scmRepository;
    }

    /**
     * Convenience method to checkout files from the repository
     */
    protected CheckOutScmResult checkOut( File workingDirectory, ScmRepository repository )
        throws Exception
    {
        CheckOutScmResult result = getScmManager().getProviderByUrl( getScmUrl() )
            .checkOut( repository, new ScmFileSet( workingDirectory ), null );

        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        return result;
    }

    /**
     * Convenience method to add a file to the working tree at the working directory
     */
    protected void addToWorkingTree( File workingDirectory, File file, ScmRepository repository )
        throws Exception
    {
        ScmProvider provider = getScmManager().getProviderByUrl( getScmUrl() );
        AddScmResult result = provider.add( repository, new ScmFileSet( workingDirectory, file ) );

        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        List addedFiles = result.getAddedFiles();

        assertEquals( "Expected 1 file in the added files list " + addedFiles, 1, addedFiles.size() );
    }
}
