package org.apache.maven.scm.provider.accurev.command.export;

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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevScmProvider;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.apache.maven.scm.repository.ScmRepository;
import org.jmock.Expectations;
import org.junit.Test;

public class AccuRevExportCommandTest
    extends AbstractAccuRevCommandTest
{

    @SuppressWarnings("unchecked")
    @Test
    public void testExportVersionOutSideWorkspace()
        throws Exception
    {

        // info defaults to no workspace...
        info.setWorkSpace( null );

        context.checking( new Expectations()
        {
            {

                one( accurev ).info( with( basedir ) );
                will( returnValue( info ) );

                one( accurev ).pop( with( basedir ), with( "mySnapShot" ),
                                    (Collection<File>) with( hasItem( new File( "/./project/dir" ) ) ),
                                    with( any( List.class ) ) );
                will( doAll( addElementsTo( 3, new File( "exported/file" ) ), returnValue( true ) ) );

            }
        } );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        AccuRevExportCommand command = new AccuRevExportCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot" ) );

        ExportScmResult result = command.export( repo, new ScmFileSet( basedir ), params );

        context.assertIsSatisfied();

        assertTrue( result.isSuccess() );
        assertHasScmFile( result.getExportedFiles(), "exported/file", ScmFileStatus.CHECKED_OUT );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testExportFailure()
        throws Exception
    {

        // info defaults to no workspace...
        info.setWorkSpace( null );

        context.checking( new Expectations()
        {
            {

                one( accurev ).info( with( basedir ) );
                will( returnValue( info ) );

                one( accurev ).pop( with( basedir ), with( "mySnapShot" ),
                                    (Collection<File>) with( hasItem( new File( "/./project/dir" ) ) ),
                                    with( any( List.class ) ) );
                will( returnValue( false ) );

            }
        } );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        AccuRevExportCommand command = new AccuRevExportCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot" ) );

        ExportScmResult result = command.export( repo, new ScmFileSet( basedir ), params );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNonPersistentWithinExistingWorkspace()
        throws Exception
    {

        // Setup info to return a stream rooted somewhere around here...
        info.setWorkSpace( "myStream_me" );
        info.setBasis( "someStream" );
        info.setTop( basedir.getParent() );

        context.checking( new Expectations()
        {
            {

                one( accurev ).info( with( basedir ) );
                will( returnValue( info ) );

                one( accurev ).stat( basedir );
                will( returnValue( null ) );
                inSequence( sequence );

                one( accurev ).rmws( "myStream_me" );
                will( returnValue( true ) );
                inSequence( sequence );

                one( accurev ).pop( with( basedir ), with( "mySnapShot" ),
                                    (Collection<File>) with( hasItem( new File( "/./project/dir" ) ) ),
                                    with( any( List.class ) ) );
                will( returnValue( true ) );
                inSequence( sequence );

                one( accurev ).reactivate( "myStream_me" );
                will( returnValue( true ) );
                inSequence( sequence );

            }
        } );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );
        repo.setPersistCheckout( true );

        AccuRevExportCommand command = new AccuRevExportCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot" ) );

        ExportScmResult result = command.export( repo, new ScmFileSet( basedir ), params );

        context.assertIsSatisfied();

        assertTrue( result.isSuccess() );
        // TODO - raise JIRA to move relative path dir to repository rather than checkout result
        // dassertThat( result.getRelativePathProjectDirectory(), is( "/project/dir" ) );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testNonPersistentCheckoutUsesExport()
        // This is same expectations as above, but using checkout method with setPersist = false.
        throws AccuRevException, ScmException
    {
        // Setup info to return a stream rooted somewhere around here...
        info.setWorkSpace( "myStream_me" );
        info.setBasis( "someStream" );
        info.setTop( basedir.getParent() );

        context.checking( new Expectations()
        {
            {

                one( accurev ).info( with( basedir ) );
                will( returnValue( info ) );

                one( accurev ).stat( basedir );
                will( returnValue( null ) );
                inSequence( sequence );

                one( accurev ).rmws( "myStream_me" );
                will( returnValue( true ) );
                inSequence( sequence );

                one( accurev ).pop( with( basedir ), with( "mySnapShot" ),
                                    (Collection<File>) with( hasItem( new File( "/./project/dir" ) ) ),
                                    with( any( List.class ) ) );
                will( returnValue( true ) );
                inSequence( sequence );

                one( accurev ).reactivate( "myStream_me" );
                will( returnValue( true ) );
                inSequence( sequence );

            }
        } );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );
        repo.setPersistCheckout( false );

        ScmRepository scmRepo = new ScmRepository( "accurev", repo );

        AccuRevScmProvider provider = new AccuRevScmProvider();
        CheckOutScmResult result = provider.checkOut( scmRepo, new ScmFileSet( basedir ), new ScmTag( "mySnapShot" ) );

        context.assertIsSatisfied();

        assertTrue( result.isSuccess() );

    }
}
