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
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;
import org.apache.maven.scm.provider.tfs.command.consumer.FileListConsumer;

/**
 * 
 */
public class TfsUpdateCommand
    extends AbstractUpdateCommand
{

    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository r, ScmFileSet f, ScmVersion v )
        throws ScmException
    {
        FileListConsumer fileConsumer = new FileListConsumer();
        ErrorStreamConsumer err = new ErrorStreamConsumer();
        
        TfsCommand command = createCommand( r, f, v );
        int status = command.execute( fileConsumer, err );
        if ( status != 0 || err.hasBeenFed() )
        {
            return new UpdateScmResult( command.getCommandString(), "Error code for TFS update command - " + status,
                                        err.getOutput(), false );
        }
        return new UpdateScmResult( command.getCommandString(), fileConsumer.getFiles() );
    }

    public TfsCommand createCommand( ScmProviderRepository r, ScmFileSet f, ScmVersion v )
    {
        String serverPath = ( (TfsScmProviderRepository) r ).getServerPath();
        TfsCommand command = new TfsCommand( "get", r, f, getLogger() );
        command.addArgument( serverPath );
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
        return command;
    }

    protected ChangeLogCommand getChangeLogCommand()
    {
        TfsChangeLogCommand changeLogCommand = new TfsChangeLogCommand();
        changeLogCommand.setLogger( getLogger() );
        return changeLogCommand;
    }

}
