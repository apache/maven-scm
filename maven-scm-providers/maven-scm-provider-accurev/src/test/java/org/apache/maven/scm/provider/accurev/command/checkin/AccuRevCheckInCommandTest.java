package org.apache.maven.scm.provider.accurev.command.checkin;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.junit.Test;

public class AccuRevCheckInCommandTest
    extends AbstractAccuRevCommandTest
{

    @Test
    public void testCheckInRecursive()
        throws Exception
    {
        // Setup test data so that the checkin area is the repo's project path.
        final ScmFileSet testFileSet = new ScmFileSet( new File( basedir, "project/dir" ) );
        final File basedir = testFileSet.getBasedir();

        final AccuRevInfo info = new AccuRevInfo( basedir );
        info.setTop( basedir.getAbsolutePath() );

        when( accurev.info( basedir ) ).thenReturn( info );

        List<File> promotedFiles = Arrays.asList( new File( "kept/file" ), new File( "promoted/file" ) );
        when( accurev.promoteAll( basedir, "A commit message" ) ).thenReturn( promotedFiles );

        AccuRevCheckInCommand command = new AccuRevCheckInCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A commit message" );
        CheckInScmResult result = command.checkIn( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getCheckedInFiles().size(), is( 2 ) );
        assertHasScmFile( result.getCheckedInFiles(), "kept/file", ScmFileStatus.CHECKED_IN );
        assertHasScmFile( result.getCheckedInFiles(), "promoted/file", ScmFileStatus.CHECKED_IN );
    }

    @Test
    public void testCheckInFailure()
        throws Exception
    {
        // Setup test data so that the checkin area is the repo's project path.
        final ScmFileSet testFileSet = new ScmFileSet( new File( basedir, "project/dir" ) );
        final File basedir = testFileSet.getBasedir();

        final AccuRevInfo info = new AccuRevInfo( basedir );
        info.setTop( basedir.getAbsolutePath() );

        when( accurev.info( basedir ) ).thenReturn( info );

        when( accurev.promoteAll( basedir, "A commit message" ) ).thenReturn( null );

        AccuRevCheckInCommand command = new AccuRevCheckInCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A commit message" );
        CheckInScmResult result = command.checkIn( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );
    }

    @Test( expected = ScmException.class )
    public void testCheckinRecursiveSubDirectoryNotSupported()
        throws AccuRevException, ScmException
    {
        final ScmFileSet testFileSet = new ScmFileSet( basedir );
        final AccuRevInfo info = new AccuRevInfo( basedir );
        info.setTop( basedir.getParent() );

        // TODO test basedir is top + project path. is OK.
        when( accurev.info( basedir ) ).thenReturn( info );

        AccuRevCheckInCommand command = new AccuRevCheckInCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "Commit message" );
        command.checkIn( repo, testFileSet, commandParameters );
        fail( "Expected ScmException" );
    }

    @Test
    public void testCheckinExplicitFiles()
        throws Exception
    {
        final List<File> files = new ArrayList<File>();

        files.add( new File( "project/dir/pom.xml" ) );
        files.add( new File( "project/dir/src/main/java/Bar.java" ) );

        final ScmFileSet testFileSet = new ScmFileSet( basedir, files );

        when( accurev.info( basedir ) ).thenReturn( info );

        when( accurev.promote( basedir, files, "A commit message" ) ).thenReturn( files );

        AccuRevCheckInCommand command = new AccuRevCheckInCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A commit message" );
        CheckInScmResult result = command.checkIn( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getCheckedInFiles().size(), is( 2 ) );
        assertHasScmFile( result.getCheckedInFiles(), "project/dir/pom.xml", ScmFileStatus.CHECKED_IN );
        assertHasScmFile( result.getCheckedInFiles(), "project/dir/src/main/java/Bar.java", ScmFileStatus.CHECKED_IN );

    }
}
