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
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.list.AbstractListCommand;
import org.apache.maven.scm.command.list.ListScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;

public class TfsListCommand
    extends AbstractListCommand
{

    protected ListScmResult executeListCommand( ScmProviderRepository r, ScmFileSet f, boolean recursive, ScmVersion v )
        throws ScmException
    {
        FileListConsumer out = new ServerFileListConsumer();
        ErrorStreamConsumer err = new ErrorStreamConsumer();
        TfsCommand command = createCommand( r, f, recursive );
        int status = command.execute( out, err );
        if ( status != 0 || err.hasBeenFed() )
            return new ListScmResult( command.getCommandline(), "Error code for TFS list command - " + status,
                                      err.getOutput(), false );
        return new ListScmResult( command.getCommandline(), out.getFiles() );
    }

    TfsCommand createCommand( ScmProviderRepository r, ScmFileSet f, boolean recursive )
    {
        TfsCommand command = new TfsCommand( "dir", r, f, getLogger() );
        if ( recursive )
            command.addArgument( "-recursive" );
        command.addArgument( f );
        return command;
    }

}

class ServerFileListConsumer
    extends FileListConsumer
{
    protected ScmFile getScmFile( String filename )
    {
        if ( filename.startsWith( "$" ) )
            filename = filename.replace( "$", "" );
        String path = currentDir + "/" + filename;
        return new ScmFile( path, ScmFileStatus.UNKNOWN );
    }
}
