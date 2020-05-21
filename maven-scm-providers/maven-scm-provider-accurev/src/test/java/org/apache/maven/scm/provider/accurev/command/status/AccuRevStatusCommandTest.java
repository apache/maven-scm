package org.apache.maven.scm.provider.accurev.command.status;

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
import static org.apache.maven.scm.ScmFileMatcher.scmFile;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevStat;
import org.apache.maven.scm.provider.accurev.CategorisedElements;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.hamcrest.Matchers;
import org.junit.Test;

public class AccuRevStatusCommandTest
    extends AbstractAccuRevCommandTest
{

    @Test
    public void testStatus()
        throws Exception
    {

        final ScmFileSet testFileSet = getScmFileSet();

        File keptFile = new File( "kept/file" );
        File keptAdded = new File( "kept/added" );
        // this is the special one, it is returned by both the kept and defunct stat calls, so the command
        // needs to filter it out.
        File keptDefunct = new File( "kept/defunct" );
        File modifiedFile = new File( "modified/file" );
        File modifiedAdded = new File( "modified/added" );
        File missingFile = new File( "missing/file" );
        File externalFile = new File( "external/file" );

        when( accurev.stat( eq( basedir ), anyListOf( File.class ), eq( AccuRevStat.DEFUNCT ) ) ).thenReturn(
                                                                                                              Arrays.asList( keptDefunct ) );
        when( accurev.stat( eq( basedir ), anyListOf( File.class ), eq( AccuRevStat.MODIFIED ) ) ).thenReturn(
                                                                                                               Arrays.asList( modifiedFile,modifiedAdded ) );
        when( accurev.stat( eq( basedir ), anyListOf( File.class ), eq( AccuRevStat.KEPT ) ) ).thenReturn(
                                                                                                           Arrays.asList(
                                                                                                                          keptDefunct,
                                                                                                                          keptFile,
                                                                                                                          keptAdded ) );

        when( accurev.stat( eq( basedir ), anyListOf( File.class ), eq( AccuRevStat.MISSING ) ) ).thenReturn(
                                                                                                              Arrays.asList( missingFile ) );

        when( accurev.stat( eq( basedir ), anyListOf( File.class ), eq( AccuRevStat.EXTERNAL ) ) ).thenReturn(
                                                                                                               Arrays.asList( externalFile ) );

        CategorisedElements catElems = new CategorisedElements();
        catElems.getMemberElements().addAll( Arrays.asList( modifiedFile, keptFile ) );
        catElems.getNonMemberElements().addAll( Arrays.asList( modifiedAdded, keptAdded ) );
        when(
              accurev.statBackingStream( eq( basedir ), (Collection<File>) argThat( hasItems( modifiedFile,
                                                                                              modifiedAdded, keptFile,
                                                                                              keptAdded ) ) ) ).thenReturn(
                                                                                                                            catElems );

        AccuRevStatusCommand command = new AccuRevStatusCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        StatusScmResult result = command.status( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getChangedFiles().size(), is( 7 ) );

        assertThat( (List<ScmFile>) result.getChangedFiles(),
                    not( Matchers.<ScmFile>hasItem( scmFile( "kept/defunct", ScmFileStatus.MODIFIED ) ) ) );
        assertHasScmFile( result.getChangedFiles(), "kept/file", ScmFileStatus.MODIFIED );
        assertHasScmFile( result.getChangedFiles(), "kept/added", ScmFileStatus.ADDED );
        assertHasScmFile( result.getChangedFiles(), "kept/defunct", ScmFileStatus.DELETED );
        assertHasScmFile( result.getChangedFiles(), "modified/file", ScmFileStatus.MODIFIED );
        assertHasScmFile( result.getChangedFiles(), "modified/added", ScmFileStatus.ADDED );
        assertHasScmFile( result.getChangedFiles(), "missing/file", ScmFileStatus.MISSING );
        assertHasScmFile( result.getChangedFiles(), "external/file", ScmFileStatus.UNKNOWN );

    }

    @Test
    public void testFailure()
        throws Exception
    {

        final ScmFileSet testFileSet = getScmFileSet();

        when( accurev.stat( basedir, testFileSet.getFileList(), AccuRevStat.MODIFIED ) ).thenReturn( null );

        AccuRevStatusCommand command = new AccuRevStatusCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        StatusScmResult result = command.status( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );

    }

}
