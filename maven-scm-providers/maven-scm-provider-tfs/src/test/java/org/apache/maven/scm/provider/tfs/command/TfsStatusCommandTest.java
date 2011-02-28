package org.apache.maven.scm.provider.tfs.command;

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

import java.util.List;
import java.util.Locale;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ChangedFileConsumer;
import org.codehaus.plexus.util.cli.Commandline;

public class TfsStatusCommandTest
    extends TfsCommandTest
{

    private ChangedFileConsumer consumer;

    private Locale defaultLocale;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        consumer = new ChangedFileConsumer( new DefaultLog() );
        defaultLocale = Locale.getDefault();
    }

    public void testCommandline()
        throws Exception    
    {
        TfsScmProviderRepository repo = getScmProviderRepository();
        Commandline cmd = new TfsStatusCommand().createCommand( repo, getScmFileSet() ).getCommandline();
        String expected =
            "tf status -login:user,password -workspace:workspace -recursive -format:detailed " + repo.getServerPath();
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    protected void tearDown()
        throws Exception
    {
        Locale.setDefault( defaultLocale );
    }

    public void testCommand()
    {
        consumer.consumeLine( "$/junk/pluginp/.classpath;C1858" );
        consumer.consumeLine( "  User:       CDESG\\subhash" );
        consumer.consumeLine( "  Date:       Mar 12, 2009 2:18:31 AM" );
        consumer.consumeLine( "  Lock:       none" );
        consumer.consumeLine( "  Change:     edit" );
        consumer.consumeLine( "  Workspace:  purinaTest" );
        consumer.consumeLine( "  Local item: [SUBHASH-PC] C:\\temp\\maven\\c4\\.classpath" );
        consumer.consumeLine( "  File type:  windows-1252" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "$/junk/pluginp/.project;C1858" );
        consumer.consumeLine( "  User:       CDESG\\subhash" );
        consumer.consumeLine( "  Date:       Mar 12, 2009 2:18:31 AM" );
        consumer.consumeLine( "  Lock:       none" );
        consumer.consumeLine( "  Change:     edit" );
        consumer.consumeLine( "  Workspace:  purinaTest" );
        consumer.consumeLine( "  Local item: [SUBHASH-PC] C:\\temp\\maven\\c4\\.project" );
        consumer.consumeLine( "  File type:  windows-1252" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "$/junk/pluginp/build.properties;C1858" );
        consumer.consumeLine( "  User:       CDESG\\subhash" );
        consumer.consumeLine( "  Date:       Mar 12, 2009 2:18:31 AM" );
        consumer.consumeLine( "  Lock:       none" );
        consumer.consumeLine( "  Change:     edit" );
        consumer.consumeLine( "  Workspace:  purinaTest" );
        consumer.consumeLine( "  Local item: [SUBHASH-PC] C:\\temp\\maven\\c4\\build.properties" );
        consumer.consumeLine( "  File type:  windows-1252" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "$/junk/pluginp/META-INF/MANIFEST.MF;C1858" );
        consumer.consumeLine( "  User:       CDESG\\subhash" );
        consumer.consumeLine( "  Date:       Mar 12, 2009 2:18:31 AM" );
        consumer.consumeLine( "  Lock:       none" );
        consumer.consumeLine( "  Change:     edit" );
        consumer.consumeLine( "  Workspace:  purinaTest" );
        consumer.consumeLine( "  Local item: [SUBHASH-PC] C:\\temp\\maven\\c4\\META-INF\\MANIFEST.MF" );
        consumer.consumeLine( "  File type:  windows-1252" );
        consumer.consumeLine( "" );

        List<ScmFile> changedFiles = consumer.getChangedFiles();
        assertNotNull( changedFiles );
        assertEquals( 4, changedFiles.size() );
        assertTrue( changedFiles.contains( new ScmFile( "C:\\temp\\maven\\c4\\.classpath", ScmFileStatus.MODIFIED ) ) );
        assertTrue( changedFiles.contains( new ScmFile( "C:\\temp\\maven\\c4\\META-INF\\MANIFEST.MF",
                                                        ScmFileStatus.MODIFIED ) ) );
    }

    public void testLocale()
    {
        Locale.setDefault( Locale.GERMAN );
        String date = "12.03.2009 02:18:31";
        consumer.consumeLine( "$/junk/pluginp/.classpath;C1858" );
        consumer.consumeLine( "  User:       CDESG\\subhash" );
        consumer.consumeLine( "  Date:       " + date );
        consumer.consumeLine( "  Lock:       none" );
        consumer.consumeLine( "  Change:     edit" );
        consumer.consumeLine( "  Workspace:  purinaTest" );
        consumer.consumeLine( "  Local item: [SUBHASH-PC] C:\\temp\\maven\\c4\\.classpath" );
        consumer.consumeLine( "  File type:  windows-1252" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "$/junk/pluginp/.project;C1858" );
        consumer.consumeLine( "  User:       CDESG\\subhash" );
        consumer.consumeLine( "  Date:       " + date );
        consumer.consumeLine( "  Lock:       none" );
        consumer.consumeLine( "  Change:     edit" );
        consumer.consumeLine( "  Workspace:  purinaTest" );
        consumer.consumeLine( "  Local item: [SUBHASH-PC] C:\\temp\\maven\\c4\\.project" );
        consumer.consumeLine( "  File type:  windows-1252" );
        consumer.consumeLine( "" );
        List<ScmFile> changedFiles = consumer.getChangedFiles();
        assertNotNull( changedFiles );
        assertEquals( 2, changedFiles.size() );
    }
}
