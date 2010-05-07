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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.plexus.util.cli.StreamConsumer;

class FileConsumer
    implements StreamConsumer
{
    private Pattern filePattern;

    public FileConsumer( List<File> matchedFilesAccumulator, Pattern filematcher )
    {
        this.matchedFiles = matchedFilesAccumulator;
        this.filePattern = filematcher;
    }

    public List<File> matchedFiles;

    // TODO make these an enum
    public static final Pattern ADD_PATTERN = Pattern.compile( "Added and kept element [/\\\\]\\.[/\\\\](\\S+)\\s*" );

    public static final Pattern UPDATE_PATTERN = Pattern
        .compile( "Updating element [/\\\\]\\.[/\\\\](\\S+)\\s*|Content.*of \"(.*)\".*" );

    public static final Pattern POPULATE_PATTERN = Pattern.compile( "Populating element [/\\\\]\\.[/\\\\](\\S+)\\s*" );

    public static final Pattern PROMOTE_PATTERN = Pattern.compile( "Promoted element [/\\\\]\\.[/\\\\](\\S+)\\s*" );

    public static final Pattern STAT_PATTERN = Pattern.compile( "[/\\\\]\\.[/\\\\](.*)" );

    /**
     * TODO - The removed files are relative to the workspace top, not the basedir of the fileset
     */
    public static final Pattern DEFUNCT_PATTERN = Pattern.compile( "Removing \"(\\S+)\".*" );

    public void consumeLine( String line )
    {

        Matcher m = filePattern.matcher( line );
        if ( m.matches() )
        {

            int i = 1;
            String fileName = null;
            while ( fileName == null && i <= m.groupCount() )
            {
                fileName = m.group( i++ );
            }

            if ( fileName != null )
            {
                matchedFiles.add( new File( fileName ) );
            }
        }

    }
}