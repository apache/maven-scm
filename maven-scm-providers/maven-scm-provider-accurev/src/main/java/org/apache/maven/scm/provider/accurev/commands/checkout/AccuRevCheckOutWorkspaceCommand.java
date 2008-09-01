package org.apache.maven.scm.provider.accurev.commands.checkout;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevScmProvider;
import org.apache.maven.scm.util.AbstractConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @version $Id$
 */
public class AccuRevCheckOutWorkspaceCommand
    extends AbstractCheckOutCommand
    implements BaseAccuRevCheckOutCommand
{
    private String accuRevExecutable;

    public AccuRevCheckOutWorkspaceCommand( String executable )
    {
        this.accuRevExecutable = executable;
    }

    /** {@inheritDoc} */
    public String getMethodName()
    {
        return "mkws";
    }

    /** {@inheritDoc} */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                        ScmVersion version, boolean recursive )
        throws ScmException
    {
        try
        {
            final CommandLineUtils.StringStreamConsumer stdout = new CommandLineUtils.StringStreamConsumer();

            //Create checkout folder if missing
            if ( !fileSet.getBasedir().exists() )
            {
                fileSet.getBasedir().mkdirs();
            }

            //Make new workspace
            AccuRevScmProviderRepository accurevRepository = (AccuRevScmProviderRepository) repository;
            Commandline makeCommandline = createMakeWorkspaceCommandLine( accurevRepository, fileSet, version );

            int exitCode = CommandLineUtils.executeCommandLine( makeCommandline, stdout, stdout );

            if ( exitCode != 0 )
            {
                return new CheckOutScmResult( makeCommandline.toString(),
                                              "The AccuRev command failed. Cannot create workspace: "
                                                  + stdout.getOutput(), stdout.getOutput(), false );
            }
            //Update the workspace
            Commandline updateCommandline = createUpdateWorkspaceCommand( accurevRepository, fileSet, version );

            final ArrayList checkedFiles = new ArrayList();
            exitCode = CommandLineUtils.executeCommandLine( updateCommandline, new AbstractConsumer( getLogger() )
            {
                private Pattern pattern = Pattern.compile( "Updating element (.*)" );

                public void consumeLine( String line )
                {
                    //Consume line to stdout consumer
                    stdout.consumeLine( line );
                    //Check if the line is matching pattern
                    Matcher m = pattern.matcher( line );
                    if ( m.matches() )
                    {
                        checkedFiles.add( m.group( 1 ) );
                    }
                }
            }, stdout );
            //Check if the update command successeed
            if ( exitCode != 0 )
            {
                return new CheckOutScmResult( updateCommandline.toString(),
                                              "The AccuRev command failed. Cannot update workspace",
                                              stdout.getOutput(), false );
            }
            return new CheckOutScmResult( makeCommandline.toString() + " & " + updateCommandline.toString(),
                                          checkedFiles );
        }
        catch ( CommandLineException e )
        {
            throw new ScmException( "internal error" );
        }
    }

    protected Commandline createUpdateWorkspaceCommand( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                                        ScmVersion version )
    {
        //TODO Implement support of fileSet, version if applyable
        Commandline commandline = new Commandline();
        commandline.setExecutable( this.accuRevExecutable );
        ArrayList params = new ArrayList();
        //Append command name
        params.add( "update" );
        //Append host if needed
        AccuRevScmProvider.appendHostToParamsIfNeeded( repository, params );
        //Append command parameters
        params.add( "-r" );
        params.add( repository.getWorkspaceName() );
        //Set command parameters
        commandline.addArguments( (String[]) params.toArray( new String[params.size()] ) );
        return commandline;
    }

    protected Commandline createMakeWorkspaceCommandLine( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                                          ScmVersion version )
    {
        //TODO Implement support of fileSet, version if applyable
        Commandline cmd = new Commandline();
        cmd.setExecutable( this.accuRevExecutable );
        //Append command name
        cmd.addArguments( new String[] { "mkws" } );
        //Append host param if needed
        List params = new ArrayList();
        AccuRevScmProvider.appendHostToParamsIfNeeded( repository, params );
        cmd.addArguments( (String[]) params.toArray( new String[params.size()] ) );
        //Append command arguments
        cmd.addArguments( new String[] {
            "-w",
            repository.getWorkspaceName(),
            "-b",
            repository.getStreamName(),
            "-l",
            fileSet.getBasedir().getAbsolutePath() } );
        return cmd;
    }
}
