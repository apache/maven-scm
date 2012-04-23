package org.apache.maven.scm.provider.jazz.command.blame;

import java.util.Locale;

import org.apache.maven.scm.log.DefaultLog;
import org.apache.maven.scm.provider.jazz.JazzScmTestCase;
import org.apache.maven.scm.provider.jazz.repository.JazzScmProviderRepository;
import org.codehaus.plexus.util.cli.Commandline;

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

/**
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzBlameCommandTest
    extends JazzScmTestCase
{
    private JazzScmProviderRepository repo;
    
    private JazzBlameConsumer blameConsumer;

    private Locale defaultLocale;

    protected void setUp()
        throws Exception
    {
        super.setUp();
        repo = getScmProviderRepository();
        blameConsumer = new JazzBlameConsumer( repo, new DefaultLog() );
        defaultLocale = Locale.getDefault();
    }

    protected void tearDown()
        throws Exception
    {
        Locale.setDefault( defaultLocale );
    }

    public void testCreateBlameCommand()
        throws Exception
    {
        Commandline cmd = new JazzBlameCommand().createBlameCommand( repo, getScmFileSet(), "test.txt").getCommandline();
        String expected = "scm annotate --username myUserName --password myPassword test.txt";
        assertCommandLine( expected, getWorkingDirectory(), cmd );
    }

    public void testConsumer()
    {
//      C:\tmp\maven\BogusTest>scm annotate --username Deb --password Deb test.txt
//      1 Deb (1008) 2011-12-14                       Test.txt
//      2 Deb (1005) 2011-12-14 59 My commit comment.
        
        blameConsumer.consumeLine( "1 Deb (1008) 2011-12-14                       Test.txt" );
        blameConsumer.consumeLine( "2 Deb (1005) 2011-12-14 59 My commit comment." );

        assertEquals( "Wrong number of lines parsed!", 2, blameConsumer.getLines().size() );
    }

}
