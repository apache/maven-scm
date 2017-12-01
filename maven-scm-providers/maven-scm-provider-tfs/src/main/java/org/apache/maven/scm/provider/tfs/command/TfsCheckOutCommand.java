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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;
import org.apache.maven.scm.provider.tfs.command.consumer.FileListConsumer;

/**
 * 
 */
// Usage: mvn scm:checkout -DcheckoutDirectory=<dir>
public class TfsCheckOutCommand
    extends AbstractCheckOutCommand
{

    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository r, ScmFileSet f, ScmVersion v,
                                                       boolean recursive, boolean shallow )
        throws ScmException
    {
        TfsScmProviderRepository tfsRepo = (TfsScmProviderRepository) r;
        String url = tfsRepo.getServerPath();
        String tfsUrl = tfsRepo.getTfsUrl();
        String workspace = tfsRepo.getWorkspace();

        // Try creating workspace
        boolean workspaceProvided = workspace != null && !workspace.trim().equals( "" );
        if ( workspaceProvided )
        {
            createWorkspace( r, f, workspace, tfsUrl );
        }

        TfsCommand command;
        int status;

        if ( workspaceProvided )
        {
            status = executeUnmapCommand( r, f );
        }
        
        ErrorStreamConsumer out = new ErrorStreamConsumer();
        ErrorStreamConsumer err = new ErrorStreamConsumer();
        if ( workspaceProvided )
        {
            command = new TfsCommand( "workfold", r, null, getLogger() );
            command.addArgument( "-workspace:" + workspace );
            command.addArgument( "-map" );
            command.addArgument( url );
            command.addArgument( f.getBasedir().getAbsolutePath() );
            status = command.execute( out, err );
            if ( status != 0 || err.hasBeenFed() )
            {
                return new CheckOutScmResult( command.getCommandString(),
                                              "Error code for TFS checkout (workfold map) command - " + status,
                                              err.getOutput(), false );
            }
        }
        FileListConsumer fileConsumer = new FileListConsumer();
        err = new ErrorStreamConsumer();
        command = createGetCommand( r, f, v, recursive );
        status = command.execute( fileConsumer, err );
        if ( status != 0 || err.hasBeenFed() )
        {
            return new CheckOutScmResult( command.getCommandString(), "Error code for TFS checkout (get) command - "
                + status, err.getOutput(), false );
        }
        
        return new CheckOutScmResult( command.getCommandString(), fileConsumer.getFiles() );
    }

    public TfsCommand createGetCommand( ScmProviderRepository r, ScmFileSet f, ScmVersion v, boolean recursive )
    {
        TfsCommand command = new TfsCommand( "get", r, f, getLogger() );
        if ( recursive )
        {
            command.addArgument( "-recursive" );
        }
        
        command.addArgument( "-force" );
        
        if ( v != null && !v.equals( "" ) )
        {
            String vType = "";
            if ( v.getType().equals( "Tag" ) )
            {
                vType = "L";
            }
            if ( v.getType().equals( "Revision" ) )
            {
                vType = "C";
            }
            command.addArgument( "-version:" + vType + v.getName() );
        }
        
        command.addArgument( f.getBasedir().getAbsolutePath() );
        
        return command;
    }

    public int executeUnmapCommand( ScmProviderRepository r, ScmFileSet f )
        throws ScmException
    {
        TfsScmProviderRepository tfsRepo = (TfsScmProviderRepository) r;
        String url = tfsRepo.getServerPath();
        String workspace = tfsRepo.getWorkspace();
        ErrorStreamConsumer out = new ErrorStreamConsumer();
        ErrorStreamConsumer err = new ErrorStreamConsumer();
        
        TfsCommand command = new TfsCommand( "workfold", r, null, getLogger() );
        command.addArgument( "-workspace:" + workspace );
        command.addArgument( "-unmap" );
        command.addArgument( url );
        
        return command.execute( out, err );
    }

    private void createWorkspace( ScmProviderRepository r, ScmFileSet f, String workspace, String url )
        throws ScmException
    {
        ErrorStreamConsumer out = new ErrorStreamConsumer();
        ErrorStreamConsumer err = new ErrorStreamConsumer();
        // Checkout dir may not exist yet
        TfsCommand command = new TfsCommand( "workspace", r, null, getLogger() );
        command.addArgument( "-new" );
        command.addArgument( "-comment:Creating workspace for maven command" );
        command.addArgument( "-server:" + url );
        command.addArgument( workspace );
        
        command.execute( out, err );
    }

}
