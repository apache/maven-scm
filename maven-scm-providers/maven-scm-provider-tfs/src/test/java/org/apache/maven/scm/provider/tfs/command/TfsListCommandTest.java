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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ServerFileListConsumer;
import org.codehaus.plexus.util.cli.Commandline;

public class TfsListCommandTest
    extends TfsCommandTest
{

    private ServerFileListConsumer consumer;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        consumer = new ServerFileListConsumer();
    }

    public void testCommandline()
        throws Exception    
    {
        TfsScmProviderRepository repo = getScmProviderRepository();
        Commandline cmd = new TfsListCommand().createCommand( repo, getScmFileSet(), true ).getCommandline();
        String expected = "tf dir -login:user,password -recursive " + getFileList();
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testMSCommand()
    {
        consumer.consumeLine( "$/junk/com.teamprise.core/libs/xstream-1.1.3/lib:" );
        consumer.consumeLine( "xpp3-1.1.3.4d_b4_min.jar" );
        consumer.consumeLine( "xpp3-license.txt" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "$/junk/com.teamprise.core/messages:" );
        consumer.consumeLine( "$com" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "$/junk/com.teamprise.core/messages/com:" );
        consumer.consumeLine( "$teamprise" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "$/junk/com.teamprise.core/messages/com/teamprise:" );
        consumer.consumeLine( "$core" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "$/junk/com.teamprise.core/messages/com/teamprise/core:" );
        consumer.consumeLine( "$pguidance" );
        consumer.consumeLine( "$workitem" );

        assertNotNull( consumer.getFiles() );
        assertEquals( 9, consumer.getFiles().size() );
        assertTrue( consumer.getFiles().contains(
                                                  new ScmFile(
                                                               "$/junk/com.teamprise.core/libs/xstream-1.1.3/lib/xpp3-license.txt",
                                                               ScmFileStatus.CHECKED_OUT ) ) );
        assertTrue( consumer.getFiles().contains(
                                                  new ScmFile( "$/junk/com.teamprise.core/messages/com",
                                                               ScmFileStatus.CHECKED_OUT ) ) );
        assertTrue( consumer.getFiles().contains(
                                                  new ScmFile(
                                                               "$/junk/com.teamprise.core/messages/com/teamprise/core/pguidance",
                                                               ScmFileStatus.CHECKED_OUT ) ) );
    }

}
