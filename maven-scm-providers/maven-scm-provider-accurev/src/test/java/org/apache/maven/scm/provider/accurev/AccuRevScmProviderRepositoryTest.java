package org.apache.maven.scm.provider.accurev;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;

import org.junit.Test;

public class AccuRevScmProviderRepositoryTest
{

    @Test
    public void testIsBasedirTheWorkspaceRoot()
    {
        // basedir is workspace root if it is the "top" of the workspace
        // or it is top/projectPath

        AccuRevInfo info = new AccuRevInfo( new File( "/my/workspace/project/path" ) );
        info.setTop( "/my/workspace" );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setProjectPath( "/project/path" );

        assertThat( repo.isWorkSpaceRoot( info ), is( true ) );

        info = new AccuRevInfo( new File( "/my/workspace/project/path/subdir" ) );
        info.setTop( "/my/workspace" );
        assertThat( repo.isWorkSpaceRoot( info ), is( false ) );
    }
}
