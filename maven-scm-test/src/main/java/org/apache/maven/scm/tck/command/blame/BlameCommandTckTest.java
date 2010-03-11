package org.apache.maven.scm.tck.command.blame;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTckTestCase;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProvider;
import org.apache.maven.scm.repository.ScmRepository;

import java.util.Date;

/**
 * @author Evgeny Mandrikov
 */
public abstract class BlameCommandTckTest
    extends ScmTckTestCase
{
    private static final String COMMIT_MSG = "Second changelog";

    public void testBlameCommand()
        throws Exception
    {
        ScmRepository repository = getScmRepository();
        ScmManager manager = getScmManager();
        ScmProvider provider = manager.getProviderByRepository( getScmRepository() );
        ScmFileSet fileSet = new ScmFileSet( getWorkingCopy() );

        BlameScmResult result;
        BlameLine line;

        // === readme.txt ===
        result = manager.blame( repository, fileSet, "readme.txt" );
        assertNotNull( "The command returned a null result.", result );
        assertResultIsSuccess( result );
        assertEquals( "Expected 1 line in blame", 1, result.getLines().size() );
        line = (BlameLine) result.getLines().get( 0 );
        String initialRevision = line.getRevision();

        //Make a timestamp that we know are after initial revision but before the second
        Date timeBeforeSecond = new Date(); // Current time
        // pause a couple seconds...
        Thread.sleep( 2000 );
        //Make a change to the readme.txt and commit the change
        ScmTestCase.makeFile( getWorkingCopy(), "/readme.txt", "changed readme.txt" );
        CheckInScmResult checkInResult = provider.checkIn( getScmRepository(), fileSet, COMMIT_MSG );
        assertTrue( "Unable to checkin changes to the repository", checkInResult.isSuccess() );

        result = manager.blame( repository, fileSet, "readme.txt" );

        // pause a couple seconds...
        Thread.sleep( 2000 );
        Date timeAfterSecond = new Date(); // Current time

        assertNotNull( "The command returned a null result.", result );
        assertResultIsSuccess( result );

        assertEquals( "Expected 1 line in blame", 1, result.getLines().size() );
        line = (BlameLine) result.getLines().get( 0 );

        assertNotNull( "Expected not null author", line.getAuthor() );
        assertNotNull( "Expected not null revision", line.getRevision() );
        assertNotNull( "Expected not null date", line.getDate() );

        assertTrue( "Expected another revision", !initialRevision.equals( line.getRevision() ) );
        if ( isTestDateTime() )
        {
            assertDateBetween( timeBeforeSecond, timeAfterSecond, line.getDate() );
        }

        // === pom.xml ===
        result = manager.blame( repository, fileSet, "pom.xml" );

        assertNotNull( "The command returned a null result.", result );

        assertResultIsSuccess( result );

        verifyResult( result );
    }

    protected boolean isTestDateTime()
    {
        return true;
    }

    protected void assertDateBetween( Date start, Date end, Date actual )
    {
        assertTrue( "Expected date between " + start + " and " + end + ", but was " + actual,
                    start.before( actual ) && actual.before( end ) );
    }

    protected abstract void verifyResult( BlameScmResult result );
}
