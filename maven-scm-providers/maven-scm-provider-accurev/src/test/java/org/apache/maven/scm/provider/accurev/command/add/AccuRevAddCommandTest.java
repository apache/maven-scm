package org.apache.maven.scm.provider.accurev.command.add;

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
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.junit.Test;

public class AccuRevAddCommandTest
    extends AbstractAccuRevCommandTest
{

    @Test
    public void testAdd()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( basedir, new File( "src/main/java/Foo.java" ) );
        final List<File> files = testFileSet.getFileList();

        when( accurev.add( basedir, files, "A new file" ) ).thenReturn(
                                                                        Collections.singletonList( new File(
                                                                                                             "added/file" ) ) );
        
        AccuRevAddCommand command = new AccuRevAddCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A new file" );
        AddScmResult result = command.add( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getAddedFiles().size(), is( 1 ) );
        assertHasScmFile( result.getAddedFiles(), "added/file", ScmFileStatus.ADDED );
    }

    @Test
    public void testAddFailed()
        throws Exception
    {
        final ScmFileSet testFileSet = new ScmFileSet( basedir, new File( "src/main/java/Foo.java" ) );
        final List<File> files = testFileSet.getFileList();

        when( accurev.add( basedir, files, "A new file" ) ).thenReturn(null);
                                                                       
        AccuRevAddCommand command = new AccuRevAddCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.MESSAGE, "A new file" );
        AddScmResult result = command.add( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( false ) );
        assertThat( result.getProviderMessage(), notNullValue() );
    }

}
