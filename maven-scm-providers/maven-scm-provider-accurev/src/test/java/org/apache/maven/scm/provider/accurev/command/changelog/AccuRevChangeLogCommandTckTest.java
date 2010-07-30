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

import static org.apache.maven.scm.ChangeSetMatcher.changeSet;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.accurev.cli.AccuRevCommandLine;
import org.apache.maven.scm.provider.accurev.cli.AccuRevJUnitUtil;
import org.apache.maven.scm.provider.accurev.command.AccuRevTckUtil;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.changelog.ChangeLogCommandTckTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith( JUnit4.class )
public class AccuRevChangeLogCommandTckTest
    extends ChangeLogCommandTckTest
{

    protected AccuRevTckUtil accurevTckTestUtil = new AccuRevTckUtil();

    @Override
    @Test
    public void testChangeLogCommand()
        throws Exception
    {
        super.testChangeLogCommand();
    }

    @SuppressWarnings( "unchecked" )
    @Test
    public void testUpstreamChangesIncludedInChangeLog()
        throws Exception
    {

        AccuRevCommandLine accurev = accurevTckTestUtil.getAccuRevCL();

        // UpdatingCopy is a workspace rooted at a substream
        String workingStream = accurevTckTestUtil.getWorkingStream();
        String subStream = accurevTckTestUtil.getDepotName() + "_sub_stream";
        accurev.mkstream( workingStream, subStream );

        ScmRepository mainRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( mainRepository );

        // Create a workspace at the updating copy location backed by the substream
        ScmBranch branch = new ScmBranch( "sub_stream" );
        provider.checkOut( mainRepository, new ScmFileSet( getUpdatingCopy() ), branch );

        Thread.sleep( 1000 );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        // Make a timestamp that we know are after initial revision but before the second
        Date timeBeforeUpstreamCheckin = new Date(); // Current time

        // pause a couple seconds... [SCM-244]
        Thread.sleep( 2000 );

        // Make a change to the readme.txt and commit the change
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );
        ScmTestCase.makeFile( getWorkingCopy(), "/src/test/java/Test.java", "changed Test.java" );
        CheckInScmResult checkInResult = provider.checkIn( mainRepository, fileSet, "upstream workspace promote" );
        assertTrue( "Unable to checkin changes to the repository", checkInResult.isSuccess() );

        Thread.sleep( 2000 );

        Date timeBeforeDownstreamCheckin = new Date();

        Thread.sleep( 2000 );

        ScmFileSet updateFileSet = new ScmFileSet( getUpdatingCopy() );
        provider.update( mainRepository, updateFileSet );
        ScmTestCase.makeFile( getUpdatingCopy(), "/pom.xml", "changed pom.xml" );
        ScmTestCase.makeFile( getUpdatingCopy(), "/src/test/java/Test.java", "changed again Test.java" );
        checkInResult = provider.checkIn( mainRepository, updateFileSet, "downstream workspace promote" );
        assertTrue( "Unable to checkin changes to the repository", checkInResult.isSuccess() );

        Thread.sleep( 2000 );

        Date timeBeforeDownstreamPromote = new Date();

        List<File> promotedFiles = new ArrayList<File>();
        accurev.promoteStream( subStream, "stream promote", promotedFiles );

        Thread.sleep( 2000 );

        Date timeEnd = new Date();

        // Changelog beforeUpstreamCheckin to end should contain both upstream and downstream changes. upstream change
        // should NOT include Test.java

        ChangeLogScmResult result =
            provider.changeLog( mainRepository, fileSet, timeBeforeUpstreamCheckin, timeEnd, 0, branch );
        assertTrue( "changelog beforeUpstreamCheckin to end", result.isSuccess() );

        List<ChangeSet> changeSets = result.getChangeLog().getChangeSets();
        assertThat( changeSets.size(), is( 2 ) );
        assertThat( changeSets, hasItems( changeSet( "Upstream changes", "/readme.txt" ),
                                          changeSet( "downstream workspace promote", "/./pom.xml",
                                                     "/./src/test/java/Test.java" ) ) );

        // Changelog beforeUpstreamCheckin to beforeDownstreamCheckin should include just upstream change including
        // Test.java
        result =
            provider.changeLog( mainRepository, fileSet, timeBeforeUpstreamCheckin, timeBeforeDownstreamCheckin, 0,
                                branch );
        assertTrue( "changelog beforeUpstreamCheckin to beforeDownstreamCheckin", result.isSuccess() );

        changeSets = result.getChangeLog().getChangeSets();
        assertThat( changeSets.size(), is( 1 ) );
        assertThat( changeSets.get( 0 ), changeSet( "Upstream changes", "/readme.txt", "/src/test/java/Test.java" ) );

        // Changelog beforeDownstreamCheckin to end should include just downstream change
        result = provider.changeLog( mainRepository, fileSet, timeBeforeDownstreamCheckin, timeEnd, 0, branch );
        assertTrue( "changelog beforeDownstreamCheckin to end", result.isSuccess() );

        changeSets = result.getChangeLog().getChangeSets();
        assertThat( changeSets.size(), is( 1 ) );
        assertThat( changeSets.get( 0 ), changeSet( "downstream workspace promote", "/./pom.xml",
                                                    "/./src/test/java/Test.java" ) );

        // Changelog beforeDownstreamPromote to end should be empty
        result = provider.changeLog( mainRepository, fileSet, timeBeforeDownstreamPromote, timeEnd, 0, branch );
        assertTrue( "changelog beforeDownstreamPromote to end", result.isSuccess() );

        changeSets = result.getChangeLog().getChangeSets();
        assertThat( changeSets.size(), is( 0 ) );

    }

    @Override
    @Before
    public void setUp()
        throws Exception
    {
        super.setUp();
    }

    @Override
    protected File getWorkingCopy()
    {
        return accurevTckTestUtil.getWorkingCopy();
    }

    @Override
    protected File getAssertionCopy()
    {
        return accurevTckTestUtil.getAssertionCopy();
    }

    @Override
    protected File getUpdatingCopy()
    {
        return accurevTckTestUtil.getUpdatingCopy();
    }

    @Override
    public String getScmUrl()
        throws Exception
    {
        return accurevTckTestUtil.getScmUrl();
    }

    @Override
    public void initRepo()
        throws Exception
    {
        accurevTckTestUtil.initRepo( getContainer() );

    }

    @Override
    @After
    public void tearDown()
        throws Exception
    {
        try
        {
            accurevTckTestUtil.tearDown();
            accurevTckTestUtil.removeWorkSpace( getWorkingCopy() );
            accurevTckTestUtil.removeWorkSpace( getAssertionCopy() );
        }
        finally
        {
            super.tearDown();
        }
    }

    @Override
    protected InputStream getCustomConfiguration()
        throws Exception

    {
        return AccuRevJUnitUtil.getPlexusConfiguration();
    }

}
