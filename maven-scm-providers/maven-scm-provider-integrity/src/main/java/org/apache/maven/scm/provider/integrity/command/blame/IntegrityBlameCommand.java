package org.apache.maven.scm.provider.integrity.command.blame;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.blame.AbstractBlameCommand;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.APISession;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * MKS Integrity implementation for Maven's AbstractBlameCommand
 * <br>This class will execute a 'si annotate' command for the specified filename
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityBlameCommand.java 1.3 2011/08/22 13:06:15EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityBlameCommand
    extends AbstractBlameCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public BlameScmResult executeBlameCommand( ScmProviderRepository repository, ScmFileSet workingDirectory,
                                               String filename )
        throws ScmException
    {
        getLogger().info( "Attempting to display blame results for file: " + filename );
        if ( null == filename || filename.length() == 0 )
        {
            throw new ScmException( "A single filename is required to execute the blame command!" );
        }
        BlameScmResult result;
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        APISession api = iRepo.getAPISession();
        // Since the si annotate command is not completely API ready, we will use the CLI for this command
        Commandline shell = new Commandline();
        shell.setWorkingDirectory( workingDirectory.getBasedir() );
        shell.setExecutable( "si" );
        shell.createArg().setValue( "annotate" );
        shell.createArg().setValue( "--hostname=" + api.getHostName() );
        shell.createArg().setValue( "--port=" + api.getPort() );
        shell.createArg().setValue( "--user=" + api.getUserName() );
        shell.createArg().setValue( "--fields=date,revision,author" );
        shell.createArg().setValue( '"' + filename + '"' );
        IntegrityBlameConsumer shellConsumer = new IntegrityBlameConsumer( getLogger() );

        try
        {
            getLogger().debug( "Executing: " + shell.getCommandline() );
            int exitCode = CommandLineUtils.executeCommandLine( shell, shellConsumer,
                                                                new CommandLineUtils.StringStreamConsumer() );
            boolean success = ( exitCode == 128 ? false : true );
            ScmResult scmResult =
                new ScmResult( shell.getCommandline().toString(), "", "Exit Code: " + exitCode, success );
            return new BlameScmResult( shellConsumer.getBlameList(), scmResult );
        }
        catch ( CommandLineException cle )
        {
            getLogger().error( "Command Line Exception: " + cle.getMessage() );
            result = new BlameScmResult( shell.getCommandline().toString(), cle.getMessage(), "", false );
        }

        return result;
    }

}
