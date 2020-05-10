package org.apache.maven.scm.provider.accurev.command.login;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.junit.Test;

public class AccuRevLoginCommandTest
    extends AbstractAccuRevCommandTest
{

    @Test
    public void testWhenNotLoggedIn()
        throws Exception
    {

        repo.setUser( "myUser" );
        repo.setPassword( "aPassword" );
        info.setUser( "(not logged in)" );
        when( accurev.info( any( File.class ) ) ).thenReturn( info );
        when( accurev.login( "myUser", "aPassword" ) ).thenReturn( true );
        AccuRevLoginCommand command = new AccuRevLoginCommand( getLogger() );

        LoginScmResult result = command.login( repo, new ScmFileSet( basedir ), new CommandParameters() );

        assertThat( result.isSuccess(), is( true ) );
        verify( accurev ).login( "myUser", "aPassword" );

    }

    @Test
    public void testWhenAlreadyLoggedInAsSomeoneElse()
        throws Exception
    {
        repo.setUser( "myUser" );
        repo.setPassword( "aPassword" );
        info.setUser( "A.N.Other" );
        when( accurev.info( any( File.class ) ) ).thenReturn( info );
        when( accurev.login( "myUser", "aPassword" ) ).thenReturn( true );
        AccuRevLoginCommand command = new AccuRevLoginCommand( getLogger() );

        LoginScmResult result = command.login( repo, new ScmFileSet( basedir ), new CommandParameters() );

        assertThat( result.isSuccess(), is( true ) );
        verify( accurev ).login( "myUser", "aPassword" );

    }

    @Test
    public void testWhenAlreadyLoggedInAsRequiredUser()
        throws Exception
    {

        repo.setUser( "myUser" );
        repo.setPassword( "aPassword" );
        info.setUser( "myUser" );
        when( accurev.info( any( File.class ) ) ).thenReturn( info );
        AccuRevLoginCommand command = new AccuRevLoginCommand( getLogger() );

        LoginScmResult result = command.login( repo, new ScmFileSet( basedir ), new CommandParameters() );

        assertThat( result.isSuccess(), is( true ) );
        // This is an important case as logging in will start an expiry timer
        // that might be shorter than the current expiry timer!
        verify( accurev, never() ).login( eq( "myUser" ), anyString() );

    }

    @Test
    public void testWhenNoUserSuppliedAndAlreadyLoggedIn()
        throws Exception
    {

        repo.setUser( null );
        info.setUser( "anyUser" );
        when( accurev.info( any( File.class ) ) ).thenReturn( info );
        AccuRevLoginCommand command = new AccuRevLoginCommand( getLogger() );

        LoginScmResult result = command.login( repo, new ScmFileSet( basedir ), new CommandParameters() );

        assertThat( result.isSuccess(), is( true ) );
        verify( accurev, never() ).login( anyString(), anyString() );

    }

    @Test
    public void testFailsWhenNoUserSuppliedAndNotLoggedIn()
        throws Exception
    {

        repo.setUser( null );
        info.setUser( "(not logged in)" );
        when( accurev.info( any( File.class ) ) ).thenReturn( info );
        AccuRevLoginCommand command = new AccuRevLoginCommand( getLogger() );

        LoginScmResult result = command.login( repo, new ScmFileSet( basedir ), new CommandParameters() );

        assertThat( result.isSuccess(), is( false ) );
        verify( accurev, never() ).login( anyString(), anyString() );

    }
}
