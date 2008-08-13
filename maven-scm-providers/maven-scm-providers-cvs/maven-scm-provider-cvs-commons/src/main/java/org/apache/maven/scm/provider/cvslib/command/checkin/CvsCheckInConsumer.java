package org.apache.maven.scm.provider.cvslib.command.checkin;

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
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsCheckInConsumer
    implements StreamConsumer
{
    private List checkedInFiles = new ArrayList();

    private String remotePath;

    private ScmLogger logger;

    public CvsCheckInConsumer( String remotePath, ScmLogger logger )
    {
        this.remotePath = remotePath;

        this.logger = logger;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        /*
         * The output from "cvs commit" contains lines like this:
         *
         *   /path/rot/repo/test-repo/check-in/foo/bar,v  <--  bar
         *
         * so this code assumes that it contains ",v  <--  "
         * it's a committed file.
         */

        logger.debug( line );

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
