package org.apache.maven.scm.provider.tfs.command;

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
import java.util.Iterator;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;

public class TfsCommandTest
    extends ScmTestCase
{

    protected TfsScmProviderRepository getScmProviderRepository()
    {
        return new TfsScmProviderRepository( "http://tfsurl", "user", "password", "serverpath", "workspace" );
    }

    protected String getFileList()
    {
        String path = "";
        for ( Iterator<File> i = getScmFileSet().getFileList().iterator(); i.hasNext(); )
        {
            File f = i.next();
            path += f.getName() + " ";
        }
        return path.trim();
    }

    protected ScmFileSet getScmFileSet()
    {
        return new ScmFileSet( getWorkingDirectory(), new File( "file" ) );
    }

    public void testFileList()
    {
        assertTrue( getScmFileSet().getFileList().size() > 0 );
    }

}
