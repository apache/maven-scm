package org.apache.maven.scm.provider.bazaar.command.changelog;

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;
import org.junit.Assert;

public class BazaarChangeLogConsumerTest
    extends ScmTestCase
{
    public void testChanglogWithMergeEntries()
        throws IOException
    {
        File testFile = getTestFile( "src/test/resources/bazaar/changeLogWithMerge.txt" );

        BazaarChangeLogConsumer consumer = new BazaarChangeLogConsumer( new DefaultLog(), null );

        FileInputStream fis = new FileInputStream( testFile );
        BufferedReader in = new BufferedReader( new InputStreamReader( fis ) );
        String s = in.readLine();
        while ( s != null )
        {
            consumer.consumeLine( s );
            s = in.readLine();
        }

        List<ChangeSet> mods = consumer.getModifications();
        assertEquals( 4, mods.size() );

        final ChangeSet ch2 = mods.get( 2 );
        Assert.assertEquals( "Unexpected committer", "tsmoergrav@slb.com", ch2.getAuthor() );
        Assert.assertEquals( "Unexpected comment", "Second", ch2.getComment() );
        Assert.assertEquals( "File count", 2, ch2.getFiles().size() );

        final ChangeFile ch2f1 = ch2.getFiles().get( 0 );
        Assert.assertEquals( "Invalid action", ScmFileStatus.MODIFIED, ch2f1.getAction() );
        Assert.assertEquals( "Invalid  file name", "changeLogWithMerge.txt", ch2f1.getName() );
        Assert.assertNull( "Unexpected originalName", ch2f1.getOriginalName() );

        final ChangeFile ch2f2 = ch2.getFiles().get( 1 );
        Assert.assertEquals( "Invalid action", ScmFileStatus.RENAMED, ch2f2.getAction() );
        Assert.assertEquals( "Invalid file name", "blablabla.txt", ch2f2.getName() );
        Assert.assertEquals( "Invalid original name", "a", ch2f2.getOriginalName() );
    }
}
