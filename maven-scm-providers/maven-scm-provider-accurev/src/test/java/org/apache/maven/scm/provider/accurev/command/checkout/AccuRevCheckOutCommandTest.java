package org.apache.maven.scm.provider.accurev.command.checkout;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.junit.Test;

/**
 * checkout a revision or branch (stream/tranid)-> make workspace. If basedir is empty and represents the top of an
 * existing workspace, then reparent the workspace if necessary and repopulate missing file, and update If basedir is
 * not empty or is a subdirectory of an existing workspace throw exception. Otherwise make a workspace and update
 * Special case for release plugin - checkout a tag to an ignored and empty subdirectory of an existing workspace. Treat
 * as an export. deactivate the workspace, export with pop -v -L then reactivate the workspace.
 * 
 * @author ggardner
 */
public class AccuRevCheckOutCommandTest
    extends AbstractAccuRevCommandTest
{

    @Test
    public void testCheckout()
        throws Exception
    {

        when( accurev.mkws( "myStream", AccuRevCheckOutCommand.getWorkSpaceName( basedir, "myStream" ), basedir ) ).thenReturn(
                                                                                                                                true );

        List<File> updatedFiles = Collections.singletonList( new File( "updated/file" ) );
        when( accurev.update( basedir, "now" ) ).thenReturn( updatedFiles );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CheckOutScmResult result = command.checkout( repo, new ScmFileSet( basedir ), new CommandParameters() );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getRelativePathProjectDirectory(), is( "/project/dir" ) );
        List<ScmFile> checkedOutFiles = result.getCheckedOutFiles();
        assertThat( checkedOutFiles.size(), is( 1 ) );
        assertHasScmFile( checkedOutFiles, "updated/file", ScmFileStatus.CHECKED_OUT );

    }

    @Test
    public void testCheckoutFailure()
        throws Exception
    {

        when( accurev.mkws( "myStream", AccuRevCheckOutCommand.getWorkSpaceName( basedir, "myStream" ), basedir ) ).thenReturn(
                                                                                                                                true );
        when( accurev.update( basedir, "now" ) ).thenReturn( null );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CheckOutScmResult result = command.checkout( repo, new ScmFileSet( basedir ), new CommandParameters() );

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );

    }

    @Test
    public void testReCheckoutExistingWorkspaceSameBasis()
        throws Exception
    {

        // Set the info result to return a workspace that already exists
        info.setWorkSpace( "someOldStream_someUser" );
        info.setBasis( "myStream" );
        info.setTop( basedir.getAbsolutePath() );

        List<File> emptyList = Collections.emptyList();

        when( accurev.pop( basedir, null ) ).thenReturn( emptyList );

        List<File> updatedFiles = Collections.singletonList( new File( "updated/file" ) );
        when( accurev.update( basedir, null ) ).thenReturn( updatedFiles );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CheckOutScmResult result = command.checkout( repo, new ScmFileSet( basedir ), new CommandParameters() );

        verify( accurev ).pop( basedir, null );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getRelativePathProjectDirectory(), is( "/project/dir" ) );

    }

    @Test
    public void testReCheckoutExistingWorkspaceDifferentBasis()
        throws Exception
    {
        // Set the info result to return a workspace that already exists
        info.setWorkSpace( "someOldStream_someUser" );
        info.setBasis( "myStream" );
        info.setTop( basedir.getAbsolutePath() );

        when( accurev.chws( basedir, "someOldStream_someUser", "mySnapShot" ) ).thenReturn( true );

        List<File> emptyPop = Collections.emptyList();
        when( accurev.popExternal( basedir, null, null, null ) ).thenReturn( emptyPop );

        List<File> updatedFiles = Collections.singletonList( new File( "updated/file" ) );
        when( accurev.update( basedir, null ) ).thenReturn( updatedFiles );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot" ) );

        CheckOutScmResult result = command.checkout( repo, new ScmFileSet( basedir ), params );

        verify( accurev ).chws( basedir, "someOldStream_someUser", "mySnapShot" );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getRelativePathProjectDirectory(), is( "/project/dir" ) );

    }

    @Test( expected = ScmException.class )
    public void testReCheckoutSubdirectoryOfExistingWorkspaceThrowsException()
        throws Exception
    {
        // Set the info result to return a workspace that already exists
        info.setWorkSpace( "someOldStream_someUser" );
        info.setBasis( "myStream" );
        info.setTop( basedir.getParentFile().getAbsolutePath() );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot" ) );

        command.checkout( repo, new ScmFileSet( basedir ), params );
        fail( "Expected exception" );

    }

    @Test
    public void testCheckoutToVersionNewWorkspace()
        throws Exception
    {

        when( accurev.mkws( "anotherStream", AccuRevCheckOutCommand.getWorkSpaceName( basedir, "anotherStream" ), basedir ) ).thenReturn(
                                                                                                                                true );

        List<File> updatedFiles = Collections.singletonList( new File( "updated/file" ) );
        when( accurev.update( basedir, "now" ) ).thenReturn( updatedFiles );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CommandParameters parameters = new CommandParameters();
        parameters.setScmVersion( CommandParameter.SCM_VERSION, new ScmRevision( "anotherStream/12" ) );
        
        CheckOutScmResult result = command.checkout( repo, new ScmFileSet( basedir ), parameters );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getCheckedOutFiles().size(), is( 1 ) );

    }
    
    @Test
    public void testCheckoutToVersionExistingWorkspace()
        throws Exception
    {

        // Set the info result to return a workspace that already exists
        info.setWorkSpace( "someOldStream_someUser" );
        info.setBasis( "myStream" );
        info.setTop( basedir.getAbsolutePath() );

        List<File> emptyList = Collections.emptyList();

        when( accurev.pop( basedir, null ) ).thenReturn( emptyList );

        List<File> updatedFiles = Collections.singletonList( new File( "updated/file" ) );
        when( accurev.update( basedir, "12" ) ).thenReturn( updatedFiles );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CommandParameters parameters = new CommandParameters();
        parameters.setScmVersion( CommandParameter.SCM_VERSION, new ScmRevision( "myStream/12" ) );
        CheckOutScmResult result = command.checkout( repo, new ScmFileSet( basedir ), parameters );

        verify( accurev ).pop( basedir, null );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getCheckedOutFiles().size(), is( 1 ) );

    }

}
