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
import static org.apache.maven.scm.provider.accurev.AddElementsAction.addElementsTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.jmock.Expectations;
import org.junit.Test;

public class AccuRevTagCommandTest
    extends AbstractAccuRevCommandTest
{

    @SuppressWarnings("unchecked")
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

        context.checking( new Expectations()
        {
            {
                one( accurev ).info( with( basedir ) );
                will( returnValue( info ) );
                inSequence( sequence );

                one( accurev ).mksnap( with( "theTagName" ), with( basisStream ) );
                will( returnValue( true ) );
                inSequence( sequence );

                one( accurev ).statTag( with( "theTagName" ), with( any( List.class ) ) );
                will( doAll( addElementsTo( 1, new File( "tagged/file" ) ), returnValue( true ) ) );

            }
        } );

        AccuRevTagCommand command = new AccuRevTagCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.TAG_NAME, "theTagName" );
        TagScmResult result = command.tag( repo, testFileSet, commandParameters );

        context.assertIsSatisfied();

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

        context.checking( new Expectations()
        {
            {
                one( accurev ).info( with( basedir ) );
                will( returnValue( info ) );
                inSequence( sequence );

                one( accurev ).mksnap( with( "theTagName" ), with( basisStream ) );
                will( returnValue( false ) );
                inSequence( sequence );

            }
        } );

        AccuRevTagCommand command = new AccuRevTagCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.TAG_NAME, "theTagName" );
        TagScmResult result = command.tag( repo, testFileSet, commandParameters );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );

    }

}
