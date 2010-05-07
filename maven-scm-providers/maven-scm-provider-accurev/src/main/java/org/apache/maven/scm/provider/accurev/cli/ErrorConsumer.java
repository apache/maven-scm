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

import java.util.regex.Pattern;

import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

final class ErrorConsumer
    implements StreamConsumer
{

    private static final Pattern[] SKIPPED_WARNINGS = {
        Pattern.compile( "No elements selected.*" ),
        Pattern.compile( "You are not in a directory.*" ),
        Pattern.compile( "Note.*" ),
        Pattern.compile( "\\s+(members,|conjunction).*" ) };

    private final ScmLogger logger;

    private final StringBuffer errors;

    public ErrorConsumer( ScmLogger logger, StringBuffer errors )
    {
        this.logger = logger;
        this.errors = errors;
    }

    public void consumeLine( String line )
    {
        errors.append( line );
        errors.append( '\n' );

        boolean matched = false;

        // if debugging log everything, otherwise log if doesn't match the skip patterns.
        int i = logger.isDebugEnabled() ? SKIPPED_WARNINGS.length : 0;
        while ( !matched && i < SKIPPED_WARNINGS.length )
        {
            matched = SKIPPED_WARNINGS[i++].matcher( line ).matches();
        }

        if ( !matched )
        {
            logger.warn( line );
        }
    }
}