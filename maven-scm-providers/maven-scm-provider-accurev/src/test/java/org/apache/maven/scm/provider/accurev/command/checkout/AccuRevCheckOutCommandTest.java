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
import static org.apache.maven.scm.provider.accurev.AddElementsAction.addElementsTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.jmock.Expectations;
import org.junit.Before;
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

    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();
        context.checking( new Expectations()
        {
            {

                one( accurev ).info( basedir );
                will( returnValue( info ) );
                inSequence( sequence );

            }
        } );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCheckout()
        throws Exception
    {

        context.checking( new Expectations()
        {
            {
                one( accurev ).mkws( "myStream", AccuRevCheckOutCommand.getWorkSpaceName( basedir, "myStream" ),
                                     basedir );
                will( returnValue( true ) );
                inSequence( sequence );

                one( accurev ).update( with( basedir ), with( (String) null ), with( any( List.class ) ) );
                will( doAll( addElementsTo( 2, new File( "updated/file" ) ), returnValue( true ) ) );
                // will(returnValue(true));
                inSequence( sequence );
            }
        } );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CheckOutScmResult result = command.checkout( repo, new ScmFileSet( basedir ), new CommandParameters() );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getRelativePathProjectDirectory(), is( "/project/dir" ) );
        List checkedOutFiles = result.getCheckedOutFiles();
        assertThat( checkedOutFiles.size(), is( 1 ) );
        assertHasScmFile( checkedOutFiles, "updated/file", ScmFileStatus.CHECKED_OUT );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCheckoutFailure()
        throws Exception
    {

        context.checking( new Expectations()
        {
            {
                one( accurev ).mkws( "myStream", AccuRevCheckOutCommand.getWorkSpaceName( basedir, "myStream" ),
                                     basedir );
                will( returnValue( true ) );
                inSequence( sequence );

                one( accurev ).update( with( basedir ), with( (String) null ), with( any( List.class ) ) );
                will( returnValue( false ) );
                // will(returnValue(true));
                inSequence( sequence );
            }
        } );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CheckOutScmResult result = command.checkout( repo, new ScmFileSet( basedir ), new CommandParameters() );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReCheckoutExistingWorkspaceSameBasis()
        throws Exception
    {

        // Set the info result to return a workspace that already exists
        info.setWorkSpace( "someOldStream_someUser" );
        info.setBasis( "myStream" );
        info.setTop( basedir.getAbsolutePath() );

        context.checking( new Expectations()
        {
            {
                one( accurev ).pop( with( basedir ), with( (List) null ), with( any( List.class ) ) );
                will( returnValue( true ) );
                inSequence( sequence );

                one( accurev ).update( with( basedir ), with( (String) null ), with( any( List.class ) ) );
                will( returnValue( true ) );
                inSequence( sequence );
            }
        } );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CheckOutScmResult result = command.checkout( repo, new ScmFileSet( basedir ), new CommandParameters() );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getRelativePathProjectDirectory(), is( "/project/dir" ) );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testReCheckoutExistingWorkspaceDifferentBasis()
        throws Exception
    {
        // Set the info result to return a workspace that already exists
        info.setWorkSpace( "someOldStream_someUser" );
        info.setBasis( "myStream" );
        info.setTop( basedir.getAbsolutePath() );

        context.checking( new Expectations()
        {
            {
                one( accurev ).chws( with( basedir ), with( "someOldStream_someUser" ), with( "mySnapShot" ) );
                will( returnValue( true ) );
                inSequence( sequence );

                one( accurev ).pop( with( basedir ), with( (List) null ), with( any( List.class ) ) );
                will( returnValue( true ) );
                inSequence( sequence );

                one( accurev ).update( with( basedir ), with( (String) null ), with( any( List.class ) ) );
                will( returnValue( true ) );
                inSequence( sequence );
            }
        } );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot" ) );

        CheckOutScmResult result = command.checkout( repo, new ScmFileSet( basedir ), params );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getRelativePathProjectDirectory(), is( "/project/dir" ) );

    }

    @Test
    public void testReCheckoutSubdirectoryOfExistingWorkspaceThrowsException()
        throws Exception
    {
        // Set the info result to return a workspace that already exists
        info.setWorkSpace( "someOldStream_someUser" );
        info.setBasis( "myStream" );
        info.setTop( basedir.getParentFile().getAbsolutePath() );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        AccuRevCheckOutCommand command = new AccuRevCheckOutCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, new ScmTag( "mySnapShot" ) );

        try
        {
            command.checkout( repo, new ScmFileSet( basedir ), params );
            fail( "Expected exception" );
        }
        catch ( ScmException e )
        {
            context.assertIsSatisfied();
        }

    }

}
