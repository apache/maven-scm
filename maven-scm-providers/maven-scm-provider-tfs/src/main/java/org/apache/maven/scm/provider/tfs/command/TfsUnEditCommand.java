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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.unedit.AbstractUnEditCommand;
import org.apache.maven.scm.command.unedit.UnEditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;
import org.apache.maven.scm.provider.tfs.command.consumer.FileListConsumer;

/**
 * 
 */
// Usage: mvn scm:unedit -DworkingDirectory=<dir> -Dincludes=*
public class TfsUnEditCommand
    extends AbstractUnEditCommand
{

    protected ScmResult executeUnEditCommand( ScmProviderRepository r, ScmFileSet f )
        throws ScmException
    {
        FileListConsumer out = new FileListConsumer();
        ErrorStreamConsumer err = new ErrorStreamConsumer();
        TfsCommand command = createCommand( r, f );
        int status = command.execute( out, err );
        if ( status != 0 || err.hasBeenFed() )
        {
            return new UnEditScmResult( command.getCommandString(), "Error code for TFS unedit command - " + status,
                                        err.getOutput(), false );
        }
        return new UnEditScmResult( command.getCommandString(), out.getFiles() );
    }

    public TfsCommand createCommand( ScmProviderRepository r, ScmFileSet f )
    {
        TfsCommand command = new TfsCommand( "undo", r, f, getLogger() );
        command.addArgument( f );
        return command;
    }
}
