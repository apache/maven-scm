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
import org.apache.maven.scm.log.DefaultLog;

import java.io.*;

/**
 * @author Evgeny Mandrikov
 */
public class PerforceFilelogConsumerTest
    extends ScmTestCase
{

    public void testParse()
        throws IOException
    {
        File testFile = getTestFile( "src/test/resources/perforce/filelog.txt" );

        PerforceFilelogConsumer consumer = new PerforceFilelogConsumer( new DefaultLog() );

        FileInputStream fis = new FileInputStream( testFile );
        BufferedReader in = new BufferedReader( new InputStreamReader( fis ) );
        String s = in.readLine();
        while ( s != null )
        {
            consumer.consumeLine( s );
            s = in.readLine();
        }

        Assert.assertEquals( "earl", consumer.getAuthor( "35" ) );
        Assert.assertEquals( "raj", consumer.getAuthor( "34" ) );
    }

}
