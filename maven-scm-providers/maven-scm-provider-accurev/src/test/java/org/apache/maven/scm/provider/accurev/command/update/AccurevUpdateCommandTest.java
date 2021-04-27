package org.apache.maven.scm.provider.accurev.command.update;

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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.accurev.Transaction;
import org.apache.maven.scm.provider.accurev.WorkSpace;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

public class AccurevUpdateCommandTest
    extends AbstractAccuRevCommandTest
{

    private ScmFileSet testFileSet;

    private File basedir;

    @Override
    public void setUp()
        throws Exception
    {
        super.setUp();
        testFileSet = new ScmFileSet( new File( "/my/workspace/project/dir" ) );
        basedir = testFileSet.getBasedir();

        info.setWorkSpace( "theWorkSpace" );
        when( accurev.info( basedir ) ).thenReturn( info );

    }

    @Test
    public void testUpdate()
        throws Exception
    {

        final File keptFile = new File( "updated/file" );
        final File keptAdded = new File( "new/file" );

        List<File> files = Arrays.asList( keptFile, keptAdded );

        when( accurev.update( eq( basedir ), anyString() ) ).thenReturn( files );

        AccuRevUpdateCommand command = new AccuRevUpdateCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.RUN_CHANGELOG_WITH_UPDATE, Boolean.toString( false ) );
        UpdateScmResult result = command.update( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getUpdatedFiles().size(), is( 2 ) );
        assertHasScmFile( result.getUpdatedFiles(), "updated/file", ScmFileStatus.UPDATED );
        assertHasScmFile( result.getUpdatedFiles(), "new/file", ScmFileStatus.UPDATED );

    }

    @Test
    public void testUpdateWithChangeLog()
        throws Exception
    {

        final WorkSpace wsBefore = new WorkSpace( "theWorkSpace", 123 );

        Map<String, WorkSpace> workspaces = Collections.singletonMap( "theWorkSpace", wsBefore );

        when( accurev.showWorkSpaces() ).thenReturn( workspaces );

        List<File> emptyList = Collections.emptyList();
        when( accurev.update( eq( basedir ), anyString() ) ).thenReturn( emptyList );

        final Date currentDate = new Date();
        List<Transaction> transactions =
            Collections.singletonList( new Transaction( 197L, currentDate, "type", "user" ) );

        when(
              accurev.history( ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(), ArgumentMatchers.<String>any(),
                      eq( 1 ), eq( true ), eq( true ) ) ).thenReturn( transactions );

        AccuRevUpdateCommand command = new AccuRevUpdateCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.RUN_CHANGELOG_WITH_UPDATE, Boolean.toString( true ) );
        UpdateScmResult result = command.update( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result, IsInstanceOf.instanceOf( AccuRevUpdateScmResult.class ) );
        AccuRevUpdateScmResult accuRevResult = (AccuRevUpdateScmResult) result;
        assertThat( accuRevResult.getFromRevision(), is( "theWorkSpace/123" ) );
        assertThat( accuRevResult.getToRevision(), is( "theWorkSpace/197" ) );

    }

    @Test
    public void testAccuRevFailure()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( new File( "/my/workspace/project/dir" ) );
        final File basedir = testFileSet.getBasedir();

        info.setWorkSpace( "theWorkSpace" );

        when( accurev.update( eq( basedir ), ArgumentMatchers.<String>any() ) ).thenReturn( null );

        AccuRevUpdateCommand command = new AccuRevUpdateCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.RUN_CHANGELOG_WITH_UPDATE, Boolean.toString( false ) );
        UpdateScmResult result = command.update( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );

    }
}
