package org.apache.maven.scm.provider.cvslib.command.checkin;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * ====================================================================
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;

import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsCheckInConsumer
    implements StreamConsumer
{
    private List checkedInFiles = new ArrayList();

    private String remotePath;

    public CvsCheckInConsumer( String remotePath )
    {
        this.remotePath = remotePath;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    public void consumeLine( String line )
    {
        /*
         * The output from "cvs commit" contains lines like this:
         *
         *   /path/rot/repo/test-repo/check-in/foo/bar,v  <--  bar
         * 
         * so this code assumes that it if starts with "/" and contains ",v  <--  "
         * it's a committed file.
         */

        if ( !line.startsWith( "/" ) )
        {
            return;
        }

        int end = line.indexOf( ",v  <--  " );

        if ( end == -1 )
        {
            return;
        }

        String fileName = line.substring( 0, end );

        if ( !fileName.startsWith( remotePath ) )
        {
            return;
        }

        fileName = fileName.substring( remotePath.length() );

        checkedInFiles.add( new ScmFile( fileName, ScmFileStatus.CHECKED_IN ) );
    }

    public List getCheckedInFiles()
    {
        return checkedInFiles;
    }
}
