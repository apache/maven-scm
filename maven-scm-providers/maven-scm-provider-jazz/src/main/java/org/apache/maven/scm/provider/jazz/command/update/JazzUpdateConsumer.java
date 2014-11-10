package org.apache.maven.scm.provider.jazz.command.update;

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
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.consumer.AbstractRepositoryConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Consume the output of the scm command for the "acept" operation.
 *
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzUpdateConsumer
    extends AbstractRepositoryConsumer
{
    /**
     * The "Update" command status flag for a resource that has been added.
     */
    public static final String UPDATE_CMD_ADD_FLAG = "-a-";

    /**
     * The "Update" command status flag for when the content or properties of a file have been modified, or the
     * properties of a directory have changed.
     */
    public static final String UPDATE_CMD_CHANGE_FLAG = "--c";

    /**
     * The "Update" command status flag for a resource that has been deleted.
     */
    public static final String UPDATE_CMD_DELETE_FLAG = "-d-";

    /**
     * The "Update" command status flag for a resource that has been renamed or moved.
     */
    public static final String UPDATE_CMD_MOVED_FLAG = "-m-";

    private List<ScmFile> fUpdatedFiles = new ArrayList<ScmFile>();

    /**
     * Construct the JazzUpdateCommand consumer.
     *
     * @param repository The repository we are working with.
     * @param logger     The logger to use.
     */
    public JazzUpdateConsumer( ScmProviderRepository repository, ScmLogger logger )
    {
        super( repository, logger );
    }

    /**
     * Process one line of output from the execution of the "scm xxxx" command.
     *
     * @param line The line of output from the external command that has been pumped to us.
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        super.consumeLine( line );
        if ( containsStatusFlag( line ) )
        {
            extractUpdatedFile( line );
        }
    }

    private boolean containsStatusFlag( String line )
    {
        boolean containsStatusFlag = false;

        if ( line.trim().length() > 3 )
        {
            String flag = line.trim().substring( 0, 3 );
            if ( UPDATE_CMD_ADD_FLAG.equals( flag ) || UPDATE_CMD_CHANGE_FLAG.equals( flag )
                || UPDATE_CMD_DELETE_FLAG.equals( flag ) || UPDATE_CMD_MOVED_FLAG.equals( flag ) )
            {
                containsStatusFlag = true;
            }
        }
        return containsStatusFlag;
    }

    private void extractUpdatedFile( String line )
    {
        String filePath = "";
        String flag = line.trim().substring( 0, 3 );
        ScmFileStatus status = ScmFileStatus.UNKNOWN;

        if ( UPDATE_CMD_ADD_FLAG.equals( flag ) )
        {
            status = ScmFileStatus.ADDED;
            filePath = line.trim().substring( 4 );
        }

        if ( UPDATE_CMD_CHANGE_FLAG.equals( flag ) )
        {
            status = ScmFileStatus.UPDATED;
            filePath = line.trim().substring( 4 );
        }

        if ( UPDATE_CMD_DELETE_FLAG.equals( flag ) )
        {
            status = ScmFileStatus.DELETED;
            filePath = line.trim().substring( 4 );
        }

        // TODO - It looks like there is a defect in RTC 2.0 SCM for moved (the "moved from <path> is not right)
        // TODO - Also it looks like if you rename a file, the output shows the old name as changed, however it should
        // be marked as deleted. (see if this is the case in RTC 3.0)
        if ( UPDATE_CMD_MOVED_FLAG.equals( flag ) )
        {
            status = ScmFileStatus.ADDED;
            String pattern = "^" + UPDATE_CMD_MOVED_FLAG + "\\s(.*)\\s\\(moved\\sfrom\\s.*$";
            Pattern r = Pattern.compile( pattern );
            Matcher m = r.matcher( line.trim() );
            if ( m.find() )
            {
                filePath = m.group( 1 );
            }
        }

        fUpdatedFiles.add( new ScmFile( filePath, status ) );
    }

    public List<ScmFile> getUpdatedFiles()
    {
        return fUpdatedFiles;
    }

}
