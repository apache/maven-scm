package org.apache.maven.scm.provider.bazaar;

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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.provider.bazaar.command.BazaarConstants;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Common code used in all tests.
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarRepoUtils
    extends PlexusTestCase
{

    public static final String[] filesInTestBranch =
        new String[] { "pom.xml", "readme.txt", "src/main/java/Application.java", "src/test/java/Test.java" };

    public static final String TCK_FILE_CONSTANT = "/";

    public static final String BRANCH_NAME = "target" + File.separator + "test-branch";

    public static final File WORKING_DIR = new File( getBasedir(), BRANCH_NAME );

    public static final String COMMIT_MESSAGE = "Add files to test branch";

    public static String getScmUrl()
        throws Exception
    {
        return "scm:bazaar:file://" + WORKING_DIR.getAbsolutePath();
    }

    public static void initRepo()
        throws Exception
    {
        // Prepare tmp directory
        if ( WORKING_DIR.exists() )
        {
            FileUtils.deleteDirectory( WORKING_DIR );

            if ( WORKING_DIR.exists() )
            {
                throw new IOException( WORKING_DIR.getAbsolutePath() + " wasn't deleted." );
            }
        }

        boolean workingDirReady = WORKING_DIR.mkdirs();

        if ( !workingDirReady )
        {
            throw new IOException( "Could not initiate test branch at: " + WORKING_DIR );
        }

        // Init repository
        String[] init_cmd = new String[]{BazaarConstants.INIT_CMD};
        BazaarUtils.execute( WORKING_DIR, init_cmd );

        // Create and add files to repository
        List files = new ArrayList();
        for ( int i = 0; i < filesInTestBranch.length; i++ )
        {
            File file = new File( WORKING_DIR.getAbsolutePath(), filesInTestBranch[i] );
            if ( file.getParentFile() != null && !file.getParentFile().exists() )
            {
                boolean success = file.getParentFile().mkdirs();
                if ( !success )
                {
                    throw new IOException( "Could not create directories in branch for: " + file );
                }
            }
            file.createNewFile();

            FileUtils.fileWrite( file.getAbsolutePath(), TCK_FILE_CONSTANT + filesInTestBranch[i] );

            files.add( file );
        }

        //Add to repository
        String[] add_cmd = new String[] { BazaarConstants.ADD_CMD };
        ScmFileSet filesToAdd = new ScmFileSet( new File( "" ), files );
        add_cmd = BazaarUtils.expandCommandLine( add_cmd, filesToAdd );
        ScmResult result = BazaarUtils.execute( WORKING_DIR, add_cmd );
        if ( !result.isSuccess() )
        {
            String message =
                "Provider message: " + result.getProviderMessage() + "\n" + "Output: " + result.getCommandOutput();
            throw new Exception( message );
        }

        // Commit the initial repository
        String[] commit_cmd = new String[]{BazaarConstants.COMMIT_CMD, BazaarConstants.MESSAGE_OPTION, COMMIT_MESSAGE};
        result = BazaarUtils.execute( WORKING_DIR, commit_cmd );
        if ( !result.isSuccess() )
        {
            String message =
                "Provider message: " + result.getProviderMessage() + "\n" + "Output: " + result.getCommandOutput();
            throw new Exception( message );
        }
    }
}
