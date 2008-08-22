package org.apache.maven.scm.provider.vss.commands.update;

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
import org.apache.maven.scm.provider.vss.commands.VssConstants;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.apache.maven.scm.util.AbstractConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:triek@thrx.de">Thorsten Riek</a>
 * @version $Id$
 */
public class VssUpdateConsumer
    extends AbstractConsumer
    implements StreamConsumer
{

    /**
     * expecting file information
     */
    private static final int GET_UNKNOWN = 0;

    /**
     * expecting file information
     */
    private static final int GET_FILE = 1;

    /**
     * expecting file information
     */
    private static final int REPLACE_FILE = 2;

    /**
     * expecting file path information
     */
    private static final int GET_FILE_PATH = 3;

    /**
     * expecting writable copy
     */
    private static final int IS_WRITABLE_COPY = 4;

    /**
     * expecting working folder
     */
    private static final int SET_WORKING_FOLDER = 5;

    /**
     * Marks start of file data
     */
    private static final String START_FILE_PATH = "$/";

    /**
     * Marks getting a new File
     */
    private static final String START_GETTING = "Getting";

    /**
     * Marks replacing a old File
     */
    private static final String START_REPLACING = "Replacing local copy of ";

    /**
     * Marks a writable copy of a File / maybe a conflict
     */
    private static final String START_WRITABLE_COPY = "A writable ";

    /**
     * Marks "Set the default folder for project" question
     */
    private static final String CONTAINS_SET_DEFAULT_WORKING_FOLDER = "as the default folder for project";

    private String currentPath = "";

    private List updatedFiles = new ArrayList();

    private VssScmProviderRepository repo;

    public VssUpdateConsumer( VssScmProviderRepository repo, ScmLogger logger )
    {
        super( logger );
        this.repo = repo;
    }

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        getLogger().debug( line );

        switch ( getLineStatus( line ) )
        {
            case GET_FILE_PATH:
                processGetFilePath( line );
                break;
            case GET_FILE:
                processGetFile( line );
                break;
            case REPLACE_FILE:
                processReplaceFile( line );
                break;
            case IS_WRITABLE_COPY:
                // FIXME is actually in error stream if command is build without -G-
                processWritableFile( line );
                break;
            case SET_WORKING_FOLDER:
                // to trash
                break;
            default:
                break;
        }
    }

    /**
     * Process the current input line in the Get File state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processGetFile( String line )
    {
        String[] fileLine = line.split( " " );
        updatedFiles.add( new ScmFile( currentPath + "/" + fileLine[1], ScmFileStatus.UPDATED ) );
        getLogger().info( fileLine[0] + ": " + currentPath + "/" + fileLine[1] );

    }

    /**
     * Process the current input line in the Replace File state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processReplaceFile( String line )
    {

        updatedFiles.add(
            new ScmFile( currentPath + "/" + line.substring( START_REPLACING.length() ), ScmFileStatus.UPDATED ) );
        getLogger().info( START_REPLACING + currentPath + "/" + line.substring( START_REPLACING.length() ) );


    }

    /**
     * Process the current input line in the Get File Path state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processGetFilePath( String line )
    {
        currentPath = line.substring( ( VssConstants.PROJECT_PREFIX + repo.getProject() ).length(), line.length() - 1 );
    }

    /**
     * Process the current input line in the writable File state.
     *
     * @param line a line of text from the VSS log output
     */
    private void processWritableFile( String line )
    {
        // FIXME extract file name
        //        String[] fileLine = line.split( " " );
        //        updatedFiles.add( new ScmFile( currentPath + "/" + fileLine[1], ScmFileStatus.MODIFIED ) );
        //        getLogger().info( fileLine[0] + ": " + currentPath + "/" + fileLine[1] );

    }

    /**
     * Identify the status of a vss get line
     *
     * @param line The line to process
     * @return status
     */
    private int getLineStatus( String line )
    {
        int argument = GET_UNKNOWN;
        if ( line.startsWith( START_FILE_PATH ) )
        {
            argument = GET_FILE_PATH;
        }
        else if ( line.startsWith( START_GETTING ) )
        {
            argument = GET_FILE;
        }
        else if ( line.startsWith( START_REPLACING ) )
        {
            argument = REPLACE_FILE;
        }
        else if ( line.startsWith( START_WRITABLE_COPY ) )
        {
            argument = IS_WRITABLE_COPY;
        }
        else if ( line.indexOf( CONTAINS_SET_DEFAULT_WORKING_FOLDER ) != -1 )
        {
            argument = SET_WORKING_FOLDER;
        }

        return argument;
    }

    public List getUpdatedFiles()
    {
        return updatedFiles;
    }

}
