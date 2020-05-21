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
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
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
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.apache.maven.scm.repository.ScmRepository;
import org.junit.Test;

public class AccuRevExportCommandTest
    extends AbstractAccuRevCommandTest
{

    @Test
    public void testExportToVersionPre490()
        throws Exception
    {
     // info defaults to no workspace...
        info.setWorkSpace( null );

        when( accurev.info( basedir ) ).thenReturn( info );
        
        // A version that does not support pop -t
        when( accurev.getClientVersion()).thenReturn( "4.7.4b" );

        List<File> poppedFiles = Collections.singletonList( new File( "exported/file" ) );
        when(
              accurev.popExternal( eq( basedir ), eq( "mySnapShot" ), eq( "now" ),
                                   (Collection<File>) argThat( hasItem( new File( "/./project/dir" ) ) ) ) ).thenReturn(
                                                                                                                         poppedFiles );

        AccuRevExportCommand command = new AccuRevExportCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot/676" ) );

        ExportScmResult result = command.export( repo, new ScmFileSet( basedir ), params );

        assertTrue( result.isSuccess() );
        assertHasScmFile( result.getExportedFiles(), "exported/file", ScmFileStatus.CHECKED_OUT );

    }
    
    @Test
    public void testExportToVersion490()
        throws Exception
    {
     // info defaults to no workspace...
        info.setWorkSpace( null );

        when( accurev.info( basedir ) ).thenReturn( info );
        
        // A version that does not support pop -t
        when( accurev.getClientVersion()).thenReturn( "4.9.0" );

        List<File> poppedFiles = Collections.singletonList( new File( "exported/file" ) );
        when(
              accurev.popExternal( eq( basedir ), eq( "mySnapShot" ), eq( "676" ),
                                   (Collection<File>) argThat( hasItem( new File( "/./project/dir" ) ) ) ) ).thenReturn(
                                                                                                                         poppedFiles );

        AccuRevExportCommand command = new AccuRevExportCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot/676" ) );

        ExportScmResult result = command.export( repo, new ScmFileSet( basedir ), params );

        assertTrue( result.isSuccess() );
        assertHasScmFile( result.getExportedFiles(), "exported/file", ScmFileStatus.CHECKED_OUT );
        verify( accurev).syncReplica();

    }
    @Test
    public void testExportVersionOutSideWorkspace()
        throws Exception
    {

        // info defaults to no workspace...
        info.setWorkSpace( null );

        when( accurev.info( basedir ) ).thenReturn( info );

        List<File> poppedFiles = Collections.singletonList( new File( "exported/file" ) );
        when(
              accurev.popExternal( eq( basedir ), eq( "mySnapShot" ), eq( (String) null ),
                                   (Collection<File>) argThat( hasItem( new File( "/./project/dir" ) ) ) ) ).thenReturn(
                                                                                                                         poppedFiles );

        AccuRevExportCommand command = new AccuRevExportCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot" ) );

        ExportScmResult result = command.export( repo, new ScmFileSet( basedir ), params );

        assertTrue( result.isSuccess() );
        assertHasScmFile( result.getExportedFiles(), "exported/file", ScmFileStatus.CHECKED_OUT );

    }

    @Test
    public void testExportFailure()
        throws Exception
    {

        // info defaults to no workspace...
        info.setWorkSpace( null );
        when( accurev.info( basedir ) ).thenReturn( info );
        when( accurev.getClientVersion()).thenReturn( "4.9.0" );
        when(
              accurev.popExternal( eq( basedir ), eq( "mySnapShot" ), eq("544"),
                                   (Collection<File>) argThat( hasItem( new File( "/./project/dir" ) ) ) ) ).thenReturn(
                                                                                                                         null );

        AccuRevExportCommand command = new AccuRevExportCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot/544"));

        ExportScmResult result = command.export( repo, new ScmFileSet( basedir ), params );

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );
    }

    @Test
    public void testNonPersistentWithinExistingWorkspace()
        throws Exception
    {

        // Setup info to return a stream rooted somewhere around here...
        info.setWorkSpace( "myStream_me" );
        info.setBasis( "someStream" );
        info.setTop( basedir.getParent() );

        when( accurev.info( basedir ) ).thenReturn( info );
        when( accurev.stat( basedir ) ).thenReturn( null );
        when( accurev.rmws( "myStream_me" ) ).thenReturn( Boolean.TRUE );
        List<File> poppedFiles = Collections.singletonList( new File( "exported/file" ) );
        when(
              accurev.popExternal( eq( basedir ), eq( "mySnapShot" ), eq( "now" ),
                                   (Collection<File>) argThat( hasItem( new File( "/./project/dir" ) ) ) ) ).thenReturn(
                                                                                                                         poppedFiles );
        when( accurev.reactivate( "myStream_me" ) ).thenReturn( Boolean.TRUE );

        repo.setPersistCheckout( true );

        AccuRevExportCommand command = new AccuRevExportCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot" ) );

        ExportScmResult result = command.export( repo, new ScmFileSet( basedir ), params );

        verify( accurev ).rmws( "myStream_me" );
        verify( accurev ).reactivate( "myStream_me" );
        assertTrue( result.isSuccess() );
        // TODO - raise JIRA to move relative path dir to repository rather than checkout result
        // dassertThat( result.getRelativePathProjectDirectory(), is( "/project/dir" ) );

    }

    @Test
    public void testNonPersistentCheckoutUsesExport()
        // This is same expectations as above, but using checkout method with setPersist = false.
        throws AccuRevException, ScmException
    {
        // Setup info to return a stream rooted somewhere around here...
        info.setWorkSpace( "myStream_me" );
        info.setBasis( "someStream" );
        info.setTop( basedir.getParent() );

        when( accurev.info( basedir ) ).thenReturn( info );
        when( accurev.stat( basedir ) ).thenReturn( null );
        when( accurev.rmws( "myStream_me" ) ).thenReturn( Boolean.TRUE );
        List<File> poppedFiles = Collections.singletonList( new File( "exported/file" ) );
        when(
              accurev.popExternal( eq( basedir ), eq( "mySnapShot" ), eq( "now" ),
                                   (Collection<File>) argThat( hasItem( new File( "/./project/dir" ) ) ) ) ).thenReturn(
                                                                                                                         poppedFiles );
        when( accurev.reactivate( "myStream_me" ) ).thenReturn( Boolean.TRUE );

        repo.setPersistCheckout( false );

        ScmRepository scmRepo = new ScmRepository( "accurev", repo );

        AccuRevScmProvider provider = new AccuRevScmProvider();
        CheckOutScmResult result = provider.checkOut( scmRepo, new ScmFileSet( basedir ), new ScmTag( "mySnapShot" ) );

        verify( accurev ).rmws( "myStream_me" );
        verify( accurev ).reactivate( "myStream_me" );

        assertTrue( result.isSuccess() );

    }

    @Test
    public void testname()
        throws Exception
    {
        String myString = "Hello " + null;
        assertThat( myString, is( "Hello null" ) );
    }
}
