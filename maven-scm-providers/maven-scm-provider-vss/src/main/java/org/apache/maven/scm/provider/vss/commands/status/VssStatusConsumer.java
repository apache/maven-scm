package org.apache.maven.scm.provider.vss.commands.status;

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
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.apache.maven.scm.util.AbstractConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * @author <a href="mailto:triek@thrx.de">Thorsten Riek</a>
 * @version $Id$
 */
public class VssStatusConsumer
    extends AbstractConsumer
    implements StreamConsumer
{

    /**
     * expecting file information
     */
    private static final int DIFF_UNKNOWN = 0;

    /**
     * expecting files to checkin
     */
    private static final int DIFF_LOCAL_FILES_NOT_IN_PROJECT = 1;

    /**
     * expecting commit
     */
    private static final int DIFF_VSS_FILES_DIFFERENT_FROM_LOCAL_FILES = 2;

    /**
     * expecting update / checkout
     */
    private static final int DIFF_VSS_FILES_NOT_IN_CURRENT_FOLDER = 3;

    /**
     * expecting setting akt remote folder
     */
    private static final int DIFF_START_DIFFING_REMOTE = 4;

    /**
     * expecting setting akt local folder
     */
    private static final int DIFF_START_DIFFING_LOCAL = 5;

    /**
     * Marks Diffing remote project folder
     */
    private static final String START_DIFFING_REMOTE = "Diffing:";


    /**
     * Marks Diffing local project folder
     */
    private static final String START_DIFFING_LOCAL = "Against:";

    /**
     * Marks Local files not in the current project
     */
    private static final String LOCAL_FILES_NOT_IN_PROJECT = "Local files not in the current project:";

    /**
     * Marks SourceSafe files different from local files
     */
    private static final String VSS_FILES_DIFFERENT_FROM_LOCAL_FILES = "SourceSafe files different from local files:";

    /**
     * Marks SourceSafe files not in the current folder
     */
    private static final String VSS_FILES_NOT_IN_CURRENT_FOLDER = "SourceSafe files not in the current folder:";

    private String remoteProjectFolder = "";

    private String localFolder = "";

    private int lastState = 0;

    private List updatedFiles = new ArrayList();

    private VssScmProviderRepository repo;

    private ScmFileSet fileSet;

    public VssStatusConsumer( VssScmProviderRepository repo, ScmLogger logger, ScmFileSet fileSet )
    {
        super( logger );
        this.repo = repo;
        this.fileSet = fileSet;
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( line );
        }

        switch ( getLineStatus( line ) )
        {
            case DIFF_LOCAL_FILES_NOT_IN_PROJECT:
                lastState = DIFF_LOCAL_FILES_NOT_IN_PROJECT;
                break;
            case DIFF_VSS_FILES_DIFFERENT_FROM_LOCAL_FILES:
                lastState = DIFF_VSS_FILES_DIFFERENT_FROM_LOCAL_FILES;
                break;
            case DIFF_VSS_FILES_NOT_IN_CURRENT_FOLDER:
                lastState = DIFF_VSS_FILES_NOT_IN_CURRENT_FOLDER;
                break;
            case DIFF_START_DIFFING_LOCAL:
            	lastState = DIFF_START_DIFFING_LOCAL;
                processLocalFolder( line );
                break;
            case DIFF_START_DIFFING_REMOTE:
            	lastState = DIFF_START_DIFFING_REMOTE;
                processRemoteProjectFolder( line );
                break;
            default:
                processLastStateFiles( line );
                break;
        }
    }

    /**
     * Process the current input line in the Get File state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processLastStateFiles( String line )
    {

        if ( line != null && line.trim().length() > 0 )
        {
            if ( lastState == DIFF_START_DIFFING_LOCAL ) {
            	setLocalFolder(localFolder + line);
            	getLogger().debug("Local folder: " + localFolder);
            } else if ( lastState == DIFF_START_DIFFING_REMOTE ) {
            	setRemoteProjectFolder(remoteProjectFolder + line);            	
            	getLogger().debug("Remote folder: " + localFolder);
            }
        	
            String[] fileLine = line.split( " " );
            for ( int i = 0; i < fileLine.length; i++ )
            {
                if ( fileLine[i].trim().length() > 0 )
                {
                    if ( lastState == DIFF_LOCAL_FILES_NOT_IN_PROJECT )
                    {
                        updatedFiles.add( new ScmFile( localFolder + fileLine[i], ScmFileStatus.ADDED ) );
                    }
                    else if ( lastState == DIFF_VSS_FILES_NOT_IN_CURRENT_FOLDER )
                    {
                        updatedFiles.add( new ScmFile( localFolder + fileLine[i], ScmFileStatus.UPDATED ) );
                    }
                    else if ( lastState == DIFF_VSS_FILES_DIFFERENT_FROM_LOCAL_FILES )
                    {
                        updatedFiles.add( new ScmFile( localFolder + fileLine[i], ScmFileStatus.MODIFIED ) );
                    }

                    if ( getLogger().isDebugEnabled() )
                    {
                        getLogger().debug( localFolder + fileLine[i] );
                    }
                }
            }
        }
        else
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "processLastStateFiles:  empty line" );
            }
        }

    }

    /**
     * Process the current input line in the Get File Path state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processLocalFolder( String line )
    {

    	setLocalFolder( line.substring( START_DIFFING_LOCAL.length() ).trim() );

    }

    /**
     * Process the current input line in the Get File Path state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processRemoteProjectFolder( String line )
    {

    	setRemoteProjectFolder( line.substring( START_DIFFING_REMOTE.length() ).trim() );

    }

    /**
     * Identify the status of a vss get line
     *
     * @param line The line to process
     * @return status
     */
    private int getLineStatus( String line )
    {
        int argument = DIFF_UNKNOWN;
        if ( line.startsWith( LOCAL_FILES_NOT_IN_PROJECT ) )
        {
            argument = DIFF_LOCAL_FILES_NOT_IN_PROJECT;
        }
        else if ( line.startsWith( VSS_FILES_DIFFERENT_FROM_LOCAL_FILES ) )
        {
            argument = DIFF_VSS_FILES_DIFFERENT_FROM_LOCAL_FILES;
        }
        else if ( line.startsWith( VSS_FILES_NOT_IN_CURRENT_FOLDER ) )
        {
            argument = DIFF_VSS_FILES_NOT_IN_CURRENT_FOLDER;
        }
        //        else if ( line.startsWith( VSS_FILES_NOT_IN_CURRENT_FOLDER ) )
        //        {
        //            Project $/com.fum/fum-utilities/src/main/java/com/fum/utilities/protocol has no
        //            corresponding folder
        //            argument = DIFF_VSS_FILES_NOT_IN_CURRENT_FOLDER;
        //        }
        else if ( line.startsWith( START_DIFFING_LOCAL ) )
        {
            argument = DIFF_START_DIFFING_LOCAL;
        }
        else if ( line.startsWith( START_DIFFING_REMOTE ) )
        {
            argument = DIFF_START_DIFFING_REMOTE;
        }

        return argument;
    }

    public List getUpdatedFiles()
    {
        return updatedFiles;
    }

    private void setLocalFolder( String localFolder )
    {
        if ( localFolder != null && localFolder.trim().length() > 0 )
        {
            this.localFolder = localFolder.replace( java.io.File.separatorChar, '/' ) + "/";
        }
        else
        {
            this.localFolder = "";
        }
    }

    private void setRemoteProjectFolder( String remoteProjectFolder )
    {
        this.remoteProjectFolder = remoteProjectFolder;
    }

}
