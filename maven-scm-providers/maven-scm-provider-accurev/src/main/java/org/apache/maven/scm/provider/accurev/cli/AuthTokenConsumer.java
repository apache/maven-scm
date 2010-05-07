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

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

public class AuthTokenConsumer
    implements StreamConsumer
{

    private static final Pattern TOKEN_PATTERN = Pattern.compile( "(?:Password:|)\\s*([0-9a-f]+).*" );

    private String authToken = null;

    public String getAuthToken()
    {
        return authToken;
    }

    public void consumeLine( String line )
    {
        if ( StringUtils.isBlank( authToken ) )
        {
            Matcher matcher = TOKEN_PATTERN.matcher( line );
            if ( matcher.matches() )
            {
                authToken = matcher.group( 1 );
            }
        }
    }
}
