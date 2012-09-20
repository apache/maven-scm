package org.apache.maven.scm.provider.clearcase.command.status;

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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 *
 */
public class ClearCaseStatusConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private File workingDirectory;

    private List<ScmFile> checkedOutFiles = new ArrayList<ScmFile>();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public ClearCaseStatusConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;
        this.workingDirectory = workingDirectory;
    }

    // ----------------------------------------------------------------------
    // Stream Consumer Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( line );
        }
        checkedOutFiles.add(
            new ScmFile( workingDirectory.getAbsolutePath() + line.substring( 1 ), ScmFileStatus.CHECKED_OUT ) );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List<ScmFile> getCheckedOutFiles()
    {
        return checkedOutFiles;
    }
}
