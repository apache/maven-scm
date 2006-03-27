package org.apache.maven.scm.provider.bazaar.command.changelog;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;

public class BazaarChangeLogConsumerTest
    extends ScmTestCase
{
    public void testChanglogWithMergeEntries() throws IOException
    {
        File testFile = getTestFile( "src/test/resources/bazaar/changeLogWithMerge.txt" );

        BazaarChangeLogConsumer consumer = new BazaarChangeLogConsumer( new DefaultLog(), null);

        FileInputStream fis = new FileInputStream( testFile );
        BufferedReader in = new BufferedReader( new InputStreamReader( fis ) );
        String s = in.readLine();
        while ( s != null )
        {
            consumer.consumeLine( s );
            s = in.readLine();
        }

        List mods = consumer.getModifications();
        assertEquals(4, mods.size());
    }
}
