package org.apache.maven.scm.provider.cvslib.command.mkdir;

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

import org.apache.maven.scm.provider.cvslib.CvsScmTestUtils;
import org.apache.maven.scm.tck.command.mkdir.MkdirCommandTckTest;
import org.codehaus.plexus.util.FileUtils;

public class CvsMkdirCommandTckTest
    extends MkdirCommandTckTest
{
    /** {@inheritDoc} */
    public String getScmUrl()
    {
        return CvsScmTestUtils.getScmUrl( getRepositoryRoot(), getModule() );
    }

    /** {@inheritDoc} */
    protected String getModule()
    {
        return "test-repo/module";
    }

    /** {@inheritDoc} */
    public void initRepo()
        throws Exception
    {
        // TODO: should have an assertion directory?
        CvsScmTestUtils.initRepo( "src/test/tck-repository/", getRepositoryRoot(), getWorkingDirectory() );
    }

    protected void setUp()
        throws Exception
    {
        super.setUp();

        // create the directory to be added
        FileUtils.mkdir( new File( getWorkingDirectory(), getMissingDirectory() ).getPath() );
        assertTrue( new File( getWorkingDirectory(), getMissingDirectory() ).exists() );
    }
}
