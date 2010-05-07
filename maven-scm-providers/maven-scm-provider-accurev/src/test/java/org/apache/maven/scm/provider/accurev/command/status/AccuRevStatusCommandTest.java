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
import static org.apache.maven.scm.provider.accurev.AddElementsAction.addElementsTo;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevStat;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.jmock.Expectations;
import org.junit.Test;

public class AccuRevStatusCommandTest
    extends AbstractAccuRevCommandTest
{

    @SuppressWarnings("unchecked")
    @Test
    public void testStatus()
        throws Exception
    {

        final ScmFileSet testFileSet = getScmFileSet();

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        context.checking( new Expectations()
        {
            {
                File keptFile = new File( "kept/file" );
                File keptAdded = new File( "kept/added" );
                // this is the special one, it is returned by both the kept and defunct stat calls, so the command
                // needs to filter it out.
                File keptDefunct = new File( "kept/defunct" );
                File modifiedFile = new File( "modified/file" );
                File modifiedAdded = new File( "modified/added" );
                File missingFile = new File( "missing/file" );
                File externalFile = new File( "external/file" );

                one( accurev ).stat( with( basedir ), with( testFileSet.getFileList() ), with( AccuRevStat.DEFUNCT ),
                                     with( any( List.class ) ) );
                will( doAll( addElementsTo( 3, keptDefunct ), returnValue( true ) ) );

                one( accurev ).stat( with( basedir ), with( testFileSet.getFileList() ), with( AccuRevStat.MODIFIED ),
                                     with( any( List.class ) ) );
                will( doAll( addElementsTo( 3, modifiedFile, modifiedAdded ), returnValue( true ) ) );

                one( accurev ).stat( with( basedir ), with( testFileSet.getFileList() ), with( AccuRevStat.KEPT ),
                                     with( any( List.class ) ) );
                will( doAll( addElementsTo( 3, keptDefunct, keptFile, keptAdded ), returnValue( true ) ) );

                one( accurev ).stat( with( basedir ), with( testFileSet.getFileList() ), with( AccuRevStat.MISSING ),
                                     with( any( List.class ) ) );
                will( doAll( addElementsTo( 3, missingFile ), returnValue( true ) ) );

                one( accurev ).stat( with( basedir ), with( testFileSet.getFileList() ), with( AccuRevStat.EXTERNAL ),
                                     with( any( List.class ) ) );
                will( doAll( addElementsTo( 3, externalFile ), returnValue( true ) ) );

                one( accurev ).statBackingStream(
                                                  with( basedir ),
                                                  (Collection<File>) with( allOf( hasItems( modifiedFile,
                                                                                            modifiedAdded, keptFile,
                                                                                            keptAdded ),
                                                                                  not( hasItem( keptDefunct ) ) ) ),
                                                  with( any( List.class ) ), with( any( List.class ) ) );
                will( doAll( addElementsTo( 2, modifiedFile, keptFile ), addElementsTo( 3, modifiedAdded, keptAdded ),
                             returnValue( true ) ) );

            }
        } );

        AccuRevStatusCommand command = new AccuRevStatusCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        StatusScmResult result = command.status( repo, testFileSet, commandParameters );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getChangedFiles().size(), is( 7 ) );
        // Note the case.. without compiles under Eclipse but not sun JDK
        assertThat( (List<ScmFile>) result.getChangedFiles(), not( hasItem( scmFile( "kept/defunct",
                                                                                     ScmFileStatus.MODIFIED ) ) ) );
        assertHasScmFile( result.getChangedFiles(), "kept/file", ScmFileStatus.MODIFIED );
        assertHasScmFile( result.getChangedFiles(), "kept/added", ScmFileStatus.ADDED );
        assertHasScmFile( result.getChangedFiles(), "kept/defunct", ScmFileStatus.DELETED );
        assertHasScmFile( result.getChangedFiles(), "modified/file", ScmFileStatus.MODIFIED );
        assertHasScmFile( result.getChangedFiles(), "modified/added", ScmFileStatus.ADDED );
        assertHasScmFile( result.getChangedFiles(), "missing/file", ScmFileStatus.MISSING );
        assertHasScmFile( result.getChangedFiles(), "external/file", ScmFileStatus.UNKNOWN );

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testFailure()
        throws Exception
    {

        final ScmFileSet testFileSet = getScmFileSet();

        AccuRevScmProviderRepository repo = new AccuRevScmProviderRepository();
        repo.setStreamName( "myStream" );
        repo.setAccuRev( accurev );
        repo.setProjectPath( "/project/dir" );

        context.checking( new Expectations()
        {
            {
                one( accurev ).stat( with( basedir ), with( testFileSet.getFileList() ), with( AccuRevStat.MODIFIED ),
                                     with( any( List.class ) ) );
                will( returnValue( false ) );

                atMost( 4 ).of( accurev ).stat( with( basedir ), with( testFileSet.getFileList() ),
                                                with( any( AccuRevStat.class ) ), with( any( List.class ) ) );
                will( returnValue( true ) );
            }
        } );

        AccuRevStatusCommand command = new AccuRevStatusCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        StatusScmResult result = command.status( repo, testFileSet, commandParameters );

        context.assertIsSatisfied();

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );

    }

}
