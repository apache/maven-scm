package org.apache.maven.scm.provider.git.command.changelog;

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

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.changelog.ChangeLogCommandTckTest;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public abstract class GitChangeLogCommandTckTest
    extends ChangeLogCommandTckTest
{
    public static final long SLEEP_TIME_IN_MILLIS = 250L;

    /** {@inheritDoc} */
    public void initRepo()
        throws Exception
    {
        GitScmTestUtils.initRepo( "src/test/resources/linear-changelog/", getRepositoryRoot(), getWorkingCopy() );
    }

    @Override
    protected CheckOutScmResult checkOut( File workingDirectory, ScmRepository repository ) throws Exception
    {
        try
        {
            return super.checkOut( workingDirectory, repository );
        }
        finally
        {
            GitScmTestUtils.setDefaultUser( workingDirectory );
        }
    }

    @Test
    public void testChangeLogCommandFromHeadAncestorAndHead()
        throws Exception
    {
        Thread.sleep( SLEEP_TIME_IN_MILLIS );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        String startVersion = "HEAD~1";
        clr.setStartRevision( new ScmRevision( startVersion ) );
        String endVersion = "HEAD";
        clr.setEndRevision( new ScmRevision( endVersion ) );
        ChangeLogScmResult changelogResult = provider.changeLog( clr );

        List<ChangeSet> logEntries = changelogResult.getChangeLog().getChangeSets();
        assertEquals( String.format( "changelog for %s..%s returned bad number of commits", startVersion, endVersion ),
                1, logEntries.size() );


        assertThat( "bad head commit SHA1 retrieved", logEntries.get( 0 ).getRevision(), startsWith( "464921b" ) );
    }

    @Test
    public void testChangeLogCommandFromHeadToHead()
            throws Exception
    {
        Thread.sleep( SLEEP_TIME_IN_MILLIS );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        String startVersion = "HEAD";
        clr.setStartRevision( new ScmRevision( startVersion ) );
        String endVersion = "HEAD";
        clr.setEndRevision( new ScmRevision( endVersion ) );
        ChangeLogScmResult changelogResult = provider.changeLog( clr );

        List<ChangeSet> logEntries = changelogResult.getChangeLog().getChangeSets();
        assertEquals( String.format( "changelog for %s..%s returned bad number of commits", startVersion, endVersion ),
                0, logEntries.size() );
    }

    @Test
    public void testChangeLogCommandFromUndefinedToHead()
            throws Exception
    {
        Thread.sleep( SLEEP_TIME_IN_MILLIS );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        String endVersion = "HEAD";
        clr.setEndRevision( new ScmRevision( endVersion ) );
        ChangeLogScmResult changelogResult = provider.changeLog( clr );

        List<ChangeSet> logEntries = changelogResult.getChangeLog().getChangeSets();
        assertEquals( String.format( "changelog for ..%s returned bad number of commits", endVersion ),
                0, logEntries.size() );
    }

    @Test
    public void testChangeLogCommandFromVersionToUndefined()
            throws Exception
    {
        Thread.sleep( SLEEP_TIME_IN_MILLIS );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        String startVersion = "e3864d9";
        clr.setStartRevision( new ScmRevision( startVersion ) );
        ChangeLogScmResult changelogResult = provider.changeLog( clr );

        List<ChangeSet> logEntries = changelogResult.getChangeLog().getChangeSets();
        assertEquals( String.format( "changelog for %s.. returned bad number of commits", startVersion ),
                2, logEntries.size() );

        assertThat( "bad commit SHA1 retrieved", logEntries.get( 0 ).getRevision(), startsWith( "464921b" ) );
        assertThat( "bad commit SHA1 retrieved", logEntries.get( 1 ).getRevision(), startsWith( "db46d63" ) );
    }

    @Test
    public void testChangeLogCommandFromVoneToVtwo()
            throws Exception
    {
        Thread.sleep( SLEEP_TIME_IN_MILLIS );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        String startVersion = "0f1e817";
        clr.setStartRevision( new ScmRevision( startVersion ) );
        String endVersion = "db46d63";
        clr.setEndRevision( new ScmRevision( endVersion ) );
        ChangeLogScmResult changelogResult = provider.changeLog( clr );

        List<ChangeSet> logEntries = changelogResult.getChangeLog().getChangeSets();
        assertEquals( String.format( "changelog for %s.. returned bad number of commits", startVersion ),
                2, logEntries.size() );

        assertThat( "bad commit SHA1 retrieved", logEntries.get( 0 ).getRevision(), startsWith( "db46d63" ) );
        assertThat( "bad commit SHA1 retrieved", logEntries.get( 1 ).getRevision(), startsWith( "e3864d9" ) );
    }

    @Test
    public void testChangeLogCommandWithStartEndInBadOrder()
            throws Exception
    {
        Thread.sleep( SLEEP_TIME_IN_MILLIS );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        String startVersion = "db46d63";
        clr.setStartRevision( new ScmRevision( startVersion ) );
        String endVersion = "0f1e817";
        clr.setEndRevision( new ScmRevision( endVersion ) );
        ChangeLogScmResult changelogResult = provider.changeLog( clr );

        List<ChangeSet> logEntries = changelogResult.getChangeLog().getChangeSets();
        assertEquals( String.format( "changelog for %s..%s should return no commits", startVersion, endVersion ),
                0, logEntries.size() );
    }

    @Test
    public void testChangeLogCommandFromHeadToStartOfRepository()
            throws Exception
    {
        Thread.sleep( SLEEP_TIME_IN_MILLIS );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        String version = "HEAD";
        clr.setRevision( new ScmRevision( version ) );
        ChangeLogScmResult changelogResult = provider.changeLog( clr );

        List<ChangeSet> logEntries = changelogResult.getChangeLog().getChangeSets();
        assertEquals( String.format( "changelog for %s returned bad number of commits", version ),
                5, logEntries.size() );
    }

    @Test
    public void testChangeLogCommandFromVersionToStartOfRepository()
            throws Exception
    {
        Thread.sleep( SLEEP_TIME_IN_MILLIS );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        String version = "db46d63";
        clr.setRevision( new ScmRevision( version ) );
        ChangeLogScmResult changelogResult = provider.changeLog( clr );

        List<ChangeSet> logEntries = changelogResult.getChangeLog().getChangeSets();
        assertEquals( String.format( "changelog for %s returned bad number of commits", version ),
                4, logEntries.size() );

        assertThat( "bad commit SHA1 retrieved", logEntries.get( 0 ).getRevision(), startsWith( "db46d63" ) );
        assertThat( "bad commit SHA1 retrieved", logEntries.get( 1 ).getRevision(), startsWith( "e3864d9" ) );
        assertThat( "bad commit SHA1 retrieved", logEntries.get( 2 ).getRevision(), startsWith( "0f1e817" ) );
        assertThat( "bad commit SHA1 retrieved", logEntries.get( 3 ).getRevision(), startsWith( "e75cb5a" ) );

        List<String> tags4 = Arrays.asList( "Tag4a", "Tag4b" );
        List<String> tags2 = Collections.singletonList( "Tag2" );
        List<String> noTags = Collections.emptyList();

        assertEquals( "Incorrect tags found", tags4,  sorted( logEntries.get( 0 ).getTags() ) );
        assertEquals( "Incorrect tags found", noTags, sorted( logEntries.get( 1 ).getTags() ) );
        assertEquals( "Incorrect tags found", tags2,  sorted( logEntries.get( 2 ).getTags() ) );
        assertEquals( "Incorrect tags found", noTags, sorted( logEntries.get( 3 ).getTags() ) );
    }

    private List<String> sorted( List<String> input )
    {
        List<String> result = new ArrayList<>( input );
        Collections.sort( result );
        return result;
    }

}
