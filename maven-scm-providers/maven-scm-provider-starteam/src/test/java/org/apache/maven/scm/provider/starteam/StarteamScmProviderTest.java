package org.apache.maven.scm.provider.starteam;

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

import java.io.File;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmTestCase;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 * @version
 */
public class StarteamScmProviderTest
    extends ScmTestCase
{
    
    public void testGoodGetRelativeFile()
        throws Exception
    {
    	File basedir = new File ( getBasedir() );
    	
    	File testDir = new File( basedir.getPath() + "/target/../target/testdir" );
    	
    	testDir.mkdirs();
    	
    	File testFile = new File ( testDir, "testfile.txt" );
    	
    	testFile.createNewFile();
    	
    	String relativePath = StarteamScmProvider.getRelativePath( basedir, testFile );
    	
    	assertEquals( "target/testdir/testfile.txt", relativePath.replace('\\', '/') );
    	
    }
    
    public void testBadGetRelativeFile()
        throws Exception
    {
        File basedir = new File ( getBasedir() );
	
        File testDir1 = new File( basedir.getPath() + "/target/testdir1" );
        testDir1.mkdirs();

        File testDir2 = new File( basedir.getPath() + "/target/testdir2" );
        testDir2.mkdirs();
	
        File testFile = new File ( testDir1, "testfile.txt" );
	
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
    
 }
