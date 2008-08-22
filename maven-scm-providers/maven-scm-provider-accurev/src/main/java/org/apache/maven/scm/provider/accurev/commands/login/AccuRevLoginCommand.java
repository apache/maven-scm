package org.apache.maven.scm.provider.accurev.commands.login;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.util.ArrayList;

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.login.AbstractLoginCommand;
import org.apache.maven.scm.command.login.LoginScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevScmProvider;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @version $Id$
 */
public class AccuRevLoginCommand extends AbstractLoginCommand
{
    private String executable;

    public AccuRevLoginCommand( String executable )
    {
        this.executable = executable;
    }

    /** {@inheritDoc} */
    public LoginScmResult executeLoginCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                               CommandParameters parameters ) throws ScmException
    {
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        String username = repository.getUser();
        if ( null == username )
        {
            //No need to login
            return new LoginScmResult( null, null, null, true );
        }
        //Do the login
        Commandline cl = createLoginCommandLine( (AccuRevScmProviderRepository) repository );
        try
        {
            int exitCode = CommandLineUtils.executeCommandLine( cl, stderr, stderr );
            //Create result based on exit code
            LoginScmResult scmResult;
            if ( exitCode != 0 )
            {
                scmResult = new LoginScmResult( cl.toString(), null, stderr.getOutput(), false );
            }
            else
            {
                scmResult = new LoginScmResult( cl.toString(), null, null, true );
            }
            return scmResult;
        }
        catch ( CommandLineException e )
        {
            throw new ScmException( e.getMessage(), e );
        }
    }

    /**
     * Creates the login command line
     *
     * @param repository AccuRevScmProviderRepository object
     * @return Commadline object to perform login
     */
    protected Commandline createLoginCommandLine( AccuRevScmProviderRepository repository )
    {
        Commandline cl = new Commandline();
        cl.setExecutable( this.executable );

        ArrayList params = new ArrayList();
        params.add( "login" );
        //Append host info
        AccuRevScmProvider.appendHostToParamsIfNeeded( repository, params );
        //Append command params
        params.add( repository.getUser() );
        String password = repository.getPassword();
        params.add( StringUtils.isEmpty( password ) ? "\"\"" : password );
        //Set arguments to command line
        cl.addArguments( (String[]) params.toArray( new String[params.size()] ) );
        return cl;
    }
}
