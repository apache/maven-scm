package org.apache.maven.scm.provider.integrity.command.fileinfo;

/**
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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.fileinfo.AbstractFileInfoCommand;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.APISession;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * MKS Integrity implementation for Maven's AbstractFileInfoCommand
 * <br>This command will run a 'si memberinfo' command.  Even though this
 * command is supported via the API, we're using the CLI to execute as
 * its not clear what exactly this command is supposed to be returning
 * to the Maven SCM framework.  Hence the CLI output is returned to the
 * console verbatim
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityFileInfoCommand.java 1.3 2011/08/22 13:06:28EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityFileInfoCommand
    extends AbstractFileInfoCommand
{
    /**
     * Even though this command is supported via the MKS JAVA API, since at this time we really don't
     * know what the SCM plugin is looking to get in return for this command, we're simply going to
     * run this command via the CLI and return the output verbatim
     */
    @Override
    public ScmResult executeFileInfoCommand( ScmProviderRepository repository, File workingDirectory, String filename )
        throws ScmException
    {
        getLogger().info( "Attempting to display scm file information for file: " + filename );
        if ( null == filename || filename.length() == 0 )
        {
            throw new ScmException( "A single filename is required to execute the fileinfo command!" );
        }
        ScmResult result;
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        APISession api = iRepo.getAPISession();
        Commandline shell = new Commandline();
        shell.setWorkingDirectory( workingDirectory );
        shell.setExecutable( "si" );
        shell.createArg().setValue( "memberinfo" );
        shell.createArg().setValue( "--hostname=" + api.getHostName() );
        shell.createArg().setValue( "--port=" + api.getPort() );
        shell.createArg().setValue( "--user=" + api.getUserName() );
        shell.createArg().setValue( '"' + filename + '"' );
        IntegrityFileInfoConsumer shellConsumer = new IntegrityFileInfoConsumer( getLogger() );

        try
        {
            getLogger().debug( "Executing: " + shell.getCommandline() );
            int exitCode = CommandLineUtils.executeCommandLine( shell, shellConsumer,
                                                                new CommandLineUtils.StringStreamConsumer() );
            boolean success = ( exitCode == 128 ? false : true );
            result = new ScmResult( shell.getCommandline().toString(), "", "Exit Code: " + exitCode, success );

        }
        catch ( CommandLineException cle )
        {
            getLogger().error( "Command Line Exception: " + cle.getMessage() );
            result = new ScmResult( shell.getCommandline().toString(), cle.getMessage(), "", false );
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ScmResult executeCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                        CommandParameters parameters )
        throws ScmException
    {
        return executeFileInfoCommand( repository, fileSet.getBasedir(),
                                       parameters.getString( CommandParameter.FILE ) );
    }

}
