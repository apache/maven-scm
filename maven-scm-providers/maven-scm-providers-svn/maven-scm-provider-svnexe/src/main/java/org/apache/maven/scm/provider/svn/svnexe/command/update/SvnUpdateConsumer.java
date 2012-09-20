package org.apache.maven.scm.provider.svn.svnexe.command.update;

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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.scm.ChangeFile;
import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.svn.svnexe.command.AbstractFileCheckingConsumer;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 *
 */
public class SvnUpdateConsumer
    extends AbstractFileCheckingConsumer
{
    private static final String UPDATED_TO_REVISION_TOKEN = "Updated to revision";

    private static final String AT_REVISION_TOKEN = "At revision";

    private static final String EXPORTED_REVISION_TOKEN = "Exported revision";

    private static final String RESTORED_TOKEN = "Restored";
    
    private List<ChangeSet> changeSets = new ArrayList<ChangeSet>();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public SvnUpdateConsumer( ScmLogger logger, File workingDirectory )
    {
        super( logger, workingDirectory );
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    protected void parseLine( String line )
    {
        line = line.trim();

        String statusString = line.substring( 0, 1 );

        String file = line.substring( 3 ).trim();
        //[SCM-368]
        if ( file.startsWith( workingDirectory.getAbsolutePath() ) )
        {
            if ( file.length() == workingDirectory.getAbsolutePath().length() )
            {
                file = ".";
            }
            else
            {
                file = file.substring( this.workingDirectory.getAbsolutePath().length() + 1 );
            }
        }

        ScmFileStatus status;

        if ( line.startsWith( UPDATED_TO_REVISION_TOKEN ) )
        {
            String revisionString = line.substring( UPDATED_TO_REVISION_TOKEN.length() + 1, line.length() - 1 );

            revision = parseInt( revisionString );

            return;
        }
        else if ( line.startsWith( AT_REVISION_TOKEN ) )
        {
            String revisionString = line.substring( AT_REVISION_TOKEN.length() + 1, line.length() - 1 );

            revision = parseInt( revisionString );

            return;
        }
        else if ( line.startsWith( EXPORTED_REVISION_TOKEN ) )
        {
            String revisionString = line.substring( EXPORTED_REVISION_TOKEN.length() + 1, line.length() - 1 );

            revision = parseInt( revisionString );

            return;
        }
        else if ( line.startsWith( RESTORED_TOKEN ) )
        {
            return;
        }
        else if ( statusString.equals( "A" ) )
        {
            status = ScmFileStatus.ADDED;
        }
        else if ( statusString.equals( "U" ) || statusString.equals( "M" ) )
        {
            status = ScmFileStatus.UPDATED;
        }
        else if ( statusString.equals( "D" ) )
        {
            status = ScmFileStatus.DELETED;
        }
        else
        {
            //Do nothing

            return;
        }

        addFile( new ScmFile( file, status ) );
        
        List<ChangeFile>
        changeFiles =
            Arrays.asList( new ChangeFile[] { new ChangeFile( line, Integer.valueOf( revision ).toString() ) } );

        ChangeSet changeSet = new ChangeSet( null, null, null, changeFiles );
        changeSets.add( changeSet );
    }

    public List<ScmFile> getUpdatedFiles()
    {
        return getFiles();
    }

    public List<ChangeSet> getChangeSets()
    {
        return changeSets;
    }

    public void setChangeSets( List<ChangeSet> changeSets )
    {
        this.changeSets = changeSets;
    }
}
