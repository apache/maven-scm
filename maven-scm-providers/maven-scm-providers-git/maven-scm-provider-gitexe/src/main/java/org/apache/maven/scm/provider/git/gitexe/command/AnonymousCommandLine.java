package org.apache.maven.scm.provider.git.gitexe.command;

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

import org.codehaus.plexus.util.cli.Commandline;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CommandLine extension to mask password
 * @since 1.9.3
 */
public class AnonymousCommandLine
    extends Commandline
{

    public static final String PASSWORD_PLACE_HOLDER = "********";

    private Pattern passwordPattern = Pattern.compile( "^.*:(.*)@.*$" );

    /**
     * Provides an anonymous output to mask password. Considering URL of type :
     * &lt;&lt;protocol&gt;&gt;://&lt;&lt;user&gt;&gt;:&lt;&lt;password&gt;&gt;@
     * &lt;&lt;host_definition&gt;&gt;
     */
    @Override
    public String toString()
    {
        String output = super.toString();
        final Matcher passwordMatcher = passwordPattern.matcher( output );
        if ( passwordMatcher.find() )
        {
            // clear password
            final String clearPassword = passwordMatcher.group( 1 );
            // to be replaced in output by stars
            output = output.replace( clearPassword, PASSWORD_PLACE_HOLDER );
        }
        return output;
    }
}