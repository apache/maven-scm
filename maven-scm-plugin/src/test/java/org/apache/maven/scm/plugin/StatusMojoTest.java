package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.SvnScmTestUtils;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StatusMojoTest
    extends AbstractMojoTestCase
{
    public void testStatusMojo()
        throws Exception
    {
        if ( !ScmTestCase.isSystemCmd( SvnScmTestUtils.SVN_COMMAND_LINE ) )
        {
            System.err.println( "'" + SvnScmTestUtils.SVN_COMMAND_LINE
                + "' is not a system command. Ignored " + getName() + "." );
            return;
        }

        StatusMojo mojo =
            (StatusMojo) lookupMojo( "status", getTestFile( "src/test/resources/mojos/status/status.xml" ) );

        mojo.setWorkingDirectory( new File( getBasedir() ) );
        mojo.execute();
    }
}
