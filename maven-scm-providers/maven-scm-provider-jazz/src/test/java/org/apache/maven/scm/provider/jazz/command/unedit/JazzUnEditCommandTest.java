package org.apache.maven.scm.provider.jazz.command.unedit;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.provider.jazz.JazzScmTestCase;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

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

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzUnEditCommandTest
    extends JazzScmTestCase
{
    private JazzScmProviderRepository repo;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        repo = getScmProviderRepository();
    }

    public void testCreateUneditCommandWithSpecificFiles()
        throws Exception
    {
        Commandline cmd = new JazzUnEditCommand().createUneditCommand( repo, getScmFileSet() ).getCommandline();
        String expected =
            "scm lock release --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword file1 file2";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testCreateUneditCommandWithEmptyFileSet()
        throws Exception
    {
        ScmFileSet scmFileSet = new ScmFileSet( new File( "." ) );
        // An empty file set will be all files, which jazz will take as a "."
        Commandline cmd =
            new JazzUnEditCommand().createUneditCommand( repo, scmFileSet ).getCommandline();
        String expected =
            "scm lock release --repository-uri https://localhost:9443/jazz --username myUserName --password myPassword .";
        assertCommandLine( expected, scmFileSet.getBasedir(), cmd );
    }
}
