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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.StringUtils;

/**
 * <p/>
 * Base class for all TcK tests.
 * </p>
 * <p/>
 * Basically all it does is to setup a default test enviroment
 * common for all tck tests. The default setup includes:
 * <ol>
 * <li>Delete all default locations (working copy, updating copy etc)</li>
 * <li>Initialize the repository</li>
 * <li>Check out the repository to the working copy</li>
 * </ol>
 * </p>
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbj�rn Eikli Sm�rgrav</a>
 * @version $Id$
 */
public abstract class ScmTckTestCase
    extends ScmTestCase
{
    private ScmRepository scmRepository;

    private List<String> scmFileNames;

    /**
     * @return A provider specific and valid url for the repository
     * @throws Exception if any
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
    protected List<String> getScmFileNames()
    {
        return scmFileNames;
    }

    /**
     * <p/>
     * Initialize repository at the {@link #getScmUrl()} location with the files in {@link #getScmFileNames()}
     * </p>
     * <p/>
     * The setup is also asserting on the existence of these files. <br>
     * This should only be used by this class (thus do not call this method from derived classes)
     * </p>
     * <b>Note</b>: 'svnadmin' should be a system command.
     *
     * @throws Exception if any
     */
    public abstract void initRepo()
        throws Exception;

    /**
     * {@inheritDoc}
     */
    protected void setUp()
        throws Exception
    {
        super.setUp();

        scmRepository = null;

        scmFileNames = new ArrayList<String>( 4 );
        scmFileNames.add( "/pom.xml" );
        scmFileNames.add( "/readme.txt" );
        scmFileNames.add( "/src/main/java/Application.java" );
        scmFileNames.add( "/src/test/java/Test.java" );

        initRepo();

        checkOut( getWorkingCopy(), getScmRepository() );

        Iterator<String> it = getScmFileNames().iterator();
        while ( it.hasNext() )
        {
            assertFile( getWorkingCopy(), it.next() );
        }
    }

    /**
     * This method is available to those SCM clients that need to perform
     * a cleanup at the end of the tests. It is needed when server side
     * operations are performed, or the check out dirs are outside
     * of the normal target directory.
     */
    public void removeRepo()
        throws Exception
    {
    }

    /**
     * Provided to allow removeRepo() to be called. 
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown()
        throws Exception
    {
        super.tearDown();
        removeRepo();
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
     * Convenience method to check out files from the repository
     */
    protected CheckOutScmResult checkOut( File workingDirectory, ScmRepository repository )
        throws Exception
    {
        CheckOutScmResult result =
            getScmManager().getProviderByUrl( getScmUrl() ).checkOut( repository, new ScmFileSet( workingDirectory ),
                                                                      (ScmVersion) null );

        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        return result;
    }

    /**
     * Convenience method to check in files to the repository
     */
    protected CheckInScmResult checkIn( File workingDirectory, ScmRepository repository )
        throws Exception
    {
        CheckInScmResult result = getScmManager().getProviderByUrl( getScmUrl() )
            .checkIn( repository, new ScmFileSet( workingDirectory ), (ScmVersion) null, "Initial Checkin" );

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

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.FORCE_ADD, Boolean.TRUE.toString() );

        AddScmResult result = provider.add( repository, new ScmFileSet( workingDirectory, file ), commandParameters );

        assertTrue( "Check result was successful, output: " + result.getCommandOutput(), result.isSuccess() );

        List<ScmFile> addedFiles = result.getAddedFiles();

        if ( new File( workingDirectory, file.getPath() ).isFile() )
        {
            // Don't check directory add because some SCM tools ignore it
            assertEquals( "Expected 1 file in the added files list " + addedFiles, 1, addedFiles.size() );
        }
    }

    /**
     * take the files of the given list, add them to a TreeMap and
     * use the pathName String as key for the Map.
     * This function is useful for every TCK which has to check for the
     * existence of more than 1 file of the returned ScmResult, regardless
     * of their order in the list.
     * All backslashes in the path will be replaced by forward slashes
     * for Windows compatibility.
     *
     * @param files List with {@code ScmFile}s
     * @return Map key=pathName, value=ScmFile
     */
    protected Map<String, ScmFile> mapFilesByPath( List<ScmFile> files )
    {
        if ( files == null )
        {
            return null;
        }

        Map<String, ScmFile> mappedFiles = new TreeMap<String, ScmFile>();
        for ( ScmFile scmFile : files )
        {
            String path = StringUtils.replace( scmFile.getPath(), "\\", "/" );
            mappedFiles.put( path, scmFile );
        }

        return mappedFiles;
    }
}
