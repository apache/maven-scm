package org.apache.maven.scm.provider.hg.command.blame;

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

import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.command.HgConsumer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 * @author Olivier Lamy
 * @since 1.4
 */
public class HgBlameConsumer
    extends HgConsumer
{
    private List<BlameLine> lines = new ArrayList<BlameLine>();

    private static final String HG_TIMESTAMP_PATTERN = "EEE MMM dd HH:mm:ss yyyy Z";

    public HgBlameConsumer( ScmLogger logger )
    {
        super( logger );

    }

    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        /* godin 0 Sun Jan 31 03:04:54 2010 +0300 */
        String annotation;
        if(trimmedLine.indexOf(": ") > -1)
        {
            annotation = trimmedLine.substring(0, trimmedLine.indexOf(": ")).trim();
        } else {
            annotation = trimmedLine.substring(0, trimmedLine.lastIndexOf(":")).trim();
        }

        String author = annotation.substring( 0, annotation.indexOf( ' ' ) );
        annotation = annotation.substring( annotation.indexOf( ' ' ) + 1 ).trim();

        String revision = annotation.substring( 0, annotation.indexOf( ' ' ) );
        annotation = annotation.substring( annotation.indexOf( ' ' ) + 1 ).trim();

        String dateStr = annotation;
        Date dateTime = parseDate( dateStr, null, HG_TIMESTAMP_PATTERN );

        lines.add( new BlameLine( dateTime, revision, author ) );
    }

    public List<BlameLine> getLines()
    {
        return lines;
    }
}
