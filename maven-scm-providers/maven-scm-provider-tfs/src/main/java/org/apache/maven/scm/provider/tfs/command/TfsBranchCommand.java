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

import java.util.ArrayList;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.branch.AbstractBranchCommand;
import org.apache.maven.scm.command.branch.BranchScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;

/**
 * 
 */
public class TfsBranchCommand
    extends AbstractBranchCommand
{

    protected ScmResult executeBranchCommand( ScmProviderRepository r, ScmFileSet f, String branch, String message )
        throws ScmException
    {
        TfsCommand command = createCommand( r, f, branch );
        StringStreamConsumer out = new StringStreamConsumer();
        ErrorStreamConsumer err = new ErrorStreamConsumer();
        int status = command.execute( out, err );
        getLogger().info( "status of branch command is= " + status + "; err= " + err.getOutput() );
        if ( status != 0 || err.hasBeenFed() )
        {
            return new BranchScmResult( command.getCommandString(), "Error code for TFS branch command - " + status,
                                        err.getOutput(), false );
        }
        return new BranchScmResult( command.getCommandString(), new ArrayList<ScmFile>( 0 ) );
    }

    public TfsCommand createCommand( ScmProviderRepository r, ScmFileSet f, String branch )
    {
        TfsCommand command = new TfsCommand( "branch", r, f, getLogger() );

        //SCM-759
        //command.addArgument( f.getBasedir().getAbsolutePath() );
        String serverPath = ( (TfsScmProviderRepository) r ).getServerPath();
        command.addArgument( serverPath );


        command.addArgument( "-checkin" );
        command.addArgument( branch );
        return command;
    }
    

}
