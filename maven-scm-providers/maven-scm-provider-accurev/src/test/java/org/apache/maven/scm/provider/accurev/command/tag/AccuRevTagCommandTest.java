package org.apache.maven.scm.provider.accurev.command.tag;

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
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.junit.Test;

public class AccuRevTagCommandTest
    extends AbstractAccuRevCommandTest
{

    @Test
    public void testTag()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( new File( "/my/workspace/project/dir" ) );
        final File basedir = testFileSet.getBasedir();

        final String basisStream = "basisStream";
        final AccuRevInfo info = new AccuRevInfo( basedir );
        info.setBasis( basisStream );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        when( accurev.info( basedir )).thenReturn(info);
        
        when( accurev.mksnap( "theTagName", basisStream ) ).thenReturn( Boolean.TRUE );

        List<File> taggedFiles = Collections.singletonList( new File( "tagged/file" ) );
        when( accurev.statTag( "theTagName" ) ).thenReturn( taggedFiles );

        AccuRevTagCommand command = new AccuRevTagCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.TAG_NAME, "theTagName" );
        TagScmResult result = command.tag( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getTaggedFiles().size(), is( 1 ) );
        assertHasScmFile( result.getTaggedFiles(), "tagged/file", ScmFileStatus.TAGGED );

    }

    @Test
    public void testAccuRevError()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( new File( "/my/workspace/project/dir" ) );
        final File basedir = testFileSet.getBasedir();

        final String basisStream = "basisStream";
        final AccuRevInfo info = new AccuRevInfo( basedir );
        info.setBasis( basisStream );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );
        when( accurev.info( basedir )).thenReturn(info);

        when( accurev.mksnap( "theTagName", basisStream ) ).thenReturn( Boolean.FALSE );
        AccuRevTagCommand command = new AccuRevTagCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.TAG_NAME, "theTagName" );
        TagScmResult result = command.tag( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );

    }

}
