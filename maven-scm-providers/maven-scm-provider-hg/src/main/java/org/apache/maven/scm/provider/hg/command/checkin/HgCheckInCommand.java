package org.apache.maven.scm.provider.hg.command.checkin;

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
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.command.status.HgStatusCommand;
import org.apache.maven.scm.provider.hg.repository.HgScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class HgCheckInCommand
    extends AbstractCheckInCommand
{
    /**
     * {@inheritDoc}
     */
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      ScmVersion tag )
        throws ScmException
    {
        if ( tag != null && !StringUtils.isEmpty( tag.getName() ) )
        {
            throw new ScmException( "This provider can't handle tags for this operation" );
        }


        File workingDir = fileSet.getBasedir();
        String branchName = HgUtils.getCurrentBranchName( getLogger(), workingDir );
        boolean differentOutgoingBranch = repo.isPushChanges() ? HgUtils.differentOutgoingBranchFound( getLogger(), workingDir, branchName ) : false;

        // Get files that will be committed (if not specified in fileSet)
        List<ScmFile> commitedFiles = new ArrayList<ScmFile>();
        List<File> files = fileSet.getFileList();
        if ( files.isEmpty() )
        { //Either commit all changes
            HgStatusCommand statusCmd = new HgStatusCommand();
            statusCmd.setLogger( getLogger() );
            StatusScmResult status = statusCmd.executeStatusCommand( repo, fileSet );
            List<ScmFile> statusFiles = status.getChangedFiles();
            for ( ScmFile file : statusFiles )
            {
                if ( file.getStatus() == ScmFileStatus.ADDED || file.getStatus() == ScmFileStatus.DELETED ||
                    file.getStatus() == ScmFileStatus.MODIFIED )
                {
                    commitedFiles.add( new ScmFile( file.getPath(), ScmFileStatus.CHECKED_IN ) );
                }
            }

        }
        else
        { //Or commit spesific files
            for ( File file : files )
            {
                commitedFiles.add( new ScmFile( file.getPath(), ScmFileStatus.CHECKED_IN ) );
            }
        }

        // Commit to local branch
        String[] commitCmd = new String[]{ HgCommandConstants.COMMIT_CMD, HgCommandConstants.MESSAGE_OPTION, message };
        commitCmd = HgUtils.expandCommandLine( commitCmd, fileSet );
        ScmResult result =
            HgUtils.execute( new HgConsumer( getLogger() ), getLogger(), fileSet.getBasedir(), commitCmd );

        // Push to parent branch if any
        HgScmProviderRepository repository = (HgScmProviderRepository) repo;

        if ( repo.isPushChanges() )
        {

            if ( !repository.getURI().equals( fileSet.getBasedir().getAbsolutePath() ) )
            {
                String[] pushCmd = new String[]{ HgCommandConstants.PUSH_CMD,
                    differentOutgoingBranch ? HgCommandConstants.REVISION_OPTION + branchName : null,
                    repository.getURI() };

                result = HgUtils.execute( new HgConsumer( getLogger() ), getLogger(), fileSet.getBasedir(), pushCmd );
            }

            return new CheckInScmResult( commitedFiles, result );
        }

        return new CheckInScmResult( commitedFiles, result );
    }


}
