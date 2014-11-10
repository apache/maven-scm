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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

/**
 * @author Olivier Lamy
 *
 */
public class TfsTagCommand
    extends AbstractTagCommand
{

    protected ScmResult executeTagCommand( ScmProviderRepository r, ScmFileSet f, String tag, String message )
        throws ScmException
    {
        return executeTagCommand( r, f, tag, new ScmTagParameters( message ) );
    }

    protected ScmResult executeTagCommand( ScmProviderRepository r, ScmFileSet f, String tag,
                                           ScmTagParameters scmTagParameters )
        throws ScmException
    {
        TfsCommand command = createCommand( r, f, tag, scmTagParameters );

        StringStreamConsumer out = new StringStreamConsumer();
        ErrorStreamConsumer err = new ErrorStreamConsumer();

        int status = command.execute( out, err );
        if ( status != 0 || err.hasBeenFed() )
        {
            return new TagScmResult( command.getCommandString(), "Error code for TFS label command - " + status,
                                     err.getOutput(), false );
        }
        List<ScmFile> files = new ArrayList<ScmFile>( f.getFileList().size() );
        for ( File file : f.getFileList() )
        {
            files.add( new ScmFile( file.getPath(), ScmFileStatus.TAGGED ) );
        }
        return new TagScmResult( command.getCommandString(), files );

    }

    public TfsCommand createCommand( ScmProviderRepository r, ScmFileSet f, String tag,
                                        ScmTagParameters scmTagParameters )
    {
        TfsScmProviderRepository tfsRepo = (TfsScmProviderRepository) r;
        String url = tfsRepo.getServerPath();

        TfsCommand command = new TfsCommand( "label", r, f, getLogger() );
        command.addArgument( tag );
        command.addArgument( url );
        command.addArgument( "-recursive" );
        command.addArgument( "-child:replace" );
        String message = scmTagParameters.getMessage();
        if ( message != null && !message.equals( "" ) )
        {
            command.addArgument( "-comment:" + message );
        }
        return command;
    }

}
