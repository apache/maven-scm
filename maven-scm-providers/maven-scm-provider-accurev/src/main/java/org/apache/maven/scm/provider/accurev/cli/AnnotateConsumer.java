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

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.util.AbstractConsumer;

/**
 * @author Evgeny Mandrikov
 * @author Grant Gardner
 * @since 1.4
 */
public class AnnotateConsumer
    extends AbstractConsumer
{

    /* 3 godin 2009/11/18 16:26:33 */
    private static final Pattern LINE_PATTERN = Pattern.compile( "^\\s+(\\d+)\\s+(\\w+)\\s+([0-9/]+ [0-9:]+).*" );

    private List<BlameLine> lines;

    public AnnotateConsumer( List<BlameLine> lines, ScmLogger scmLogger )
    {

        super( scmLogger );
        this.lines = lines;
    }

    public void consumeLine( String line )
    {

        final Matcher matcher = LINE_PATTERN.matcher( line );
        if ( matcher.matches() )
        {
            String revision = matcher.group( 1 ).trim();
            String author = matcher.group( 2 ).trim();
            String dateStr = matcher.group( 3 ).trim();

            Date date = parseDate( dateStr, null, AccuRev.ACCUREV_TIME_FORMAT_STRING );

            lines.add( new BlameLine( date, revision, author ) );
        }
        else
        {
            throw new RuntimeException( "Unable to parse annotation from line: " + line );
        }
    }

    public List<BlameLine> getLines()
    {

        return lines;
    }
}
