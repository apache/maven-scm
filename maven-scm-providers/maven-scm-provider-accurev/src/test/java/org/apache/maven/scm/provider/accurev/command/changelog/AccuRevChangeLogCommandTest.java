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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isOneOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.apache.maven.scm.ChangeFileMatcher.changeFile;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
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
import org.apache.maven.scm.command.changelog.ChangeLogSet;
import org.apache.maven.scm.provider.accurev.FileDifference;
import org.apache.maven.scm.provider.accurev.Stream;
import org.apache.maven.scm.provider.accurev.Transaction;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.junit.Test;

public class AccuRevChangeLogCommandTest
    extends AbstractAccuRevCommandTest
{
    @Test
    public void testChangeLogBetweenStreamsUnsupported()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( new File( basedir, "project/dir" ) );

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

    @Test
    public void testChangeLogToNow()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( new File( basedir, "project/dir" ) );

        // start tran (35)
        List<Transaction> startTransaction = Collections.singletonList( new Transaction( 35L, new Date(), "sometran",
                                                                                         "anyone" ) );
        when( accurev.history( "aStream", "12", null, 1, true, true ) ).thenReturn( startTransaction );

        // end tran (42)
        List<Transaction> endTransaction = Collections.singletonList( new Transaction( 42L, new Date(), "sometran",
                                                                                       "anyone" ) );
        when( accurev.history( "aStream", "now", null, 1, true, true ) ).thenReturn( endTransaction );

        Stream basisStream = new Stream( "aStream", 10, "myDepot", 1, "myDepot", getDate( 2008, 1, 1 ), "normal" );
        when( accurev.showStream( "aStream" ) ).thenReturn( basisStream );

        List<FileDifference> emptyList = Collections.emptyList();
        when( accurev.diff( "myStream", "12", "42" ) ).thenReturn( emptyList );

        List<Transaction> noTransactions = Collections.emptyList();
        when( accurev.history( "aStream", "13", "42", 0, false, false ) ).thenReturn( noTransactions );

        AccuRevChangeLogCommand command = new AccuRevChangeLogCommand( getLogger() );
        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.START_SCM_VERSION, new ScmRevision( "aStream/12" ) );

        assertThat( command.changelog( repo, testFileSet, params ), not( nullValue() ) );

    }

    @Test
    public void testStandardCase()
        throws Exception
    {
        // Workspace to root stream, keeps and promotes

        // Setup test data so that the checkin area is the repo's project path.
        final ScmFileSet testFileSet = new ScmFileSet( new File( basedir, "project/dir" ) );

        // stream 1 only => not found
        // stream 2 only => created
        // stream 1 and 2 => version or moved
        // comment to contain all the changes eg file moved from to
        // files to contain all the referenced files
        // NOTE: The version specs, and stream numbers used in this test are
        // important!!

        // start tran (35)
        List<Transaction> startTransaction = Collections.singletonList( new Transaction( 35L, new Date(), "sometran",
                                                                                         "anyone" ) );
        when( accurev.history( "myStream", "2009/01/01 10:00:00", null, 1, true, true ) ).thenReturn( startTransaction );

        // end tran (42)
        List<Transaction> endTransaction = Collections.singletonList( new Transaction( 42L, new Date(), "sometran",
                                                                                       "anyone" ) );

        Stream basisStream = new Stream( "myStream", 10, "myDepot", 1, "myDepot", getDate( 2008, 1, 1 ), "normal" );
        when( accurev.showStream( "myStream" ) ).thenReturn( basisStream );

        when( accurev.history( "myStream", "2009/01/12 13:00:00", null, 1, true, true ) ).thenReturn( endTransaction );

        // now we call diff between the tran ids - 35 to 42
        FileDifference promoted = new FileDifference( 10L, "/promoted/file", "4/2", "/promoted/file", "6/1" );
        FileDifference removed = new FileDifference( 20L, null, null, "/removed/file", "6/1" );
        FileDifference created = new FileDifference( 30L, "/created/file", "6/1", null, null );
        FileDifference moved = new FileDifference( 40L, "/moved/to", "4/2", "/moved/from", "6/1" );

        when( accurev.diff( "myStream", "35", "42" ) ).thenReturn( Arrays.asList( promoted, removed, created, moved ) );

        // and we call hist for tranid + 1 to end trand id

        // getDate uses calendar's zero indexed months
        final Date dateFrom = getDate( 2009, 0, 1, 10, 0, 0, null );
        final Date dateTo = getDate( 2009, 0, 12, 13, 0, 0, null );
        final Date keepWhen = getDate( 2009, 0, 2, 9, 0, 0, null );
        final Date promoteWhen = getDate( 2009, 0, 4, 23, 0, 0, null );

        final Transaction promoteOne = new Transaction( 10L, keepWhen, "promote", "aUser" );
        promoteOne.addVersion( 5L, "/./kept/file", "10/5", "5/5", "3/2" );
        promoteOne.setComment( "a Comment" );

        final Transaction promoteTwo = new Transaction( 12L, promoteWhen, "promote", "anOther" );
        promoteTwo.addVersion( 10L, "/./promoted/file", "10/5", "4/2", null );
        promoteTwo.setComment( "my Promotion" );

        when( accurev.history( "myStream", "36", "42", 0, false, false ) ).thenReturn( Arrays.asList( promoteOne,
                                                                                                      promoteTwo ) );

        AccuRevChangeLogCommand command = new AccuRevChangeLogCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A commit message" );
        commandParameters.setDate( CommandParameter.START_DATE, dateFrom );
        commandParameters.setDate( CommandParameter.END_DATE, dateTo );
        ChangeLogScmResult result = command.changelog( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        List<ChangeSet> changeSets = result.getChangeLog().getChangeSets();
        assertThat( changeSets.size(), is( 3 ) );
        ChangeSet cs = (ChangeSet) changeSets.get( 0 );
        assertThat( cs.getAuthor(), is( "aUser" ) );
        assertThat( cs.getComment(), is( "a Comment" ) );
        assertThat( cs.getDate(), is( keepWhen ) );
        assertThat( cs.getFiles().size(), is( 1 ) );
        ChangeFile cf = (ChangeFile) cs.getFiles().get( 0 );

        assertThat( cf.getName(), is( "/./kept/file" ) );
        assertThat( cf.getRevision(), is( "10/5 (5/5)" ) );

        cs = (ChangeSet) changeSets.get( 2 );
        assertThat( cs.getAuthor(), is( "various" ) );
        // created/removed/moved but not the file that was in the promoted
        // set...
        assertThat( cs.getComment(), is( "Upstream changes" ) );
        assertThat( cs.getFiles().size(), is( 3 ) );
        assertThat( cs.containsFilename( "created/file" ), is( true ) );
    }

    @Test
    public void testWorkspaceChangelog()
        throws Exception
    {

        final ScmFileSet testFileSet = new ScmFileSet( new File( basedir, "project/dir" ) );
        final Date keepWhen = getDate( 2009, 0, 2, 9, 0, 0, null );
        final Date promoteWhen = getDate( 2009, 0, 4, 23, 0, 0, null );
        final Date fromDate = getDate( 2009, 0, 1, 0, 0, 0, null );
        final Date toDate = getDate( 2009, 0, 5, 1, 0, 0, null );

        // start tran (35)
        List<Transaction> startTransaction = Collections.singletonList( new Transaction( 35L, fromDate, "sometran",
                                                                                         "anyone" ) );
        when( accurev.history( "workspace5", "35", null, 1, true, true ) ).thenReturn( startTransaction );

        // end tran (42)
        List<Transaction> endTransaction = Collections.singletonList( new Transaction( 42L, toDate, "sometran",
                                                                                       "anyone" ) );

        when( accurev.history( "workspace5", "42", null, 1, true, true ) ).thenReturn( endTransaction );

        // Stream hierarchy
        // S2 < S4, S2 < WS3, S4 < WS5, S4 << WS7
        // 
        // Changelog(WS5,35,42) involves
        // -- diff(S4,35,42)
        // -- hist(WS5,36-42)
        // -- hist(S4,36-42)
        //
        // Promote S4 to S2 - not in diffS4, not in histS4, not in hist WS5 -
        // not in changeset
        // Promote WS3 to S2 - in diffS4, not hist histS4, not in hist WS5 - in
        // "upstream changes"
        // Promote WS5 to S4 - in diffS4, in histS4, not in hist WS5 - not in
        // changeset (real version from WS5)
        // Promote WS7 to S4 - in diffS4, in histS4, not in hist WS5 - in
        // changeset as a promote transaction
        // Keep WS5 - not in diffS4, not in histS4, in histWS5 - in changeset as
        // a keep transaction

        // This workspace is stream 5, attached to basis mystream 5
        Stream workspaceStream = new Stream( "workspace5", 5, "stream4", 4, "myDepot", getDate( 2008, 10, 1, 10, 0, 0,
                                                                                                null ), "workspace" );
        when( accurev.showStream( "workspace5" ) ).thenReturn( workspaceStream );

        Stream basisStream = new Stream( "stream4", 4, "myDepot", 1, "myDepot", getDate( 2008, 1, 1 ), "normal" );
        when( accurev.showStream( "stream4" ) ).thenReturn( basisStream );

        // now we call diff between the tran ids - 35 to 42
        FileDifference diffWS3toS2 = new FileDifference( 32L, "/promoted/WS3toS2", "3/2", "/promoted/WS3toS2", "6/1" );
        FileDifference diffWS5toS4 = new FileDifference( 54L, "/promoted/WS5toS4", "5/3", "/promoted/WS5toS4", "8/1" );
        FileDifference diffWS7toS4 = new FileDifference( 74L, "/promoted/WS7toS4", "7/12", "/promoted/WS7toS4", "3/13" );

        when( accurev.diff( "stream4", "35", "42" ) )
            .thenReturn( Arrays.asList( diffWS3toS2, diffWS5toS4, diffWS7toS4 ) );

        // and we call hist for tranid + 1 to end trand ii

        Transaction promoteWS5toS4 = new Transaction( 37L, promoteWhen, "promote", "aUser" );
        promoteWS5toS4.setComment( "WS5toS4" );
        promoteWS5toS4.addVersion( 54L, "/./promoted/WS5toS4", "4/5", "5/3", "3/2" );

        Transaction promoteWS7toS4 = new Transaction( 38L, promoteWhen, "promote", "aUser" );
        promoteWS7toS4.setComment( "WS7toS4" );
        promoteWS7toS4.addVersion( 74L, "/./promoted/WS7toS4", "4/11", "7/12", "3/2" );

        when( accurev.history( "stream4", "36", "42", 0, false, false ) ).thenReturn( Arrays.asList( promoteWS5toS4,
                                                                                                     promoteWS7toS4 ) );

        Transaction keepWS5 = new Transaction( 39L, keepWhen, "keep", "anOther" );
        keepWS5.addVersion( 5L, "/./kept/WS5", "5/7", "5/7", "7/21" );
        keepWS5.setComment( "keepWS5" );

        when( accurev.history( "workspace5", "36", "42", 0, false, false ) ).thenReturn( Collections
                                                                                             .singletonList( keepWS5 ) );

        AccuRevChangeLogCommand command = new AccuRevChangeLogCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setScmVersion( CommandParameter.START_SCM_VERSION, new ScmRevision( "workspace5/35" ) );
        commandParameters.setScmVersion( CommandParameter.END_SCM_VERSION, new ScmRevision( "workspace5/42" ) );
        ChangeLogScmResult result = command.changelog( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        ChangeLogSet changelog = result.getChangeLog();
        assertThat( changelog.getStartVersion().getName(), is( "workspace5/35" ) );
        assertThat( changelog.getEndVersion().getName(), is( "workspace5/42" ) );
        List<ChangeSet> changesets = changelog.getChangeSets();
        assertThat( changesets.size(), is( 3 ) );

        for ( ChangeSet changeSet : changesets )
        {
            assertThat( changeSet.getComment(), isOneOf( "Upstream changes", "WS7toS4", "keepWS5" ) );

            if ( "Upstream changes".equals( changeSet.getComment() ) )
            {
                assertThat( changeSet.getFiles().size(), is( 1 ) );
                ChangeFile changeFile = (ChangeFile) changeSet.getFiles().get( 0 );
                assertThat( changeFile, is( changeFile( "/promoted/WS3toS2" ) ) );
            }
        }
    }

    @Test
    public void testChangeLogFailed()
        throws Exception
    {
        // Workspace to root stream, keeps and promotes

        // Setup test data so that the checkin area is the repo's project path.
        final ScmFileSet testFileSet = new ScmFileSet( new File( basedir, "project/dir" ) );

        final Date dateFrom = getDate( 2009, 0, 01, 10, 00, 00, null );
        final Date dateTo = getDate( 2009, 0, 12, 13, 00, 00, null );

        // start tran (35)
        List<Transaction> startTransaction = Collections.singletonList( new Transaction( 35L, new Date(), "sometran",
                                                                                         "anyone" ) );
        when( accurev.history( "myStream", "2009/01/01 10:00:00", null, 1, true, true ) ).thenReturn( startTransaction );

        // end tran (42)
        List<Transaction> endTransaction = Collections.singletonList( new Transaction( 42L, new Date(), "sometran",
                                                                                       "anyone" ) );

        when( accurev.history( "myStream", "2009/01/12 13:00:00", null, 1, true, true ) ).thenReturn( endTransaction );

        when(
              accurev.history( eq( "myStream" ), any( String.class ), any( String.class ), eq( 0 ), eq( false ),
                               eq( false ) ) ).thenReturn( null );

        AccuRevChangeLogCommand command = new AccuRevChangeLogCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A commit message" );
        commandParameters.setDate( CommandParameter.START_DATE, dateFrom );
        commandParameters.setDate( CommandParameter.END_DATE, dateTo );
        ChangeLogScmResult result = command.changelog( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );
    }
}
