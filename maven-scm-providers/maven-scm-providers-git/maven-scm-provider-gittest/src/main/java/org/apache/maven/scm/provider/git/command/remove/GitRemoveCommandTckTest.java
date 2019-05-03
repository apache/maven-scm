package org.apache.maven.scm.provider.git.command.remove;

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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.codehaus.plexus.util.FileUtils;

/**
 * This test tests the remove command.
 *
 * @author Georg Tsakumagos
 */
public abstract class GitRemoveCommandTckTest
    extends ScmTckTestCase
{
    /** {@inheritDoc} */
    public void initRepo()
        throws Exception
    {
        GitScmTestUtils.initRepo( "src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory() );
    }

    public void testCommandRemoveWithFile()
        throws Exception
    {
        File workingDirectory = createTempDirectory();

        File toBeRemoved = new File( workingDirectory.getAbsolutePath() + File.separator + "toto.xml" );

        Files.write( toBeRemoved.getAbsoluteFile().toPath(),
            "data".getBytes( StandardCharsets.UTF_8 ), StandardOpenOption.APPEND, StandardOpenOption.CREATE );

        ScmFileSet fileSet =  new ScmFileSet( getWorkingCopy(), toBeRemoved );
        RemoveScmResult removeResult = getScmManager().remove( getScmRepository(), fileSet, getBasedir() );
        assertResultIsSuccess( removeResult );

        FileUtils.deleteDirectory( workingDirectory );
    }

    public void testCommandRemoveWithDirectory()
        throws Exception
    {
        File workingDirectory = createTempDirectory();

        File toBeRemoved = new File( workingDirectory.getAbsolutePath() + File.separator + "toto" );
        toBeRemoved.mkdir();

        ScmFileSet fileSet =  new ScmFileSet( getWorkingCopy(), toBeRemoved );
        RemoveScmResult removeResult = getScmManager().remove( getScmRepository(), fileSet, getBasedir() );
        assertResultIsSuccess( removeResult );

        FileUtils.deleteDirectory( workingDirectory );
    }

    public void testCommandRemoveWithTwoDirectories()
        throws Exception
    {
        File workingDirectory = createTempDirectory();

        File toBeRemoved1 = new File( workingDirectory.getAbsolutePath() + File.separator + "toto" );
        toBeRemoved1.mkdir();

        File toBeRemoved2 = new File( workingDirectory.getAbsolutePath() + File.separator + "tata" );
        toBeRemoved2.mkdir();

        ScmFileSet fileSet =  new ScmFileSet( getWorkingCopy(), Arrays.asList( toBeRemoved1, toBeRemoved2 ) );
        RemoveScmResult removeResult = getScmManager().remove( getScmRepository(), fileSet, getBasedir() );
        assertResultIsSuccess( removeResult );
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
