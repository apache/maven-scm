package org.apache.maven.scm.provider.clearcase.command.blame;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author Jérémie Lagarde
 */
public class ClearCaseBlameConsumerTest
    extends ScmTestCase
{

    public void testConsumer()
        throws IOException
    {
        
        InputStream inputStream = getResourceAsStream( "/clearcase/blame/clearcase.log" );

        BufferedReader in = new BufferedReader( new InputStreamReader( inputStream ) );

        String s = in.readLine();

        ClearCaseBlameConsumer consumer = new ClearCaseBlameConsumer( new DefaultLog() );

        while ( s != null )
        {
            consumer.consumeLine( s );

            s = in.readLine();
        }

        Assert.assertEquals( 12, consumer.getLines().size() );

        BlameLine line1 = (BlameLine) consumer.getLines().get( 0 );
        Assert.assertEquals( "7", line1.getRevision() );
        Assert.assertEquals( "jeremie lagarde", line1.getAuthor() );

        BlameLine line12 = (BlameLine) consumer.getLines().get( 11 );
        Assert.assertEquals( "5", line12.getRevision() );
        Assert.assertEquals( "evgeny mandrikov", line12.getAuthor() );
    }

}