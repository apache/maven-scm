package org.apache.maven.scm.provider.starteam.command.unedit;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;

import java.io.File;
import java.util.Collection;

/**
 * @author <a href="mailto:dantran@gmail.com">Dan T. Tran</a>
 */
public class StarteamUnEditConsumerTest
    extends ScmTestCase
{
    private static String [] TEST_OUTPUT = {"Folder: driver  (working dir: /usr/scm-starteam/driver)",
        "maven.xml: unlocked", "Folder: driver  (working dir: /usr/scm-starteam/driver/target/checkout)",
        "maven.xml: unlocked", "project.properties: unlocked", "project.xml: unlocked",
        "Folder: bootstrap  (working dir: /usr/scm-starteam/driver/target/checkout/bootstrap)", "maven.xml: unlocked",
        "project.properties: unlocked", "project.xml: unlocked"};

    public void testParse()
        throws Exception
    {

        File basedir = new File( "/usr/scm-starteam/driver" );

        StarteamUnEditConsumer consumer = new StarteamUnEditConsumer( new DefaultLog(), basedir );

        for ( int i = 0; i < TEST_OUTPUT.length; ++ i )
        {
            consumer.consumeLine( TEST_OUTPUT[i] );
        }

        Collection entries = consumer.getUnEditFiles();

        assertEquals( "Wrong number of entries returned", 7, entries.size() );

    }

}
