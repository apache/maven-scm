package org.apache.maven.scm.provider.svn.svnexe.command;

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
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class SvnCommandLineUtilsTest
    extends ScmTestCase
{
    public void testCryptPassword()
    {
        SvnScmProviderRepository repo =
            new SvnScmProviderRepository( "https://svn.apache.org/repos/asf/maven/scm/trunk", "username", "password" );
        String clString =
            SvnCommandLineUtils.cryptPassword( SvnCommandLineUtils.getBaseSvnCommandLine( new File( "." ), repo ) );
        Commandline expectedCmd = new Commandline( "svn --username username --password ***** --non-interactive" );
        expectedCmd.setWorkingDirectory( new File( "." ).getAbsolutePath() );
        assertEquals( expectedCmd.toString(), clString );

        repo = new SvnScmProviderRepository( "https://svn.apache.org/repos/asf/maven/scm/trunk", "username", null );
        clString =
            SvnCommandLineUtils.cryptPassword( SvnCommandLineUtils.getBaseSvnCommandLine( new File( "." ), repo ) );
        assertCommandLine( "svn --username username --non-interactive", new File( "." ),
                           SvnCommandLineUtils.getBaseSvnCommandLine( new File( "." ), repo ) );
    }
}
