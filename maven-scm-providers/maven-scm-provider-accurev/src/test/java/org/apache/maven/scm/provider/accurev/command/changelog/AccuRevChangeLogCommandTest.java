package org.apache.maven.scm.provider.accurev.command.changelog;

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

import static org.apache.maven.scm.provider.accurev.AddElementsAction.addElementsTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.Transaction;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.jmock.Expectations;
import org.junit.Test;

public class AccuRevChangeLogCommandTest
    extends AbstractAccuRevCommandTest
{
    @Test
    public void testChangeLogBetweenStreamsUnsupported()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( new File( basedir, "project/dir" ) );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        AccuRevChangeLogCommand command = new AccuRevChangeLogCommand( getLogger() );

        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.START_SCM_VERSION, new ScmRevision( "aStream/12" ) );
        params.setScmVersion( CommandParameter.END_SCM_VERSION, new ScmRevision( "anotherStream/20" ) );

        try
        {
            command.changelog( repo, testFileSet, params );
            fail( "Expected accurev exception" );
        }
        catch ( ScmException e )
        {
            // Expected
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSimpleCase()
        throws Exception
    {
        // Workspace to root stream, keeps and promotes

        // Setup test data so that the checkin area is the repo's project path.
        final ScmFileSet testFileSet = new ScmFileSet( new File( basedir, "project/dir" ) );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        // getDate uses calendar's zero indexed months
        final Date dateFrom = getDate( 2009, 0, 1, 10, 0, 0, null );
        final Date dateTo = getDate( 2009, 0, 12, 13, 0, 0, null );
        final Date keepWhen = getDate( 2009, 0, 2, 9, 0, 0, null );
        final Date promoteWhen = getDate( 2009, 0, 4, 23, 0, 0, null );

        context.checking( new Expectations()
        {
            {

                Transaction keep = new Transaction( 10L, keepWhen, "keep", "aUser" );
                keep.addVersion( 5L, "/./kept/file", "10/5", "10/5", "3/2" );
                keep.setComment( "a Comment" );

                Transaction promote = new Transaction( 12L, promoteWhen, "promote", "anOther" );
                promote.addVersion( 10L, "/./promoted/file", "10/5", "4/2", null );
                promote.setComment( "my Promotion" );

                one( accurev ).history( with( "myStream" ), with( "2009/01/01 10:00:00" ),
                                        with( "2009/01/12 13:00:00" ), with( 0 ), with( any( List.class ) ) );
                will( doAll( addElementsTo( 4, keep, promote ), returnValue( true ) ) );
            }
        } );

        AccuRevChangeLogCommand command = new AccuRevChangeLogCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A commit message" );
        commandParameters.setDate( CommandParameter.START_DATE, dateFrom );
        commandParameters.setDate( CommandParameter.END_DATE, dateTo );
        ChangeLogScmResult result = command.changelog( repo, testFileSet, commandParameters );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getChangeLog().getChangeSets().size(), is( 2 ) );
        ChangeSet cs = (ChangeSet) result.getChangeLog().getChangeSets().get( 0 );
        assertThat( cs.getAuthor(), is( "aUser" ) );
        assertThat( cs.getComment(), is( "a Comment" ) );
        assertThat( cs.getDate(), is( keepWhen ) );
        assertThat( cs.getFiles().size(), is( 1 ) );
        ChangeFile cf = (ChangeFile) cs.getFiles().get( 0 );

        assertThat( cf.getName(), is( "/./kept/file" ) );
        assertThat( cf.getRevision(), is( "10/5 (10/5)" ) );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testChangeLogFailed()
        throws Exception
    {
        // Workspace to root stream, keeps and promotes

        // Setup test data so that the checkin area is the repo's project path.
        final ScmFileSet testFileSet = new ScmFileSet( new File( basedir, "project/dir" ) );

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        final Date dateFrom = getDate( 2009, 01, 01, 10, 00, 00, null );
        final Date dateTo = getDate( 2009, 01, 12, 13, 00, 00, null );

        context.checking( new Expectations()
        {
            {

                one( accurev ).history( with( "myStream" ), with( any( String.class ) ), with( any( String.class ) ),
                                        with( 0 ), with( any( List.class ) ) );
                will( returnValue( false ) );
            }
        } );

        AccuRevChangeLogCommand command = new AccuRevChangeLogCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A commit message" );
        commandParameters.setDate( CommandParameter.START_DATE, dateFrom );
        commandParameters.setDate( CommandParameter.END_DATE, dateTo );
        ChangeLogScmResult result = command.changelog( repo, testFileSet, commandParameters );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );
    }

}
