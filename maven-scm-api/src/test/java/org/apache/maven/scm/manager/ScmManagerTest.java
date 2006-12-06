package org.apache.maven.scm.manager;

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

import junit.framework.TestCase;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class ScmManagerTest
    extends TestCase
{
    public void testCleanScmUrl()
        throws Exception
    {
        BasicScmManager manager = new BasicScmManager();

        assertEquals( "https://svn.apache.org/repos/asf/maven/scm/trunk/maven-scm-api",
                      manager.cleanScmUrl( "https://svn.apache.org/repos/asf/maven/scm/trunk/maven-scm-api" ) );
        assertEquals( "https://svn.apache.org/repos/asf/maven/scm/trunk/maven-scm-manager", manager.cleanScmUrl(
            "https://svn.apache.org/repos/asf/maven/scm/trunk/maven-scm-api/../maven-scm-manager" ) );
        assertEquals( "https://svn.apache.org/repos/asf/maven/scm/trunk/",
                      manager.cleanScmUrl( "https://svn.apache.org/repos/asf/maven/scm/trunk/maven-scm-api/../" ) );
        assertEquals( "d:\\myrepo\\mydir", manager.cleanScmUrl( "d:\\myrepo\\mydir" ) );
        assertEquals( "d:\\myrepo\\mydir2", manager.cleanScmUrl( "d:\\myrepo\\mydir\\..\\mydir2" ) );
        assertEquals( "//depot/repos/...", manager.cleanScmUrl( "//depot/repos/..." ) );
        assertEquals( "//depot/repo2/...", manager.cleanScmUrl( "//depot/repos/../repo2/..." ) );
    }
}
