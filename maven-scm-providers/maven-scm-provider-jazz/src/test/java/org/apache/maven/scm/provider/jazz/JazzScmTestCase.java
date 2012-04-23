package org.apache.maven.scm.provider.jazz;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;

/**
 * The base class for our Jazz test cases.
 * It sets up a dummy JazzScmProviderRepository for testing purposes.
 * 
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public abstract class JazzScmTestCase
    extends ScmTestCase
{

    /**
     * Get a dummy JazzScmProviderRepostitory to allow us to test.
     * @return The dummy JazzScmProviderRepository.
     */
    protected JazzScmProviderRepository getScmProviderRepository()
    {
        return new JazzScmProviderRepository( "https://localhost:9443/jazz", "myUserName", "myPassword",
                                              "localhost", 9443, "Dave's Repository Workspace" );
    }

    /**
     * Return a list of our files, space separated as a single string.
     * @return The list of files.
     */
    protected String getFiles()
    {
        String path = "";
        for ( Iterator<File> it = getScmFileSet().getFileList().iterator(); it.hasNext(); )
        {
            File file = (File) it.next();
            path += file.getName() + " ";
        }
        return path.trim();
    }

    /**
     * Return a dummy set of files.
     * @return A ScmFileSet of dummy files to allow us to test. 
     */
    protected ScmFileSet getScmFileSet()
    {
        File file1 = new File( "file1" );
        File file2 = new File( "file2" );
        List<File> fileList = new ArrayList<File>();

        fileList.add( file1 );
        fileList.add( file2 );

        return new ScmFileSet( getWorkingDirectory(), fileList );
    }
}
