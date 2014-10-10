package org.apache.maven.scm.provider.git.gitexe.command.tag;

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
import org.apache.maven.scm.provider.git.repository.GitScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.text.MessageFormat;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitTagCommandTest
    extends ScmTestCase
{
    private File messageFile;

    private String messageFileString;

    public void setUp()
        throws Exception
    {
        super.setUp();

        messageFile = new File( "commit-message" );

        String path = messageFile.getAbsolutePath();
        if ( path.indexOf( ' ' ) >= 0 )
        {
            path = "\"" + path + "\"";
        }
        messageFileString = "-F " + path;
    }

    public void testCommandLineTag()
        throws Exception
    {
        testCommandLine( "scm:git:http://foo.com/git/trunk", "my-tag-1", "git tag " + messageFileString + " my-tag-1" );
    }

    public void testCommandLineWithUsernameAndTag()
        throws Exception
    {
        testCommandLine( "scm:git:http://anonymous@foo.com/git/trunk", "my-tag-1",
                         "git tag " + messageFileString + " my-tag-1" );
    }

    public void testPushCommandLineWithUsernameAndPassword()
        throws Exception
    {
    	final String scmProtocol = "scm:git:";
    	
        final String scmUrl = "https://user:password@foo.com/git/trunk";
        final String tag = "my-tag-1";
        
        final ScmRepository repository = getScmManager().makeScmRepository( scmProtocol.concat( scmUrl ) );
        final GitScmProviderRepository gitRepository = (GitScmProviderRepository) repository.getProviderRepository();
        
        final Commandline cl = GitTagCommand.createPushCommandLine( gitRepository, null, tag );
        
        assertCommandLine( "git push https://user:password@foo.com/git/trunk refs/tags/my-tag-1", null, cl );

        // Message that should appear in the output log as the result of toString()
        final String scmUrlFakeForTest="https://user:********@foo.com/git/trunk";
        
        assertTrue( MessageFormat.format( "The target log message should contain <{0}> but it contains <{1}>",
            scmUrlFakeForTest, cl.toString() ), cl.toString().contains( scmUrlFakeForTest ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    private void testCommandLine( String scmUrl, String tag, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/git-checkin-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        GitScmProviderRepository gitRepository = (GitScmProviderRepository) repository.getProviderRepository();

        Commandline cl = GitTagCommand.createCommandLine( gitRepository, workingDirectory, tag, messageFile );

        assertCommandLine( commandLine, workingDirectory, cl );
    }
}
