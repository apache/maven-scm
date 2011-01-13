package org.apache.maven.scm.provider.starteam.command.checkin;

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

import java.io.File;
import java.util.Collection;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 */
public class StarteamCheckInConsumerTest
    extends ScmTestCase
{
    private static String[] TEST_OUTPUT = {"Folder: driver  (working dir: /usr/scm-starteam/driver)",
        "maven.xml: checked in", "Folder: driver  (working dir: /usr/scm-starteam/driver/target/checkout)",
        "maven.xml: checked in", "project.properties: skipped", "project.xml: checked in",
        "Folder: bootstrap  (working dir: /usr/scm-starteam/driver/target/checkout/bootstrap)",
        "Folder: dev  (working dir: /usr/scm-starteam/driver/target/checkout/dev)", "maven.xml:skipped",
        "project.properties: skipped", "project.xml: checked in"};

    public void testParse()
        throws Exception
    {
        File basedir = new File( "/usr/scm-starteam/driver" );

        StarteamCheckInConsumer consumer = new StarteamCheckInConsumer( new DefaultLog(), basedir );

        for ( int i = 0; i < TEST_OUTPUT.length; ++i )
        {
            consumer.consumeLine( TEST_OUTPUT[i] );
        }

        Collection<ScmFile> entries = consumer.getCheckedInFiles();

        assertEquals( "Wrong number of entries returned", 4, entries.size() );

        ;

        for ( ScmFile entry : entries )
        {
            assertTrue( entry.getPath().startsWith( "./" ) );

            assertTrue( entry.getStatus() == ScmFileStatus.CHECKED_OUT );
        }


    }
}
