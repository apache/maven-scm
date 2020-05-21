package org.apache.maven.scm.provider.accurev.command.remove;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.apache.maven.scm.ScmFileMatcher.assertHasScmFile;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.junit.Test;

public class AccuRevRemoveCommandTest
    extends AbstractAccuRevCommandTest
{

    @Test
    public void testRemove()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( basedir, new File( "src/main/java/Foo.java" ) );

        List<File> removedFiles = Collections.singletonList( new File( "removed/file" ) );

        when( accurev.defunct( basedir, testFileSet.getFileList(), "A deleted file" ) ).thenReturn( removedFiles );

        AccuRevRemoveCommand command = new AccuRevRemoveCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A deleted file" );
        RemoveScmResult result = command.remove( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getRemovedFiles().size(), is( 1 ) );
        assertHasScmFile( result.getRemovedFiles(), "removed/file", ScmFileStatus.DELETED );
    }

    @Test
    public void testAddFailed()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( basedir, new File( "src/main/java/Foo.java" ) );

        when( accurev.defunct( basedir, testFileSet.getFileList(), "A deleted file" ) ).thenReturn( null );

        AccuRevRemoveCommand command = new AccuRevRemoveCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A deleted file" );
        RemoveScmResult result = command.remove( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );
    }

}
