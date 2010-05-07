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
import static org.apache.maven.scm.provider.accurev.AddElementsAction.addElementsTo;
import static org.apache.maven.scm.provider.accurev.PutMapEntryAction.putEntryTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.WorkSpace;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.hamcrest.core.IsInstanceOf;
import org.jmock.Expectations;
import org.junit.Test;

public class AccurevUpdateCommandTest
    extends AbstractAccuRevCommandTest
{

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdate()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( new File( "/my/workspace/project/dir" ) );
        final File basedir = testFileSet.getBasedir();

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        info.setWorkSpace( "theWorkSpace" );

        context.checking( new Expectations()
        {
            {
                File keptFile = new File( "updated/file" );
                File keptAdded = new File( "new/file" );

                one( accurev ).info( basedir );
                will( returnValue( info ) );

                // TODO Should test that the timeSpec parameter is a formatted date
                one( accurev ).update( with( basedir ), with( any( String.class ) ), with( any( List.class ) ) );
                will( doAll( addElementsTo( 2, keptFile, keptAdded ), returnValue( true ) ) );

            }
        } );

        AccuRevUpdateCommand command = new AccuRevUpdateCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.RUN_CHANGELOG_WITH_UPDATE, Boolean.toString( false ) );
        UpdateScmResult result = command.update( repo, testFileSet, commandParameters );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getUpdatedFiles().size(), is( 2 ) );
        assertHasScmFile( result.getUpdatedFiles(), "updated/file", ScmFileStatus.UPDATED );
        assertHasScmFile( result.getUpdatedFiles(), "new/file", ScmFileStatus.UPDATED );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUpdateWithChangeLog()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( new File( "/my/workspace/project/dir" ) );
        final File basedir = testFileSet.getBasedir();

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        info.setWorkSpace( "theWorkSpace" );
        info.setBasis( "myStream" );
        context.checking( new Expectations()
        {
            {
                WorkSpace wsBefore = new WorkSpace( "theWorkSpace", 123 );

                one( accurev ).info( basedir );
                will( returnValue( info ) );

                one( accurev ).showWorkSpaces( with( any( Map.class ) ) );
                will( doAll( putEntryTo( 0, "theWorkSpace", wsBefore ), returnValue( true ) ) );

                one( accurev ).update( with( basedir ), with( any( String.class ) ), with( any( List.class ) ) );
                will( returnValue( true ) );

            }
        } );

        AccuRevUpdateCommand command = new AccuRevUpdateCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.RUN_CHANGELOG_WITH_UPDATE, Boolean.toString( true ) );
        UpdateScmResult result = command.update( repo, testFileSet, commandParameters );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result, IsInstanceOf.instanceOf( AccuRevUpdateScmResult.class ) );
        AccuRevUpdateScmResult accuRevResult = (AccuRevUpdateScmResult) result;
        assertThat( accuRevResult.getFromVersion().getTimeSpec(), is( "124" ) );
        assertThat( accuRevResult.getToVersion().getTimeSpec(), nullValue() );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAccuRevFailure()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( new File( "/my/workspace/project/dir" ) );
        final File basedir = testFileSet.getBasedir();

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        info.setWorkSpace( "theWorkSpace" );

        context.checking( new Expectations()
        {
            {
                File keptFile = new File( "updated/file" );
                File keptAdded = new File( "new/file" );
                one( accurev ).info( basedir );
                will( returnValue( info ) );

                one( accurev ).update( with( basedir ), with( any( String.class ) ), with( any( List.class ) ) );
                will( doAll( addElementsTo( 2, keptFile, keptAdded ), returnValue( false ) ) );

            }
        } );

        AccuRevUpdateCommand command = new AccuRevUpdateCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.RUN_CHANGELOG_WITH_UPDATE, Boolean.toString( false ) );
        UpdateScmResult result = command.update( repo, testFileSet, commandParameters );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );

    }
}
