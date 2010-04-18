package org.apache.maven.scm.provider.perforce.command.blame;

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

import junit.framework.Assert;
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.DefaultLog;

import java.io.*;

/**
 * @author Evgeny Mandrikov
 */
public class PerforceBlameConsumerTest
    extends ScmTestCase
{

    public void testParse()
        throws IOException
    {
        File testFile = getTestFile( "src/test/resources/perforce/annotatelog.txt" );

        PerforceBlameConsumer consumer = new PerforceBlameConsumer( new DefaultLog() );

        FileInputStream fis = new FileInputStream( testFile );
        BufferedReader in = new BufferedReader( new InputStreamReader( fis ) );
        String s = in.readLine();
        while ( s != null )
        {
            consumer.consumeLine( s );
            s = in.readLine();
        }

        Assert.assertEquals( 2, consumer.getLines().size() );

        BlameLine line1 = (BlameLine) consumer.getLines().get( 0 );
        Assert.assertEquals( "1", line1.getRevision() );

        BlameLine line2 = (BlameLine) consumer.getLines().get( 1 );
        Assert.assertEquals( "2", line2.getRevision() );
    }

}
