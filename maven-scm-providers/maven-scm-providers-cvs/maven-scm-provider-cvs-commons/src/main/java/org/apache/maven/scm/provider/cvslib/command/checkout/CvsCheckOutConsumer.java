package org.apache.maven.scm.provider.cvslib.command.checkout;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsCheckOutConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private List files = new ArrayList();

    public CvsCheckOutConsumer( ScmLogger logger )
    {
        this.logger = logger;
    }

    public void consumeLine( String line )
    {
        logger.debug( line );

        if ( line.length() < 3 )
        {
            if ( StringUtils.isNotEmpty( line ) )
            {
                logger.warn(
                    "Unable to parse output from command: line length must be bigger than 3. (" + line + ")." );
            }
            return;
        }

        String status = line.substring( 0, 2 );

        String file = line.substring( 2 );

        file = file.substring( file.indexOf( '/' ) );

        if ( status.equals( "U " ) )
        {
            files.add( new ScmFile( file, ScmFileStatus.UPDATED ) );
        }
        else if ( status.equals( "P " ) )
        {
            files.add( new ScmFile( file, ScmFileStatus.PATCHED ) );
        }
        else if ( status.equals( "C " ) )
        {
            files.add( new ScmFile( file, ScmFileStatus.CONFLICT ) );
        }
        else
        {
            logger.warn( "Unknown status: '" + status + "'." );
        }
    }

    public List getCheckedOutFiles()
    {
        return files;
    }
}
