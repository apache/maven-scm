package org.apache.maven.scm.provider.integrity.command.export;

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

import com.mks.api.response.APIException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.export.AbstractExportCommand;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Member;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * MKS Integrity implementation for Maven's AbstractExportCommand
 * <br>Since the IntegrityCheckoutCommand creates a fresh Sandbox in the checkoutDirectory,
 * it does not make sense for the IntegrityExportCommand to essentially do the same thing.
 * <br>Hence, this command does not create a Sandbox, instead the entire project contents
 * are exported to the exportDirectory using the 'si projectco' command.
 * <br>This gives the user the option of exporting a fresh copy of the repository void of
 * any project.pj files
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @since 1.6
 */
public class IntegrityExportCommand
    extends AbstractExportCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public ExportScmResult executeExportCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                 ScmVersion scmVersion, String outputDirectory )
        throws ScmException
    {
        // First lets figure out where we need to export files to...
        String exportDir = outputDirectory;
        exportDir =
            ( ( null != exportDir && exportDir.length() > 0 ) ? exportDir : fileSet.getBasedir().getAbsolutePath() );
        // Let the user know where we're going to be exporting the files...
        getLogger().info( "Attempting to export files to " + exportDir );
        ExportScmResult result;
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        try
        {
            // Lets set our overall export success flag
            boolean exportSuccess = true;
            // Perform a fresh checkout of each file in the member list...
            List<Member> projectMembers = iRepo.getProject().listFiles( exportDir );
            // Initialize the list of files we actually exported...
            List<ScmFile> scmFileList = new ArrayList<ScmFile>();
            for ( Iterator<Member> it = projectMembers.iterator(); it.hasNext(); )
            {
                Member siMember = it.next();
                try
                {
                    getLogger().info( "Attempting to export file: " + siMember.getTargetFilePath() + " at revision "
                                          + siMember.getRevision() );
                    siMember.checkout( iRepo.getAPISession() );
                    scmFileList.add( new ScmFile( siMember.getTargetFilePath(), ScmFileStatus.UNKNOWN ) );
                }
                catch ( APIException ae )
                {
                    exportSuccess = false;
                    ExceptionHandler eh = new ExceptionHandler( ae );
                    getLogger().error( "MKS API Exception: " + eh.getMessage() );
                    getLogger().debug( eh.getCommand() + " exited with return code " + eh.getExitCode() );
                }
            }
            // Lets advice the user that we've checked out all the members
            getLogger().info(
                "Exported " + scmFileList.size() + " files out of a total of " + projectMembers.size() + " files!" );
            if ( exportSuccess )
            {
                result = new ExportScmResult( "si co", scmFileList );
            }
            else
            {
                result = new ExportScmResult( "si co", "Failed to export all files!", "", exportSuccess );
            }
        }
        catch ( APIException aex )
        {
            ExceptionHandler eh = new ExceptionHandler( aex );
            getLogger().error( "MKS API Exception: " + eh.getMessage() );
            getLogger().debug( eh.getCommand() + " exited with return code " + eh.getExitCode() );
            result = new ExportScmResult( eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false );
        }

        return result;
    }

}
