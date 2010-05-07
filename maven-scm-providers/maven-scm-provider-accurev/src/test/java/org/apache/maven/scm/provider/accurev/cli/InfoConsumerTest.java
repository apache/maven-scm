package org.apache.maven.scm.provider.accurev.cli;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.junit.Test;

public class InfoConsumerTest
{

    @Test
    public void testConsumeOutsideWorkspace()
        throws Exception
    {

        AccuRevInfo info = consume( "/info.outsideworkspace.txt" );

        assertNull( info.getBasis() );
        assertNull( info.getTop() );
        assertNull( info.getWorkSpace() );
        assertThat( info.getUser(), is( "ggardner" ) );

    }

    @Test
    public void testConsumeInsideWorkspace()
        throws Exception
    {
        AccuRevInfo info = consume( "/info.inworkspace.txt" );

        assertThat( info.getBasis(), is( "maventst" ) );
        assertThat( info.getTop(), is( "/home/ggardner/accurev/ws/maventst" ) );
        assertThat( info.getWorkSpace(), is( "maventst_ggardner" ) );
        assertThat( info.getUser(), is( "ggardner" ) );

    }

    private AccuRevInfo consume( String resource )
        throws IOException
    {
        AccuRevInfo info = new AccuRevInfo( new File( "/my/project/dir" ) );
        StreamConsumer consumer = new InfoConsumer( info );

        BufferedReader reader = new BufferedReader( new InputStreamReader( this.getClass()
            .getResourceAsStream( resource ) ) );

        String line = reader.readLine();
        while ( line != null )
        {
            consumer.consumeLine( line );
            line = reader.readLine();
        }
        return info;
    }

}
