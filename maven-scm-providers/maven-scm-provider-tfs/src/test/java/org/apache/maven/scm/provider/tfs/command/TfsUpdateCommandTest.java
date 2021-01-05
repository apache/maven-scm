package org.apache.maven.scm.provider.tfs.command;

import java.io.IOException;

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

import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

public class TfsUpdateCommandTest
    extends TfsCommandTest
{

    public void testCommandLine()
        throws IOException    
    {
        TfsScmProviderRepository repo = getScmProviderRepository();
        ScmRevision rev = new ScmRevision( "revision" );
        Commandline cmd = new TfsUpdateCommand().createCommand( repo, getScmFileSet(), rev ).getCommandline();
        String path = repo.getServerPath();
        String expected = "tf get -login:user,password " + path + " -version:Crevision";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }
    
    public void testCommandLine_emptyName()
        throws IOException
    {
        TfsScmProviderRepository repo = getScmProviderRepository();
        ScmRevision rev = new ScmRevision( "" );
        Commandline cmd = new TfsUpdateCommand().createCommand( repo, getScmFileSet(), rev ).getCommandline();
        String path = repo.getServerPath();
        String expected = "tf get -login:user,password " + path;
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

}
