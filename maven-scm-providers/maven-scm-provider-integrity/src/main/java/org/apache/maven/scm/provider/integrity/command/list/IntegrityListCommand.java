package org.apache.maven.scm.provider.integrity.command.list;

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
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Member;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * MKS Integrity implementation for Maven's AbstractListCommand
 * <br>This command will a 'si viewproject' command listing all the files in the project
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityListCommand.java 1.4 2011/08/22 13:06:30EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityListCommand
    extends AbstractListCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public ListScmResult executeListCommand( ScmProviderRepository repository, ScmFileSet fileSet, boolean recursive,
                                             ScmVersion scmVersion )
        throws ScmException
    {
        ListScmResult result;
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        getLogger().info( "Listing all files in project " + iRepo.getConfigruationPath() );
        try
        {
            // Get a listing for all the members in the project...
            List<Member> projectMembers = iRepo.getProject().listFiles( fileSet.getBasedir().getAbsolutePath() );
            // Initialize the list of ScmFile objects for the ListScmResult
            List<ScmFile> scmFileList = new ArrayList<ScmFile>();
            for ( Iterator<Member> it = projectMembers.iterator(); it.hasNext(); )
            {
                Member siMember = it.next();
                scmFileList.add( new ScmFile( siMember.getTargetFilePath(), ScmFileStatus.UNKNOWN ) );
            }
            result = new ListScmResult( scmFileList, new ScmResult( "si viewproject", "", "", true ) );

        }
        catch ( APIException aex )
        {
            ExceptionHandler eh = new ExceptionHandler( aex );
            getLogger().error( "MKS API Exception: " + eh.getMessage() );
            getLogger().debug( eh.getCommand() + " exited with return code " + eh.getExitCode() );
            result = new ListScmResult( eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false );
        }

        return result;
    }

}
