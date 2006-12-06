package org.apache.maven.scm.provider.starteam.command.checkout;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.starteam.command.StarteamCommandLineUtils;
import org.apache.maven.scm.provider.starteam.repository.StarteamScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class StarteamCheckOutCommandTest
    extends ScmTestCase
{
    public void testGetCommandLine()
        throws Exception
    {
        ScmFileSet workingCopy = new ScmFileSet( this.getWorkingCopy() );

        String workDirAbsolutePath = StarteamCommandLineUtils.toJavaPath( workingCopy.getBasedir().getAbsolutePath() );

        String starteamUrl = "user:password@host:1234/project/view";
        String mavenUrl = "scm:starteam:" + starteamUrl;

        String expectedCmd =
            "stcmd co -x -nologo -stop" + " -p " + starteamUrl + " -fp " + workDirAbsolutePath + " -is -vl myTag";

        testCommandLine( mavenUrl, workingCopy, "myTag", expectedCmd );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, ScmFileSet workingCopy, String tag, String commandLine )
        throws Exception
    {
        ScmRepository repo = getScmManager().makeScmRepository( scmUrl );

        StarteamScmProviderRepository repository = (StarteamScmProviderRepository) repo.getProviderRepository();

        Commandline cl = StarteamCheckOutCommand.createCommandLine( repository, workingCopy, tag );

        assertEquals( commandLine, cl.toString() );
    }

}
