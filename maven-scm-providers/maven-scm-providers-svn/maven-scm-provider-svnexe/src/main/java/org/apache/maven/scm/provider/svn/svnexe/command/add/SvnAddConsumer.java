package org.apache.maven.scm.provider.svn.svnexe.command.add;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SvnAddConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private List addedFiles = new ArrayList();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public SvnAddConsumer( ScmLogger logger )
    {
        this.logger = logger;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    public void consumeLine( String line )
    {
        if ( line.length() <= 3 )
        {
            logger.warn( "Unexpected input, the line must be at least three characters long. Line: '" + line + "'." );

            return;
        }

        String statusString = line.substring( 0, 1 );

        String file = line.substring( 3 );

        ScmFileStatus status;

        if ( statusString.equals( "A" ) )
        {
            status = ScmFileStatus.ADDED;
        }
        else
        {
            logger.info( "Unknown file status: '" + statusString + "'." );

            return;
        }

        addedFiles.add( new ScmFile( file, status ) );
    }

    public List getAddedFiles()
    {
        return addedFiles;
    }

}
