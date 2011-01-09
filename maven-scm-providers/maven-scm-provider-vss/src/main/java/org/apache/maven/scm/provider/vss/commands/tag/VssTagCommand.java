package org.apache.maven.scm.provider.vss.commands.tag;

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
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.vss.commands.VssCommandLineUtils;
import org.apache.maven.scm.provider.vss.commands.VssConstants;
import org.apache.maven.scm.provider.vss.repository.VssScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:matpimenta@gmail.com">Mateus Pimenta</a>
 * @since 1.3
 */
public class VssTagCommand
    extends AbstractTagCommand
{

    protected ScmResult executeTagCommand( ScmProviderRepository repository, ScmFileSet fileSet, String tagName,
                                           ScmTagParameters scmTagParameters )
        throws ScmException
    {
        return executeTagCommand( repository, fileSet, tagName, scmTagParameters == null ? "" : scmTagParameters
            .getMessage() );
    }


    /**
     * @see org.apache.maven.scm.command.tag.AbstractTagCommand#executeTagCommand(org.apache.maven.scm.provider.ScmProviderRepository, org.apache.maven.scm.ScmFileSet, java.lang.String, java.lang.String)
     */
    protected ScmResult executeTagCommand( ScmProviderRepository repository, ScmFileSet fileSet, String tagName,
                                           String message )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing tag command..." );
        }

        VssScmProviderRepository repo = (VssScmProviderRepository) repository;

        Commandline cl = buildCmdLine( repo, fileSet, tagName, message );

        VssTagConsumer consumer = new VssTagConsumer( repo, getLogger() );

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
                return new TagScmResult( cl.toString(), "The vss command failed.", error, false );
            }
            // print out the writable copy for manual handling
            if ( getLogger().isWarnEnabled() )
            {
                getLogger().warn( error );
            }
        }

        return new TagScmResult( cl.toString(), consumer.getUpdatedFiles() );
    }

    public Commandline buildCmdLine( VssScmProviderRepository repo, ScmFileSet fileSet, String tagName, String message )
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

        command.createArg().setValue( VssConstants.COMMAND_LABEL );

        command.createArg().setValue( VssConstants.PROJECT_PREFIX + repo.getProject() );

        // User identification to get access to vss repository
        if ( repo.getUserPassword() != null )
        {
            command.createArg().setValue( VssConstants.FLAG_LOGIN + repo.getUserPassword() );
        }

        // Ignore: Do not ask for input under any circumstances.
        command.createArg().setValue( VssConstants.FLAG_AUTORESPONSE_DEF );

        command.createArg().setValue( VssConstants.FLAG_LABEL + tagName );

        return command;
    }

}
