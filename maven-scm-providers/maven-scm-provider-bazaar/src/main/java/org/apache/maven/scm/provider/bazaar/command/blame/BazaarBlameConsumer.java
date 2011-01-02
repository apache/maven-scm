package org.apache.maven.scm.provider.bazaar.command.blame;

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
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Evgeny Mandrikov
 * @author Olivier Lamy
 * @since 1.4
 */
public class BazaarBlameConsumer
    extends BazaarConsumer
{
    private static final String BAZAAR_TIMESTAMP_PATTERN = "yyyyMMdd";

    private List<BlameLine> lines = new ArrayList<BlameLine>();

    public BazaarBlameConsumer( ScmLogger logger )
    {
        super( logger );
    }

    public void doConsume( ScmFileStatus status, String trimmedLine )
    {
        /*1   godin@godin 20100131*/
        String annotation = trimmedLine.substring( 0, trimmedLine.indexOf( '|' ) ).trim();

        String dateStr = annotation.substring( annotation.lastIndexOf( ' ' ) + 1 );
        annotation = annotation.substring( 0, annotation.lastIndexOf( ' ' ) );

        String author = annotation.substring( annotation.lastIndexOf( ' ' ) + 1 );
        annotation = annotation.substring( 0, annotation.lastIndexOf( ' ' ) );

        String revision = annotation.trim();

        Date date = parseDate( dateStr, null, BAZAAR_TIMESTAMP_PATTERN );

        lines.add( new BlameLine( date, revision, author ) );
    }

    public List<BlameLine> getLines()
    {
        return lines;
    }
}
