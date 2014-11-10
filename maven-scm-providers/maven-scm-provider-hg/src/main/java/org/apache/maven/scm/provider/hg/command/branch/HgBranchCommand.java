package org.apache.maven.scm.provider.hg.command.branch;

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

import org.apache.maven.scm.ScmBranchParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.branch.AbstractBranchCommand;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.HgUtils;
import org.apache.maven.scm.provider.hg.command.HgCommandConstants;
import org.apache.maven.scm.provider.hg.command.HgConsumer;
import org.apache.maven.scm.provider.hg.command.inventory.HgListConsumer;
import org.apache.maven.scm.provider.hg.repository.HgScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;

/**
 * Branch. Mercurial has weird branches. After a branch is created, it must be committed to the server, otherwise
 * the branch does not exist (yet) in the repository.
 *
 * @author Henning Schmiedehausen
 *
 */
public class HgBranchCommand
    extends AbstractBranchCommand
    implements Command
{

    protected ScmResult executeBranchCommand( ScmProviderRepository scmProviderRepository, ScmFileSet fileSet,
                                              String branch, String message )
        throws ScmException
    {
        return executeBranchCommand( scmProviderRepository, fileSet, branch, new ScmBranchParameters( message ) );
    }

    /**
     * {@inheritDoc}
     */
    protected ScmResult executeBranchCommand( ScmProviderRepository scmProviderRepository, ScmFileSet fileSet,
                                              String branch, ScmBranchParameters scmBranchParameters )
        throws ScmException
    {

        if ( StringUtils.isBlank( branch ) )
        {
            throw new ScmException( "branch must be specified" );
        }

        if ( !fileSet.getFileList().isEmpty() )
        {
            throw new ScmException( "This provider doesn't support branchging subsets of a directory" );
        }

        File workingDir = fileSet.getBasedir();

        // build the command
        String[] branchCmd = new String[] { HgCommandConstants.BRANCH_CMD, branch };

        // keep the command about in string form for reporting
        HgConsumer branchConsumer = new HgConsumer( getLogger() )
        {
            public void doConsume( ScmFileStatus status, String trimmedLine )
            {
                // noop
            }
        };

        ScmResult result = HgUtils.execute( branchConsumer, getLogger(), workingDir, branchCmd );
        HgScmProviderRepository repository = (HgScmProviderRepository) scmProviderRepository;

        if ( !result.isSuccess() )
        {
            throw new ScmException( "Error while executing command " + joinCmd( branchCmd ) );
        }

        // First commit.
        String[] commitCmd =
            new String[] { HgCommandConstants.COMMIT_CMD, HgCommandConstants.MESSAGE_OPTION,
                scmBranchParameters.getMessage() };

        result = HgUtils.execute( new HgConsumer( getLogger() ), getLogger(), workingDir, commitCmd );

        if ( !result.isSuccess() )
        {
            throw new ScmException( "Error while executing command " + joinCmd( commitCmd ) );
        }

        // now push, if we should.

        if ( repository.isPushChanges() )
        {
            if ( !repository.getURI().equals( fileSet.getBasedir().getAbsolutePath() ) )
            {

                String[] pushCmd = new String[] {
                    HgCommandConstants.PUSH_CMD,
                    HgCommandConstants.NEW_BRANCH_OPTION,
                    repository.getURI()
                };

                result = HgUtils.execute( new HgConsumer( getLogger() ), getLogger(), fileSet.getBasedir(), pushCmd );

                if ( !result.isSuccess() )
                {
                    throw new ScmException( "Error while executing command " + joinCmd( pushCmd ) );
                }
            }
        }

        // do an inventory to return the files branched (all of them)
        String[] listCmd = new String[]{ HgCommandConstants.INVENTORY_CMD };
        HgListConsumer listconsumer = new HgListConsumer( getLogger() );

        result = HgUtils.execute( listconsumer, getLogger(), fileSet.getBasedir(), listCmd );

        if ( !result.isSuccess() )
        {
            throw new ScmException( "Error while executing command " + joinCmd( listCmd ) );
        }

        List<ScmFile> files = listconsumer.getFiles();
        List<ScmFile> fileList = new ArrayList<ScmFile>();
        for ( ScmFile f : files )
        {
            fileList.add( new ScmFile( f.getPath(), ScmFileStatus.TAGGED ) );
        }

        return new BranchScmResult( fileList, result );
    }

    private String joinCmd( String[] cmd )
    {
        StringBuilder result = new StringBuilder();
        for ( int i = 0; i < cmd.length; i++ )
        {
            String s = cmd[i];
            result.append( s );
            if ( i < cmd.length - 1 )
            {
                result.append( " " );
            }
        }
        return result.toString();
    }
}
