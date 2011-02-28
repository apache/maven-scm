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

import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

public class TfsTagCommandTest
    extends TfsCommandTest
{

    public void testCommandline()
        throws Exception    
    {
        TfsScmProviderRepository repo = getScmProviderRepository();
        ScmTagParameters param = new ScmTagParameters( "Message of many words" );
        Commandline cmd = new TfsTagCommand().createCommand( repo, getScmFileSet(), "tag", param ).getCommandline();
        String expected =
            "tf label -login:user,password tag " + repo.getServerPath()
                + " -recursive -child:replace -comment:\"Message of many words\"";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

}
