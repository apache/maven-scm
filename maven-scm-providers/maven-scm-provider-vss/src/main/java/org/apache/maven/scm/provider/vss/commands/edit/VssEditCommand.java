package org.apache.maven.scm.provider.vss.commands.edit;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.edit.AbstractEditCommand;
import org.apache.maven.scm.command.edit.EditScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.vss.commands.VssCommandLineUtils;
import org.apache.maven.scm.provider.vss.commands.VssConstants;
import org.apache.maven.scm.provider.vss.commands.changelog.VssHistoryCommand;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:triek@thrx.de">Thorsten Riek</a>
 */
public class VssEditCommand
    extends AbstractEditCommand
{

    /** {@inheritDoc} */
    protected ScmResult executeEditCommand( ScmProviderRepository repository, ScmFileSet fileSet )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing checkout command..." );
        }

        VssScmProviderRepository repo = (VssScmProviderRepository) repository;

        List<Commandline> commandLines = buildCmdLine( repo, fileSet );

        VssEditConsumer consumer = new VssEditConsumer( repo, getLogger() );

        //      TODO handle deleted files from VSS
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        StringBuilder sb = new StringBuilder();
        List<ScmFile> updatedFiles = new ArrayList<ScmFile>();

        for ( Commandline cl : commandLines )
        {

            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" + cl.toString() );
            }

            exitCode = VssCommandLineUtils.executeCommandline( cl, consumer, stderr, getLogger() );

            if ( exitCode != 0 )
            {
                String error = stderr.getOutput();

                if ( getLogger().isDebugEnabled() )
                {
                    getLogger().debug( "VSS returns error: [" + error + "] return code: [" + exitCode + "]" );
                }
                if ( error.indexOf( "A writable copy of" ) < 0 )
                {
                    return new EditScmResult( cl.toString(), "The vss command failed.", error, false );
                }
                // print out the writable copy for manual handling
                if ( getLogger().isWarnEnabled() )
                {
                    getLogger().warn( error );
                }
                break;
            }

            sb.append( cl.toString() + '\n' );
            updatedFiles.addAll( consumer.getUpdatedFiles() );

        }
        return new EditScmResult( sb.toString(), updatedFiles );

    }

    public List<Commandline> buildCmdLine( VssScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        List<File> files = fileSet.getFileList();
        List<Commandline> commands = new ArrayList<Commandline>();

        if ( files.size() > 0 )
        {

            String base;
            try
            {
                base = fileSet.getBasedir().getCanonicalPath();
            }
            catch ( IOException e )
            {
                throw new ScmException( "Invalid canonical path", e );
            }

            for ( File file : files )
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

                command.createArg().setValue( VssConstants.COMMAND_CHECKOUT );

                String absolute;
                try
                {
                    absolute = file.getCanonicalPath();
                    String relative;
                    int index = absolute.indexOf( base );
                    if ( index >= 0 )
                    {
                        relative = absolute.substring( index + base.length() );
                    }
                    else
                    {
                        relative = file.getPath();
                    }

                    relative = relative.replace( '\\', '/' );

                    if ( !relative.startsWith( "/" ) )
                    {
                        relative = '/' + relative;
                    }

                    String relativeFolder = relative.substring( 0, relative.lastIndexOf( '/' ) );

                    command.setWorkingDirectory( new File( fileSet.getBasedir().getAbsolutePath() + File.separatorChar
                        + relativeFolder ).getCanonicalPath() );

                    command.createArg().setValue( VssConstants.PROJECT_PREFIX + repo.getProject() + relative );
                }
                catch ( IOException e )
                {
                    throw new ScmException( "Invalid canonical path", e );
                }

                //User identification to get access to vss repository
                if ( repo.getUserPassword() != null )
                {
                    command.createArg().setValue( VssConstants.FLAG_LOGIN + repo.getUserPassword() );
                }

                //Ignore: Do not ask for input under any circumstances.
                command.createArg().setValue( VssConstants.FLAG_AUTORESPONSE_DEF );

                commands.add( command );

            }

        }
        else
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

            command.createArg().setValue( VssConstants.COMMAND_CHECKOUT );

            command.createArg().setValue( VssConstants.PROJECT_PREFIX + repo.getProject() );
            //Display the history of an entire project list
            command.createArg().setValue( VssConstants.FLAG_RECURSION );

            //User identification to get access to vss repository
            if ( repo.getUserPassword() != null )
            {
                command.createArg().setValue( VssConstants.FLAG_LOGIN + repo.getUserPassword() );
            }

            //Ignore: Do not ask for input under any circumstances.
            command.createArg().setValue( VssConstants.FLAG_AUTORESPONSE_DEF );

            commands.add( command );

        }

        return commands;
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
