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

import java.util.Iterator;

import junit.framework.TestCase;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

public class TfsEditCommandTest
    extends TfsCommandTest
{

    private FileListConsumer consumer;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        consumer = new FileListConsumer();
    }
    
    public void testCommandline()
    {
        TfsScmProviderRepository repo = getScmProviderRepository();
        Commandline cmd = new TfsEditCommand().createCommand( repo, getScmFileSet() ).command;
        String expected =
            "tf checkout -login:user,password " + getFileList();
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testCommand()
    {
        consumer.consumeLine( ".classpath" );
        consumer.consumeLine( ".project" );
        consumer.consumeLine( "build.properties" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "META-INF:" );
        consumer.consumeLine( "MANIFEST.MF" );
        consumer.consumeLine( "" );
        consumer.consumeLine( "src\\pluginp:" );
        consumer.consumeLine( "Activator.java" );

        assertNotNull( consumer.getFiles() );
        assertEquals( 7, consumer.getFiles().size() );
    }

}
