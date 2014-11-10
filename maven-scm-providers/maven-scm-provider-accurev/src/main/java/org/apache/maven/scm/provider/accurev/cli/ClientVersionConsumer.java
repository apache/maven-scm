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
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * 
 */
public class ClientVersionConsumer
    implements StreamConsumer
{
    private static final Pattern CLIENT_VERSION_PATTERN = Pattern.compile( "^AccuRev ([0-9a-z.]+) .*" );

    private String clientVersion;

    public void consumeLine( String line )
    {
        if ( clientVersion == null )
        {
            Matcher matcher = CLIENT_VERSION_PATTERN.matcher( line );
            if ( matcher.matches() )
            {
                clientVersion = matcher.group( 1 );
            }
        }
    }

    public String getClientVersion()
    {
        return clientVersion;
    }

}
