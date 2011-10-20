package org.apache.maven.scm.provider.svn.svnexe.command.info;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

public class SvnInfoCommandTest
    extends ScmTestCase
{

    public void testGetInfoOnEmptyFileSet()
        throws Exception
    {
        ScmFileSet fileSet = new ScmFileSet( new File( getBasedir() ) );
        
        testCommandLine( "scm:svn:http://foo.com/svn/trunk", fileSet, "svn --non-interactive info" );
    }
    
    private void testCommandLine( String scmUrl, ScmFileSet fileSet, String commandLine )
        throws Exception
    {
        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        Commandline cl = SvnInfoCommand.createCommandLine( svnRepository, fileSet, false, null );
        
        assertCommandLine( commandLine, fileSet.getBasedir(), cl );
    }
}
