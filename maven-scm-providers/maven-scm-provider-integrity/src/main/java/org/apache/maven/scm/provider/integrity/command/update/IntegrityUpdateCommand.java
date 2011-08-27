package org.apache.maven.scm.provider.integrity.command.update;

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
import com.mks.api.response.Response;
import com.mks.api.response.Result;
import com.mks.api.response.WorkItem;
import com.mks.api.response.WorkItemIterator;
import com.mks.api.si.SIModelTypeName;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Sandbox;
import org.apache.maven.scm.provider.integrity.command.changelog.IntegrityChangeLogCommand;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * MKS Integrity implementation of Maven's AbstractUpdateCommand
 * <br>This command will execute a 'si resync' to synchronize the sandbox with the server revisions.
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityUpdateCommand.java 1.3 2011/08/22 13:06:42EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityUpdateCommand
    extends AbstractUpdateCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public UpdateScmResult executeUpdateCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                 ScmVersion scmVersion )
        throws ScmException
    {
        getLogger().info( "Attempting to synchronize sandbox in " + fileSet.getBasedir().getAbsolutePath() );
        List<ScmFile> updatedFiles = new ArrayList<ScmFile>();
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        Sandbox siSandbox = iRepo.getSandbox();
        try
        {
            // Make sure we've got a valid sandbox, otherwise create it...
            if ( siSandbox.create() )
            {
                Response res = siSandbox.resync();
                // Lets capture what we got from running this resync
                WorkItemIterator wit = res.getWorkItems();
                while ( wit.hasNext() )
                {
                    WorkItem wi = wit.next();
                    if ( wi.getModelType().equals( SIModelTypeName.MEMBER ) )
                    {
                        Result message = wi.getResult();
                        getLogger().debug( wi.getDisplayId() + " " + ( null != message ? message.getMessage() : "" ) );
                        if ( null != message && message.getMessage().length() > 0 )
                        {
                            updatedFiles.add( new ScmFile( wi.getDisplayId(),
                                                           message.getMessage().equalsIgnoreCase( "removed" )
                                                               ? ScmFileStatus.DELETED
                                                               : ScmFileStatus.UPDATED ) );
                        }
                    }
                }
                return new UpdateScmResult( res.getCommandString(), updatedFiles );
            }
            else
            {
                return new UpdateScmResult( "si resync", "Failed to synchronize workspace", "", false );
            }
        }
        catch ( APIException aex )
        {
            ExceptionHandler eh = new ExceptionHandler( aex );
            getLogger().error( "MKS API Exception: " + eh.getMessage() );
            getLogger().info( eh.getCommand() + " exited with return code " + eh.getExitCode() );
            return new UpdateScmResult( eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false );
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ChangeLogCommand getChangeLogCommand()
    {
        IntegrityChangeLogCommand command = new IntegrityChangeLogCommand();
        command.setLogger( getLogger() );
        return command;
    }
}
