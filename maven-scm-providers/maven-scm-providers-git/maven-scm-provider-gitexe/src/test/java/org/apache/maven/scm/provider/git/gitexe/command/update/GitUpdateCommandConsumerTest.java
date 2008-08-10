package org.apache.maven.scm.provider.git.gitexe.command.update;

import java.io.BufferedReader;
import java.io.File;
import java.io.Reader;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.DefaultLog;
import org.codehaus.plexus.PlexusTestCase;
import org.codehaus.plexus.util.ReaderFactory;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * @author <a href="mailto:olamy@apache.org">olamy</a>
 * @since 10 août 2008
 * @version $Id$
 */
public class GitUpdateCommandConsumerTest
    extends PlexusTestCase
{
    public void testOneUpdate()
        throws Exception
    {
        assertOneUpdate( "/src/test/resources/git/update/git-update-one.out" );
    }
    
    public void testOneUpdateOtherFormat()
        throws Exception
    {
        assertOneUpdate( "/src/test/resources/git/update/git-update-one-other-format.out" );
    }
    

    private void assertOneUpdate( String fileName )
        throws Exception
    {
        GitUpdateCommandConsumer consumer = new GitUpdateCommandConsumer( new DefaultLog(), null );

        BufferedReader r = getGitLogBufferedReader( fileName );

        String line;

        while ( ( line = r.readLine() ) != null )
        {
            //System.out.println(" line " + line );
            consumer.consumeLine( line );
        }

        List changedFiles = consumer.getUpdatedFiles();

        assertEquals( 1, changedFiles.size() );

        assertScmFile( (ScmFile) changedFiles.get( 0 ), "pom.xml", ScmFileStatus.UPDATED );
    }

    private BufferedReader getGitLogBufferedReader( String fileName )
        throws Exception
    {
        File f = getTestFile( fileName );
        Reader reader = ReaderFactory.newReader( f, "UTF-8" );
        return new BufferedReader( reader );
    }

    private void assertScmFile( ScmFile file, String fileName, ScmFileStatus status )
    {
        assertEquals( fileName, file.getPath() );
        assertEquals( status, file.getStatus() );
    }

}
