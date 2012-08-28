package org.apache.maven.scm.provider.hg;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import static org.junit.Assert.*;

import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.codehaus.plexus.util.cli.Commandline;
import org.junit.Test;

public class HgUtilsTest
{

    @Test
    public void testNullWorkingDirectory()
        throws Exception
    {
        Commandline cmd = HgUtils.buildCmd( null, new String[] {} );
        assertEquals( null, cmd.getWorkingDirectory() );
    }

    @Test
    public void testCryptPassword()
        throws Exception
    {
        Commandline cmdHttps = HgUtils.buildCmd( null, new String[] {
                HgCommandConstants.PUSH_CMD,
                null,
                "https://username:password@example.com/foobar"
        } );
        Commandline cmd = new Commandline( HgUtils.cryptPassword( cmdHttps ) );
        assertEquals( "https://username:*****@example.com/foobar", cmd.getArguments()[3] );
    }
}
