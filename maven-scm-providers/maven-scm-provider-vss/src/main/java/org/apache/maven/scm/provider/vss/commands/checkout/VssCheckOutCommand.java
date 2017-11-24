package org.apache.maven.scm.provider.vss.commands.checkout;

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
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.vss.commands.VssCommandLineUtils;
import org.apache.maven.scm.provider.vss.commands.VssConstants;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:triek@thrx.de">Thorsten Riek</a>
 * @version $Id: VssCheckOutCommand.java 02.06.2006 00:05:51
 */
public class VssCheckOutCommand
    extends AbstractCheckOutCommand
{

    /** {@inheritDoc} */
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                       ScmVersion version, boolean recursive, boolean shallow )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing checkout command..." );
        }

        VssScmProviderRepository repo = (VssScmProviderRepository) repository;

        Commandline cl = buildCmdLine( repo, fileSet, version );

        VssCheckOutConsumer consumer = new VssCheckOutConsumer( repo, getLogger() );

        //      TODO handle deleted files from VSS
        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

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
                return new CheckOutScmResult( cl.toString(), "The vss command failed.", error, false );
            }
            // print out the writable copy for manual handling
            if ( getLogger().isWarnEnabled() )
            {
                getLogger().warn( error );
            }
        }

        return new CheckOutScmResult( cl.toString(), consumer.getUpdatedFiles() );
    }

    public Commandline buildCmdLine( VssScmProviderRepository repo, ScmFileSet fileSet, ScmVersion version )
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

        command.createArg().setValue( VssConstants.COMMAND_GET );

        command.createArg().setValue( VssConstants.PROJECT_PREFIX + repo.getProject() );

        //User identification to get access to vss repository
        if ( repo.getUserPassword() != null )
        {
            command.createArg().setValue( VssConstants.FLAG_LOGIN + repo.getUserPassword() );
        }

        //Display the history of an entire project list
        command.createArg().setValue( VssConstants.FLAG_RECURSION );

        //Ignore: Do not ask for input under any circumstances.
        command.createArg().setValue( VssConstants.FLAG_AUTORESPONSE_DEF );

        //Ignore: Do not touch local writable files.
        command.createArg().setValue( VssConstants.FLAG_REPLACE_WRITABLE );

        if ( version != null )
        {
            command.createArg().setValue( VssConstants.FLAG_VERSION_LABEL + '"' + version + '"' );
        }

        return command;
    }
}
