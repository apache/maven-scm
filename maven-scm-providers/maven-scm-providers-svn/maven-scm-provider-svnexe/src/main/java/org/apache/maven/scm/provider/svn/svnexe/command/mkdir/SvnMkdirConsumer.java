package org.apache.maven.scm.provider.svn.svnexe.command.mkdir;

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
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author <a href="mailto:oching@apache.org">Maria Odea Ching</a>
 * @version $Id$
 */
public class SvnMkdirConsumer
    implements StreamConsumer
{
    private ScmLogger logger;

    private static final String COMMITTED_REVISION_TOKEN = "Committed revision";

    private int revision;
    
    private List<ScmFile> createdDirs = new ArrayList<ScmFile>();
    
    public SvnMkdirConsumer( ScmLogger logger )
    {
        this.logger = logger;
    }
    
    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( StringUtils.isBlank( line ) )
        {
            return;
        }
        
        String statusString = line.substring( 0, 1 );
        ScmFileStatus status;
       
        if ( line.startsWith( COMMITTED_REVISION_TOKEN ) )
        {
            String revisionString = line.substring( COMMITTED_REVISION_TOKEN.length() + 1, line.length() - 1 );

            revision = Integer.parseInt( revisionString );
            
            return;
        }
        else if( statusString.equals( "A" ) )
        {
            String file = line.substring( 3 );
            
            status = ScmFileStatus.ADDED;
            
            createdDirs.add( new ScmFile( file, status ) );
        }        
        else
        {
            if ( logger.isInfoEnabled() )
            {
                logger.info( "Unknown line: '" + line + "'" );
            }

            return;
        }
    }

    public int getRevision()
    {
        return revision;
    }
    
    public List<ScmFile> getCreatedDirs()
    {
        return createdDirs;
    }
}
