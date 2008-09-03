package org.apache.maven.scm.provider.cvslib.command.list;

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

import java.util.LinkedList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * Parses CVS/Entries format, for example, like
 *
 * <pre>
 * /checkoutlist/1.9/Wed Jan 26 19:08:06 2005/-kkv/
 * /commitinfo/1.10/Tue Jan 11 01:25:34 2005/-kkv/
 * /config/1.15/Sun Jan 23 02:15:57 2005/-kkv/
 * D/directory1////
 * D/directory2////
 * </pre>
 *
 * @author <a href="mailto:szakusov@emdev.ru">Sergey Zakusov</a>: implemented to fix "Unknown file status" problem
 * @version $Id$
 */
public class CvsListConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private List entries;

    /**
     * @param logger is a logger
     */
    public CvsListConsumer( ScmLogger logger )
    {
        this.logger = logger;
        this.entries = new LinkedList();
    }

    /** {@inheritDoc} */
    public void consumeLine( String i_line )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( i_line );
        }

        String[] params = i_line.split( "/" );
        if ( params.length < 2 )
        {
            if ( StringUtils.isNotEmpty( i_line ) )
            {
                if ( logger.isWarnEnabled() )
                {
                    logger.warn( "Unable to parse it as CVS/Entries format: " + i_line + "." );
                }
            }
        }
        else
        {
            entries.add( new ScmFile( params[1], ScmFileStatus.UNKNOWN ) );
        }
    }

    /**
     * @return Parse result
     */
    public List getEntries()
    {
        return entries;
    }
}
