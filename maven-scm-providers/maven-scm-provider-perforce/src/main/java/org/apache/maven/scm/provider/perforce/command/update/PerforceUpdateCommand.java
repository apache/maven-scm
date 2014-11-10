package org.apache.maven.scm.provider.perforce.command.update;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.command.update.UpdateScmResultWithRevision;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.PerforceScmProvider;
import org.apache.maven.scm.provider.perforce.repository.PerforceScmProviderRepository;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.command.changelog.PerforceChangeLogCommand;
import org.apache.maven.scm.provider.perforce.command.checkout.PerforceCheckOutCommand;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author Mike Perham
 *
 */
public class PerforceUpdateCommand
    extends AbstractUpdateCommand
    implements PerforceCommand
{
    /** {@inheritDoc} */
    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet files,
                                                    ScmVersion scmVersion )
        throws ScmException
    {
        // In Perforce, there is no difference between update and checkout.
        // Here we just run the checkout command and map the result onto an
        // UpdateScmResult.
        PerforceCheckOutCommand command = new PerforceCheckOutCommand();
        command.setLogger( getLogger() );
        CommandParameters params = new CommandParameters();
        params.setScmVersion( CommandParameter.SCM_VERSION, scmVersion );

        CheckOutScmResult cosr = (CheckOutScmResult) command.execute( repo, files, params );
        if ( !cosr.isSuccess() )
        {
            return new UpdateScmResult( cosr.getCommandLine(), cosr.getProviderMessage(), cosr.getCommandOutput(),
                                        false );
        }

        PerforceScmProviderRepository p4repo = (PerforceScmProviderRepository) repo;
        String clientspec = PerforceScmProvider.getClientspecName( getLogger(), p4repo, files.getBasedir() );
        Commandline cl = createCommandLine( p4repo, files.getBasedir(), clientspec );

        @SuppressWarnings( "unused" )
        String location = PerforceScmProvider.getRepoPath( getLogger(), p4repo, files.getBasedir() );
        PerforceHaveConsumer consumer =
            new PerforceHaveConsumer( getLogger() );

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( PerforceScmProvider.clean( "Executing " + cl.toString() ) );
            }

            CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
            int exitCode = CommandLineUtils.executeCommandLine( cl, consumer, err );

            if ( exitCode != 0 )
            {
                String cmdLine = CommandLineUtils.toString( cl.getCommandline() );

                StringBuilder msg = new StringBuilder( "Exit code: " + exitCode + " - " + err.getOutput() );
                msg.append( '\n' );
                msg.append( "Command line was:" + cmdLine );

                throw new CommandLineException( msg.toString() );
            }
        }
        catch ( CommandLineException e )
        {
            if ( getLogger().isErrorEnabled() )
            {
                getLogger().error( "CommandLineException " + e.getMessage(), e );
            }
        }

        return new UpdateScmResultWithRevision( cosr.getCommandLine(), cosr.getCheckedOutFiles(),
                                                String.valueOf( consumer.getHave() ) );
    }

    /** {@inheritDoc} */
    protected ChangeLogCommand getChangeLogCommand()
    {
        PerforceChangeLogCommand command = new PerforceChangeLogCommand();
        command.setLogger( getLogger() );
        return command;
    }

    public static Commandline createCommandLine( PerforceScmProviderRepository repo, File workingDirectory,
                                                 String clientspec )
    {
        Commandline command = PerforceScmProvider.createP4Command( repo, workingDirectory );

        if ( clientspec != null )
        {
            command.createArg().setValue( "-c" );
            command.createArg().setValue( clientspec );
        }
        command.createArg().setValue( "changes" );
        command.createArg().setValue( "-m1" );
        command.createArg().setValue( "-ssubmitted" );
        command.createArg().setValue( "//" + clientspec + "/...#have" );

        return command;
    }
}
