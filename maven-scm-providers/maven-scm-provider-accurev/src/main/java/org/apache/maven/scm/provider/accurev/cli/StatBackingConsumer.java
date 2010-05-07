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

import java.io.File;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.cli.StreamConsumer;

public class StatBackingConsumer
    implements StreamConsumer
{

    private static final Pattern STAT_PATTERN = Pattern.compile( "\\s*(\\S+)\\s+.*\\(([^()]+)\\)\\s*" );

    private static final String NO_SUCH_ELEM = "no such elem";

    private Collection<File> memberElements;

    private Collection<File> nonMemberElements;

    public StatBackingConsumer( Collection<File> memberElements, Collection<File> nonMemberElements )
    {
        this.memberElements = memberElements;
        this.nonMemberElements = nonMemberElements;
    }

    public void consumeLine( String line )
    {
        // first group is the fileName
        // second group is the indicator expected "backed","stale","overlap","underlap","no such elem";
        Pattern pattern = STAT_PATTERN;
        Matcher matcher = pattern.matcher( line );
        if ( matcher.matches() )
        {
            File file = new File( matcher.group( 1 ) );
            String indicator = matcher.group( 2 );
            if ( NO_SUCH_ELEM.equals( indicator ) )
            {
                nonMemberElements.add( file );
            }
            else
            {
                memberElements.add( file );
            }
        }

    }

}
