package org.apache.maven.scm.provider.vss.commands.add;

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
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.vss.commands.VssCommandLineUtils;
import org.apache.maven.scm.provider.vss.commands.VssConstants;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class VssAddCommand
    extends AbstractAddCommand
{
    protected ScmResult executeAddCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {
        VssScmProviderRepository repo = (VssScmProviderRepository) repository;

        if ( fileSet.getFiles().length == 0 )
        {
            throw new ScmException( "You must provide at least one file/directory to add" );
        }

        Commandline cl = buildCmdLine( repo, fileSet );

        VssAddConsumer consumer = new VssAddConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        int exitCode = VssCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

        if ( exitCode != 0 )
        {
            return new ChangeLogScmResult( cl.toString(), "The vss command failed.", stderr.getOutput(), false );
        }

        return new AddScmResult( cl.toString(), consumer.getAddedFiles() );
    }

    public Commandline buildCmdLine( VssScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        Commandline command = new Commandline();

        command.setWorkingDirectory( fileSet.getBasedir().getAbsolutePath() );

        try
        {
            command.addSystemEnvironment();
        }
        catch ( Exception e )
        {
            throw new ScmException( "Can't add system environment.", e );
        }

        command.addEnvironment( "SSDIR", repo.getVssdir() );

        String ssDir = VssCommandLineUtils.getSsDir();

        command.setExecutable( ssDir + VssConstants.SS_EXE );

        command.createArgument().setValue( VssConstants.COMMAND_ADD );

        VssCommandLineUtils.addFiles( command, fileSet );

        //        command.createArgument().setValue( VssConstants.PROJECT_PREFIX + repo.getProject() );

        //User identification to get access to vss repository
        if ( repo.getUserPassword() != null )
        {
            command.createArgument().setValue( VssConstants.FLAG_LOGIN + repo.getUserPassword() );
        }

        //Ignore: Do not ask for input under any circumstances.
        command.createArgument().setValue( VssConstants.FLAG_AUTORESPONSE_DEF );

        return command;
    }

    public Commandline buildSetCurrentProjectCmdLine( VssScmProviderRepository repo )
        throws ScmException
    {
        Commandline command = new Commandline();

        try
        {
            command.addSystemEnvironment();
        }
        catch ( Exception e )
        {
            throw new ScmException( "Can't add system environment.", e );
        }

        command.addEnvironment( "SSDIR", repo.getVssdir() );

        String ssDir = VssCommandLineUtils.getSsDir();

        command.setExecutable( ssDir + VssConstants.SS_EXE );

        command.createArgument().setValue( VssConstants.COMMAND_CP );

        command.createArgument().setValue( VssConstants.PROJECT_PREFIX + repo.getProject() );

        //User identification to get access to vss repository
        if ( repo.getUserPassword() != null )
        {
            command.createArgument().setValue( VssConstants.FLAG_LOGIN + repo.getUserPassword() );
        }

        //Ignore: Do not ask for input under any circumstances.
        command.createArgument().setValue( VssConstants.FLAG_AUTORESPONSE_DEF );

        return command;
    }

}
