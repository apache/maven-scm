package org.apache.maven.scm.provider.git.gitexe.command.remove;

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

import org.apache.maven.scm.provider.git.gitexe.GitExeTestCase;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public class GitRemoveCommandTest
    extends GitExeTestCase
{

    public void testCommandRemoveWithFile()
        throws Exception
    {
        File workingDirectory = createTempDirectory();

        File toBeRemoved = new File( workingDirectory.getAbsolutePath() + File.separator + "toto.xml" );
        FileUtils.fileAppend( toBeRemoved.getAbsolutePath(), "data" );

        Commandline cl = GitRemoveCommand.createCommandLine( workingDirectory, Arrays.asList( toBeRemoved ) );

        assertCommandLine( "git rm toto.xml", workingDirectory, cl );

        FileUtils.deleteDirectory( workingDirectory );
    }

    public void testCommandRemoveWithDirectory()
        throws Exception
    {
        File workingDirectory = createTempDirectory();

        File toBeRemoved = new File( workingDirectory.getAbsolutePath() + File.separator + "toto" );
        toBeRemoved.mkdir();

        Commandline cl = GitRemoveCommand.createCommandLine( workingDirectory, Arrays.asList( toBeRemoved ) );

        assertCommandLine( "git rm -r toto", workingDirectory, cl );

        FileUtils.deleteDirectory( workingDirectory );
    }

    public void testCommandRemoveWithTwoDirectory()
        throws Exception
    {
        File workingDirectory = createTempDirectory();

        File toBeRemoved1 = new File( workingDirectory.getAbsolutePath() + File.separator + "toto" );
        toBeRemoved1.mkdir();

        File toBeRemoved2 = new File( workingDirectory.getAbsolutePath() + File.separator + "tata" );
        toBeRemoved2.mkdir();

        Commandline cl =
            GitRemoveCommand.createCommandLine( workingDirectory, Arrays.asList( toBeRemoved1, toBeRemoved2 ) );

        assertCommandLine( "git rm -r toto tata", workingDirectory, cl );

        FileUtils.deleteDirectory( workingDirectory );
    }

    private File createTempDirectory()
        throws IOException
    {
        File dir = File.createTempFile( "gitexe", "test" );
        dir.delete();
        dir.mkdir();
        return dir;
    }
}
