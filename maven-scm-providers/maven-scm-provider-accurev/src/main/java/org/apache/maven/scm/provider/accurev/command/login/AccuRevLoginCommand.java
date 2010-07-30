package org.apache.maven.scm.provider.accurev.command.login;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommand;

public class AccuRevLoginCommand
    extends AbstractAccuRevCommand
{

    public AccuRevLoginCommand( ScmLogger logger )
    {
        super( logger );

    }

    @Override
    protected ScmResult executeAccurevCommand( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                               CommandParameters parameters )
        throws ScmException, AccuRevException
    {
        boolean result = false;
        AccuRev accurev = repository.getAccuRev();
        AccuRevInfo info = accurev.info( null );

        String providerMessage = "";
        if ( info == null )
        {
            providerMessage = "Unable to retrieve accurev info";
        }
        else if ( repository.getUser() != null )
        {
            // Check if we've already logged in as this user
            result = repository.getUser().equals( info.getUser() );
            if ( result )
            {
                providerMessage = "Skipping login - already logged in as " + repository.getUser();
            }
            else
            {
                result = accurev.login( repository.getUser(), repository.getPassword() );
                providerMessage = ( result ? "Success" : "Failure" ) + " logging in as " + repository.getUser();
            }
        }
        else
        {
            result = info.isLoggedIn();
            providerMessage = result ? ( "Logged in externally as " + info.getUser() ) : "Not logged in";
        }

        getLogger().debug( providerMessage );
        return new LoginScmResult( accurev.getCommandLines(), providerMessage, accurev.getErrorOutput(), result );

    }

    public LoginScmResult login( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (LoginScmResult) execute( repository, fileSet, parameters );
    }

}
