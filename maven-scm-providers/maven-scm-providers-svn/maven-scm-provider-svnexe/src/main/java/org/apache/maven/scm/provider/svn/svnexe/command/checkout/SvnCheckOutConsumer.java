package org.apache.maven.scm.provider.svn.svnexe.command.checkout;

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
import org.apache.maven.scm.provider.svn.svnexe.command.AbstractFileCheckingConsumer;

import java.io.File;
import java.util.List;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SvnCheckOutConsumer
    extends AbstractFileCheckingConsumer
{
    private final static String CHECKED_OUT_REVISION_TOKEN = "Checked out revision";

    public SvnCheckOutConsumer( ScmLogger logger, File workingDirectory )
    {
        super( logger, workingDirectory );
    }

    protected void parseLine( String line )
    {
        String statusString = line.substring( 0, 1 );

        String file = line.substring( 3 ).trim();

        ScmFileStatus status;

        if ( line.startsWith( CHECKED_OUT_REVISION_TOKEN ) )
        {
            String revisionString = line.substring( CHECKED_OUT_REVISION_TOKEN.length() + 1, line.length() - 1 );

            revision = parseInt( revisionString );

            return;
        }
        else if ( statusString.equals( "A" ) )
        {
            status = ScmFileStatus.ADDED;
        }
        else if ( statusString.equals( "U" ) )
        {
            status = ScmFileStatus.UPDATED;
        }
        else
        {
            logger.info( "Unknown file status: '" + statusString + "'." );

            return;
        }

        addFile( new ScmFile( file, status ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List getCheckedOutFiles()
    {
        return getFiles();
    }
}
