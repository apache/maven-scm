package org.apache.maven.scm;

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

import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p/>
 * Base class for all TcK tests.
 * </p>
 * <p/>
 * Basically all it does is to setup a default test enviroment
 * common for all tck tests. The default setup includes: <br>
 * 1. Delete all default locations (working copy, updating copy etc) <br>
 * 2. Initialize the repository <br>
 * 3. Check out the repository to the working copy<br>
 * </p>
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a>
 */
public abstract class ScmTckTestCase
    extends ScmTestCase
{
    private ScmRepository scmRepository;

    private List scmFileNames;

    /**
     * @return A provider spesific and valid url for the repository
     */
    public abstract String getScmUrl()
        throws Exception;

    /**
     * <p/>
     * Get the list of file names that is supposed to be in the test repo.
     * </p>
     * <ul>
     * <li>/pom.xml</li>
     * <li>/readme.txt</li>
     * <li>/src/main/java/Application.java</li>
     * <li>/src/test/java/Test.java</li>
     * </ul>
     *
     * @return {@link List} of {@link String} objects
     */
    protected List getScmFileNames()
    {
        return scmFileNames;
    }

    /**
     * <p/>
     * Initialize repository at the {@link #getScmUrl()} location with the files in {@link #getScmFiles()}
     * </p>
     * <p/>
     * The setup is also asserting on the existence of these files. <br>
     * This should only be used by this class (thus do not call this method from derived classes)
     * </p>
     */
    public abstract void initRepo()
        throws Exception;

    protected void setUp()
        throws Exception
    {
        super.setUp();

        scmRepository = null;

        initRepo();

        checkOut( getWorkingCopy(), getScmRepository() );

        scmFileNames = new ArrayList( 4 );
        scmFileNames.add( "/pom.xml" );
        scmFileNames.add( "/readme.txt" );
        scmFileNames.add( "/src/main/java/Application.java" );
        scmFileNames.add( "/src/test/java/Test.java" );

        Iterator it = getScmFileNames().iterator();
        while ( it.hasNext() )
        {
            assertFile( getWorkingCopy(), (String) it.next() );
        }
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
