package org.apache.maven.scm.provider.clearcase.command.update;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.clearcase.util.ClearCaseUtil;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 * @version $Id$
 */
public class ClearCaseUpdateConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private List updatedFiles = new ArrayList();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public ClearCaseUpdateConsumer( ScmLogger logger )
    {
        this.logger = logger;
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
        if ( line.indexOf( ClearCaseUtil.getLocalizedResource( "loading" ) ) > -1 )
        {
            int beginIndex = line.indexOf( '"' );
            if ( beginIndex != -1 )
            {
                String fileName = line.substring( beginIndex + 1, line.indexOf( '"', beginIndex + 1 ) );
                updatedFiles.add( new ScmFile( fileName, ScmFileStatus.UPDATED ) );
            }
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public List getUpdatedFiles()
    {
        return updatedFiles;
    }
}
