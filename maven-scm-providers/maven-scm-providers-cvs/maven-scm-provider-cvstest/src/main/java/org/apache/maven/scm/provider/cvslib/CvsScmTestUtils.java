package org.apache.maven.scm.provider.cvslib;

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

import org.apache.maven.scm.ScmTestCase;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.junit.Assert;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public final class CvsScmTestUtils
{
    /** 'cvs' command line */
    public static final String CVS_COMMAND_LINE = "cvs";

    private CvsScmTestUtils()
    {
    }

    public static String getScmUrl( File repository, String module )
    {
        return "scm:cvs|local|" + repository + "|" + module;
    }

    public static void executeCVS( File workingDirectory, String arguments )
        throws Exception
    {
        ScmTestCase.execute( workingDirectory, CVS_COMMAND_LINE, arguments );
    }

    public static void initRepo( File repository, File workingDirectory, File assertionDirectory )
        throws IOException
    {
        initRepo( "src/test/repository/", repository, workingDirectory );

        FileUtils.deleteDirectory( assertionDirectory );

        Assert.assertTrue( assertionDirectory.mkdirs() );
    }

    public static void initRepo( String source, File repository, File workingDirectory )
        throws IOException
    {
        // Copy the repository to target
        File src = PlexusTestCase.getTestFile( source );

        FileUtils.deleteDirectory( repository );

        Assert.assertTrue( repository.mkdirs() );

        FileUtils.copyDirectoryStructure( src, repository );

        FileUtils.deleteDirectory( workingDirectory );

        Assert.assertTrue( workingDirectory.mkdirs() );
    }
}
