package org.apache.maven.scm.provider.tfs.command;

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

import java.util.Iterator;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ChangedFileConsumer;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;

public class TfsStatusCommand
    extends AbstractStatusCommand
{

    protected StatusScmResult executeStatusCommand( ScmProviderRepository r, ScmFileSet f )
        throws ScmException
    {
        TfsScmProviderRepository tfsRepo = (TfsScmProviderRepository) r;

        TfsCommand command = createCommand( tfsRepo, f );
        ChangedFileConsumer out = new ChangedFileConsumer( getLogger() );
        ErrorStreamConsumer err = new ErrorStreamConsumer();
        
        int status = command.execute( out, err );
        if ( status != 0 || err.hasBeenFed() )
        {
            return new StatusScmResult( command.getCommandString(), "Error code for TFS status command - " + status,
                                        err.getOutput(), false );
        }
        Iterator iter = out.getChangedFiles().iterator();
        getLogger().debug( "Iterating" );
        while ( iter.hasNext() )
        {
            ScmFile file = (ScmFile) iter.next();
            getLogger().debug( file.getPath() + ":" + file.getStatus() );
        }
        return new StatusScmResult( command.getCommandString(), out.getChangedFiles() );
    }

    public TfsCommand createCommand( TfsScmProviderRepository r, ScmFileSet f )
    {
        String url = r.getServerPath();
        String workspace = r.getWorkspace();
        TfsCommand command = new TfsCommand( "status", r, f, getLogger() );
        if ( workspace != null && !workspace.trim().equals( "" ) )
        {
            command.addArgument( "-workspace:" + workspace );
        }
        command.addArgument( "-recursive" );
        command.addArgument( "-format:detailed" );
        command.addArgument( url );
        return command;
    }
}

