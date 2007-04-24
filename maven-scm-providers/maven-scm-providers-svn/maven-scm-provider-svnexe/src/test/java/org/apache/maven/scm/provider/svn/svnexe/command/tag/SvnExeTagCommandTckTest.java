package org.apache.maven.scm.provider.svn.svnexe.command.tag;

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

import org.apache.maven.scm.provider.svn.command.tag.SvnTagCommandTckTest;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * This test tests the tag command.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SvnExeTagCommandTckTest
    extends SvnTagCommandTckTest
{
    public void testTagUserNameSvnSsh()
        throws Exception
    {
        File messageFile = File.createTempFile( "maven-scm", "commit" );
        messageFile.deleteOnExit();

        testCommandLine( "scm:svn:svn+ssh://foo.com/svn/trunk", "svntag", messageFile, "user",
                         "svn --username user --non-interactive copy --file " + messageFile.getAbsolutePath() +
                             " . svn+ssh://user@foo.com/svn/tags/svntag" );
    }

    private void testCommandLine( String scmUrl, String tag, File messageFile, String user, String commandLine )
        throws Exception
    {
        File workingDirectory = getTestFile( "target/svn-update-command-test" );

        ScmRepository repository = getScmManager().makeScmRepository( scmUrl );

        SvnScmProviderRepository svnRepository = (SvnScmProviderRepository) repository.getProviderRepository();

        svnRepository.setUser( user );

        Commandline cl = SvnTagCommand.createCommandLine( svnRepository, workingDirectory, tag, messageFile );

        assertEquals( commandLine, cl.toString() );
    }
}
