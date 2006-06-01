package org.apache.maven.scm.provider.vss.commands.status;

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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.vss.commands.VssConstants;
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
     * Marks start of file data
     */
    private static String START_FILE_PATH = "$/";

    /**
     * Marks Diffing remote project folder
     */
    private static final String START_DIFFING_REMOTE = "Diffing:";

    /**
     * Marks Diffing local project folder
     */
    private static final String START_DIFFING_LOCAL = "Against:";

    //    Diffing: $/com.fum/fum-utilities
    //    Against: D:\work\fum-utilities

    /**
     * Marks Local files not in the current project
     */
    private static final String LOCAL_FILES_NOT_IN_PROJECT = "Local files not in the current project:";

    //      .classpath  .project  Diff.txt  getVSS.xml  out.txt

    /**
     * Marks SourceSafe files different from local files
     */
    private static final String VSS_FILES_DIFFERENT_FROM_LOCAL_FILES = "SourceSafe files different from local files:";

    //      .classpath  .project  Diff.txt  getVSS.xml  out.txt

    /**
     * Marks SourceSafe files not in the current folder
     */
    private static final String VSS_FILES_NOT_IN_CURRENT_FOLDER = "SourceSafe files not in the current folder:";

    /**
     * Marks "Set the default folder for project" question
     */
    private static final String CONTAINS_SET_DEFAULT_WORKING_FOLDER = "as the default folder for project";

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

    public void consumeLine( String line )
    {

        switch ( getLineStatus( line ) )
        {
            case DIFF_LOCAL_FILES_NOT_IN_PROJECT:
                getLogger().debug( line );
                lastState = DIFF_LOCAL_FILES_NOT_IN_PROJECT;
                break;
            case DIFF_VSS_FILES_DIFFERENT_FROM_LOCAL_FILES:
                getLogger().debug( line );
                lastState = DIFF_VSS_FILES_DIFFERENT_FROM_LOCAL_FILES;
                break;
            case DIFF_VSS_FILES_NOT_IN_CURRENT_FOLDER:
                getLogger().debug( line );
                lastState = DIFF_VSS_FILES_NOT_IN_CURRENT_FOLDER;
                break;
            case DIFF_START_DIFFING_LOCAL:
                getLogger().debug( line );
                processLocalFolder( line );
                break;
            case DIFF_START_DIFFING_REMOTE:
                getLogger().debug( line );
                processRemoteProjectFolder( line );
                break;
            default:
                getLogger().debug( line );
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
                    getLogger().debug( localFolder + fileLine[i] );
                }
            }
        }
        else
        {
            getLogger().debug( "processLastStateFiles:  empty line" );
        }

    }

    /**
     * Process the current input line in the Get File Path state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processLocalFolder( String line )
    {

        int folderLength = ( START_DIFFING_LOCAL + " " + fileSet.getBasedir().getAbsolutePath() ).length();

        if ( folderLength < line.length() )
        {
            setLocalFolder( line.substring( folderLength, line.length() ) );
        }
        else
        {
            setLocalFolder( "" );
        }

    }

    /**
     * Process the current input line in the Get File Path state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processRemoteProjectFolder( String line )
    {

        int folderLength = ( START_DIFFING_REMOTE + " " + VssConstants.PROJECT_PREFIX + repo.getProject() ).length();

        if ( folderLength < line.length() )
        {
            setRemoteProjectFolder( line.substring( folderLength, line.length() ) );
        }
        else
        {
            setRemoteProjectFolder( "" );
        }

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
