package org.apache.maven.scm.provider.git.gitexe.command.update;

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

import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Test;

import java.io.File;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 *
 */
public class GitUpdateCommandTest
    extends ScmTestCase
{
    @Test
    public void testCommandLineNoBranch()
        throws Exception
    {
        testCommandLine( "scm:git:http://foo.com/git", null, "git pull http://foo.com/git" );
    }

    @Test
    public void testCommandLineWithBranch()
    throws Exception
    {
        testCommandLine( "scm:git:http://foo.com/git", new ScmBranch( "mybranch" ), "git pull http://foo.com/git mybranch" );
    }

    @Test
    public void testCommandLineLatestRevision()
        throws Exception
    {
        testLatestRevisionCommandLine( "scm:git:http://foo.com/git", null, "git log -n1 --date-order"  );
    }
    
    // ----------------------------------------------------------------------
    // private helper functions
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, ScmVersion branch, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/git-update-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        GitScmProviderRepository gitRepository = (GitScmProviderRepository) repository.getProviderRepository();

        Commandline cl = GitUpdateCommand.createCommandLine( gitRepository, workingDirectory, branch );

        assertCommandLine( commandLine, workingDirectory, cl );
    }

    private void testLatestRevisionCommandLine( String scmUrl, ScmBranch branch, String commandLine )
    throws Exception
    {
        File workingDirectory = getTestFile( "target/git-update-command-test" );
    
        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );
    
        GitScmProviderRepository gitRepository = (GitScmProviderRepository) repository.getProviderRepository();
    
        Commandline cl = GitUpdateCommand.createLatestRevisionCommandLine( gitRepository, workingDirectory, branch );
    
        assertCommandLine( commandLine, workingDirectory, cl );
    }

}
