package org.apache.maven.scm.provider.cvslib.command.checkout;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.List;
import java.io.File;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;

import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.logging.Logger;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsCheckOutConsumer
    implements StreamConsumer
{
    private Logger logger;

    private String workingDirectory;

    private List files = new ArrayList();

    public CvsCheckOutConsumer( Logger logger, File workingDirectory )
    {
        this.logger = logger;

        this.workingDirectory = workingDirectory.getAbsolutePath();
    }

    public void consumeLine( String line )
    {
        if ( line.length() < 3 )
        {
            logger.warn( "Unable to parse output from command: line length must be bigger than 3." );
        }

        String status = line.substring( 0, 2 );

        String file = line.substring( 2 );

        if ( file.startsWith( workingDirectory ) )
        {
            file = file.substring( workingDirectory.length() );
        }

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
