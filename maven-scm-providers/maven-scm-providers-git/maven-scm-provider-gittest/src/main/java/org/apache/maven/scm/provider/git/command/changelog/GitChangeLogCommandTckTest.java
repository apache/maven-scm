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

import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.provider.git.GitScmTestUtils;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.tck.command.changelog.ChangeLogCommandTckTest;

import java.io.File;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 */
public abstract class GitChangeLogCommandTckTest
    extends ChangeLogCommandTckTest
{

    /** {@inheritDoc} */
    public void initRepo()
        throws Exception
    {
        GitScmTestUtils.initRepo( "src/test/resources/repository/", getRepositoryRoot(), getWorkingDirectory() );
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

    public void testChangeLogCommandBetweenHeadAndPreviousCommit()
            throws Exception
    {
        Thread.sleep( 1000 );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        // We set only endRevision to HEAD
        clr.setStartRevision( new ScmRevision( "HEAD~1" ) );
        clr.setEndRevision( new ScmRevision( "HEAD" ) );

        //Make a change to the readme.txt and commit the change
        this.edit( getWorkingCopy(), "readme.txt", null, getScmRepository() );
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );
        CheckInScmResult checkInResult = provider.checkIn( getScmRepository(), fileSet, "dummy commit message" );
        assertTrue( "Unable to checkin changes to the repository", checkInResult.isSuccess() );

        ChangeLogScmResult result = provider.changeLog( clr );
        assertTrue( result.getProviderMessage(), result.isSuccess() );
        assertEquals(
                "the changelog from root does not have good commits number",
                1, result.getChangeLog().getChangeSets().size()
        );
    }

    public void testChangeIsEmptyForVersionHeadToHead()
            throws Exception
    {
        Thread.sleep( 1000 );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        // We set only endRevision to HEAD
        clr.setStartRevision( new ScmRevision( "HEAD" ) );
        clr.setEndRevision( new ScmRevision( "HEAD" ) );

        //Make a change to the readme.txt and commit the change
        this.edit( getWorkingCopy(), "readme.txt", null, getScmRepository() );
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );
        CheckInScmResult checkInResult = provider.checkIn( getScmRepository(), fileSet, "dummy commit message" );
        assertTrue( "Unable to checkin changes to the repository", checkInResult.isSuccess() );

        ChangeLogScmResult result = provider.changeLog( clr );
        assertTrue( result.getProviderMessage(), result.isSuccess() );
        assertEquals(
                "the changelog from root does not have good commits number",
                0, result.getChangeLog().getChangeSets().size()
        );
    }


    public void testChangeLogCommandWithOnlyEndVersion()
            throws Exception
    {
        Thread.sleep( 1000 );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );

        // We set only endRevision to HEAD
        clr.setEndRevision( new ScmRevision( "HEAD" ) );
        ChangeLogScmResult firstResult = provider.changeLog( clr );

        int firstLogSize = firstResult.getChangeLog().getChangeSets().size();
        assertEquals( "Unexpected initial log size", 0, firstLogSize );

        Thread.sleep( 2000 );
        //Make a change to the readme.txt and commit the change
        this.edit( getWorkingCopy(), "readme.txt", null, getScmRepository() );
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );
        CheckInScmResult checkInResult = provider.checkIn( getScmRepository(), fileSet, "dummy commit message" );
        assertTrue( "Unable to checkin changes to the repository", checkInResult.isSuccess() );

        ChangeLogScmResult secondResult = provider.changeLog( clr );
        assertTrue( secondResult.getProviderMessage(), secondResult.isSuccess() );
        assertEquals(
                "the changelog from root does not have good commits number",
                0, secondResult.getChangeLog().getChangeSets().size()
        );
    }

    public void testChangeLogCommandFromRootToEnd()
            throws Exception
    {
        Thread.sleep( 1000 );
        ScmRepository scmRepository = getScmRepository();
        ScmProvider provider = getScmManager().getProviderByRepository( scmRepository );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        ChangeLogScmRequest clr = new ChangeLogScmRequest( scmRepository, fileSet );
        clr.setStartFromRoot();
        clr.setEndRevision( new ScmRevision( "HEAD" ) );
        ChangeLogScmResult firstResult = provider.changeLog( clr );

        int firstLogSize = firstResult.getChangeLog().getChangeSets().size();
        assertEquals( "Unexpected initial log size", 1, firstLogSize );

        Thread.sleep( 2000 );
        //Make a change to the readme.txt and commit the change
        this.edit( getWorkingCopy(), "readme.txt", null, getScmRepository() );
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );
        CheckInScmResult checkInResult = provider.checkIn( getScmRepository(), fileSet, "dummy commit message" );
        assertTrue( "Unable to checkin changes to the repository", checkInResult.isSuccess() );

        ChangeLogScmResult secondResult = provider.changeLog( clr );
        assertTrue( secondResult.getProviderMessage(), secondResult.isSuccess() );
        assertEquals(
                "the changelog from root does not have good commits number",
                2, secondResult.getChangeLog().getChangeSets().size()
        );
    }
}
