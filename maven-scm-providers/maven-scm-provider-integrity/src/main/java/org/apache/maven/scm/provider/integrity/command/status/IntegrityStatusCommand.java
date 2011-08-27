package org.apache.maven.scm.provider.integrity.command.status;

/**
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

import com.mks.api.response.APIException;
import com.mks.api.response.Item;
import com.mks.api.response.WorkItem;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Sandbox;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * MKS Integrity implementation for Maven's AbstractStatusCommand
 * <br>This command will execute a 'si viewsandbox' and report on all
 * changed and dropped working files.  Additionally, this command
 * will also run a 'si viewnonmembers' command to report on all new
 * files added in the sandbox directory
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityStatusCommand.java 1.6 2011/08/22 13:06:36EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityStatusCommand
    extends AbstractStatusCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public StatusScmResult executeStatusCommand( ScmProviderRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        StatusScmResult result;
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        getLogger().info( "Status of files changed in sandbox " + fileSet.getBasedir().getAbsolutePath() );
        try
        {
            // Initialize the list of ScmFile objects for the StatusScmResult
            List<ScmFile> scmFileList = new ArrayList<ScmFile>();

            // Get a listing for all the changes in the sandbox
            Sandbox siSandbox = iRepo.getSandbox();
            // Get the excludes and includes list from the configuration
            String excludes = Sandbox.formatFilePatterns( fileSet.getExcludes() );
            String includes = Sandbox.formatFilePatterns( fileSet.getIncludes() );

            // Get the new members found in the sandbox
            List<ScmFile> newMemberList = siSandbox.getNewMembers( excludes, includes );
            // Update the scmFileList with our updates
            scmFileList.addAll( newMemberList );

            // Get the changed/dropped members from the sandbox
            List<WorkItem> changeList = siSandbox.getChangeList();
            for ( Iterator<WorkItem> wit = changeList.iterator(); wit.hasNext(); )
            {
                WorkItem wi = wit.next();
                File memberFile = new File( wi.getField( "name" ).getValueAsString() );
                // Separate the changes into files that have been updated and deleted files
                if ( siSandbox.hasWorkingFile( (Item) wi.getField( "wfdelta" ).getValue() ) )
                {
                    scmFileList.add( new ScmFile( memberFile.getAbsolutePath(), ScmFileStatus.UPDATED ) );
                }
                else
                {
                    scmFileList.add( new ScmFile( memberFile.getAbsolutePath(), ScmFileStatus.DELETED ) );
                }
            }

            if ( scmFileList.size() == 0 )
            {
                getLogger().info( "No local changes found!" );
            }
            result = new StatusScmResult( scmFileList, new ScmResult( "si viewsandbox", "", "", true ) );
        }
        catch ( APIException aex )
        {
            ExceptionHandler eh = new ExceptionHandler( aex );
            getLogger().error( "MKS API Exception: " + eh.getMessage() );
            getLogger().debug( eh.getCommand() + " exited with return code " + eh.getExitCode() );
            result = new StatusScmResult( eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false );
        }

        return result;
    }

}
