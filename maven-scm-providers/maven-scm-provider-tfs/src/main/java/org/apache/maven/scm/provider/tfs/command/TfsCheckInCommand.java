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
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.tfs.TfsScmProviderRepository;
import org.apache.maven.scm.provider.tfs.command.consumer.ErrorStreamConsumer;
import org.apache.maven.scm.provider.tfs.command.consumer.FileListConsumer;

public class TfsCheckInCommand
    extends AbstractCheckInCommand
{
    private static String policiesArgument = "/override:checkin_policy";
    private static final String TFS_CHECKIN_POLICIES_ERROR = "TF10139";

    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository r, ScmFileSet f, String m, ScmVersion v )
        throws ScmException
    {
        TfsCommand command = createCommand( r, f, m );
        FileListConsumer fileConsumer = new FileListConsumer();
        ErrorStreamConsumer err = new ErrorStreamConsumer();

        int status = command.execute( fileConsumer, err );
        getLogger().debug( "status of checkin command is= " + status + "; err= " + err.getOutput() );

        //[SCM-753] support TFS checkin-policies - TFS returns error, that can be ignored.
        if( err.hasBeenFed() && err.getOutput().startsWith( TFS_CHECKIN_POLICIES_ERROR ) )
        {
            getLogger().info( "exclusion: got error " + TFS_CHECKIN_POLICIES_ERROR + " due to checkin policies. ignoring it..." ); 
        }
        else
        {//TODO - open bug for this (status is 0 or 1 bcoz checkin policies
            if ( status != 0 || err.hasBeenFed() )
            {
                getLogger().error( "ERROR in command: " + command.getCommandString() + "; Error code for TFS checkin command - " + status ); 
                return new CheckInScmResult( command.getCommandString(), "Error code for TFS checkin command - " + status,
                                             err.getOutput(), false );
            }
        }
        return new CheckInScmResult( command.getCommandString(), fileConsumer.getFiles() );
    }


    public TfsCommand createCommand( ScmProviderRepository r, ScmFileSet f, String m )
    {
        TfsCommand command = new TfsCommand( "checkin", r, f, getLogger() );
        command.addArgument( "-noprompt" );
        if ( m != null && !m.equals( "" ) )
        {
            command.addArgument( "-comment:" + m + "" );
        }
        command.addArgument( f );
        
        TfsScmProviderRepository tfsScmProviderRepo = ( TfsScmProviderRepository )r;
        if( tfsScmProviderRepo.isUseCheckinPolicies() )
        {
            //handle TFS-policies (by adding "/override:";Auto-Build: Version Update";)
            command.addArgument( policiesArgument );
        }

        return command;
    }

}
