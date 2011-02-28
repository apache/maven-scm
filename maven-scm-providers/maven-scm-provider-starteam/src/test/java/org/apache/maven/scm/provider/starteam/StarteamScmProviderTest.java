package org.apache.maven.scm.provider.starteam;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;

import java.io.File;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 */
public class StarteamScmProviderTest
    extends ScmTestCase
{

    public void testGoodGetRelativeFile()
        throws Exception
    {
        File basedir = new File( getBasedir() );

        File testDir = new File( basedir.getPath() + "/target/../target/testdir" );

        testDir.mkdirs();

        File testFile = new File( testDir, "testfile.txt" );

        testFile.createNewFile();

        String relativePath = StarteamScmProvider.getRelativePath( basedir, testFile );
        relativePath = relativePath.replace( '\\', '/' ) ;
        assertEquals("not expected relativePath, found " + relativePath , "target/testdir/testfile.txt", relativePath);

    }

    public void testBadGetRelativeFile()
        throws Exception
    {
        File basedir = new File( getBasedir() );

        File testDir1 = new File( basedir.getPath() + "/target/testdir1" );
        testDir1.mkdirs();

        File testDir2 = new File( basedir.getPath() + "/target/testdir2" );
        testDir2.mkdirs();

        File testFile = new File( testDir1, "testfile.txt" );

        testFile.createNewFile();

        try
        {
            StarteamScmProvider.getRelativePath( testDir2, testFile );
            fail( "Bad relative path found!" );
        }
        catch ( ScmException e )
        {

        }

    }

    /**
     * To specify multiple views url, we must use '|'( pipe ) as separator,
     * must separate host and port using |
     *
     * @throws Exception
     */
    public void testMultipleViewsUrl()
        throws Exception
    {
        String scmSpecificUrl = "user:password@host|1234|/project/rootview:subview/folder";
        //String scmSpecificUrl = "user:password@host|1234/project/rootview:subview/folder"; //should work as well
        StarteamScmProvider provider = new StarteamScmProvider();
        StarteamScmProviderRepository starteamProvider =
            (StarteamScmProviderRepository) provider.makeProviderScmRepository( scmSpecificUrl, '|' );
        assertEquals( "user", starteamProvider.getUser() );
        assertEquals( "password", starteamProvider.getPassword() );
        assertEquals( 1234, starteamProvider.getPort() );
        assertEquals( "host", starteamProvider.getHost() );
        assertEquals( "/project/rootview:subview/folder", starteamProvider.getPath() );
        assertEquals( "user:password@host:1234/project/rootview:subview/folder", starteamProvider.getFullUrl() );
    }

}
