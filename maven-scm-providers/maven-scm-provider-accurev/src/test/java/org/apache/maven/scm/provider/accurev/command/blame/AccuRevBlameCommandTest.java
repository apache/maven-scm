package org.apache.maven.scm.provider.accurev.command.blame;

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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Collections;
import java.util.Date;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommandTest;
import org.junit.Test;

public class AccuRevBlameCommandTest
    extends AbstractAccuRevCommandTest
{

    @Test
    public void testBlame()
        throws Exception
    {

        final File file = new File( "src/main/java/Foo.java" );
        final ScmFileSet testFileSet = new ScmFileSet( basedir, file );

        final Date date = new Date();
        final BlameLine blameLine = new BlameLine( date, "12", "theAuthor" );

        when( accurev.annotate( basedir, file ) ).thenReturn( Collections.singletonList( blameLine ) );

        AccuRevBlameCommand command = new AccuRevBlameCommand( getLogger() );

        CommandParameters commandParameters = new CommandParameters();
        commandParameters.setString( CommandParameter.FILE, file.getPath() );
        BlameScmResult result = command.blame( repo, testFileSet, commandParameters );

        assertThat( result.isSuccess(), is( true ) );
        assertThat( result.getLines().size(), is( 1 ) );
        assertThat( ( (BlameLine) result.getLines().get( 0 ) ), is( blameLine ) );

    }
}
