package org.apache.maven.scm.provider.tfs.command.blame;

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

import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.util.AbstractConsumer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Evgeny Mandrikov
 * @since 1.4
 */
public class TfsBlameConsumer
    extends AbstractConsumer
{
    private static final String TFS_TIMESTAMP_PATTERN = "MM/dd/yyyy";

    /* 3 username 3/13/2006 line */
    // TODO simplify

    private static final Pattern LINE_PATTERN = Pattern.compile( "([^ ]+)[ ]+([^ ]+)[ ]+([^ ]+)" );

    private List<BlameLine> lines = new ArrayList<BlameLine>();

    public TfsBlameConsumer( ScmLogger logger )
    {
        super( logger );
    }

    public void consumeLine( String line )
    {
        Matcher matcher = LINE_PATTERN.matcher( line );
        if ( matcher.find() )
        {
            String revision = matcher.group( 1 ).trim();
            String author = matcher.group( 2 ).trim();
            String dateStr = matcher.group( 3 ).trim();

            Date date = parseDate( dateStr, null, TFS_TIMESTAMP_PATTERN );

            lines.add( new BlameLine( date, revision, author ) );
        }
    }

    public List<BlameLine> getLines()
    {
        return lines;
    }
}
