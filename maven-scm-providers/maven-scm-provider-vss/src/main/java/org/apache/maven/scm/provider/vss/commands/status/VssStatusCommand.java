package org.apache.maven.scm.provider.vss.commands.status;

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
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.vss.commands.VssCommandLineUtils;
import org.apache.maven.scm.provider.vss.commands.VssConstants;
import org.apache.maven.scm.provider.vss.commands.changelog.VssHistoryCommand;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:triek@thrx.de">Thorsten Riek</a>
 * @version $Id$
 */
public class VssStatusCommand
    extends AbstractStatusCommand
{
    /** {@inheritDoc} */
    protected StatusScmResult executeStatusCommand( ScmProviderRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        getLogger().debug( "executing status command..." );

        VssScmProviderRepository repo = (VssScmProviderRepository) repository;

        Commandline cl = buildCmdLine( repo, fileSet );

        VssStatusConsumer consumer = new VssStatusConsumer( repo, getLogger(), fileSet );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        getLogger().debug( "Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" + cl.toString() );

        exitCode = VssCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

        if ( exitCode != 0 )
        {
            String error = stderr.getOutput();
            getLogger().debug( "VSS returns error: [" + error + "] return code: [" + exitCode + "]" );
            if ( false )
            {
                return new StatusScmResult( cl.toString(), "The vss command failed.", error, false );
            }
            // print out the writable copy for manual handling
            //            getLogger().warn(error);
        }

        return new StatusScmResult( cl.toString(), consumer.getUpdatedFiles() );
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

        command.createArgument().setValue( VssConstants.COMMAND_DIFF );

        command.createArgument().setValue( VssConstants.PROJECT_PREFIX + repo.getProject() );

        //User identification to get access to vss repository
        if ( repo.getUserPassword() != null )
        {
            command.createArgument().setValue( VssConstants.FLAG_LOGIN + repo.getUserPassword() );
        }

        //Display the history of an entire project list
        command.createArgument().setValue( VssConstants.FLAG_RECURSION );

        //Ignore: Do not ask for input under any circumstances.
        command.createArgument().setValue( VssConstants.FLAG_AUTORESPONSE_DEF );

        // TODO: Get Labled Version
        // command.createArgument().setValue( VssConstants.FLAG_VERSION_LABEL );

        return command;
    }

    /**
     * @return
     */
    protected ChangeLogCommand getChangeLogCommand()
    {
        VssHistoryCommand command = new VssHistoryCommand();

        command.setLogger( getLogger() );

        return command;
    }

}
