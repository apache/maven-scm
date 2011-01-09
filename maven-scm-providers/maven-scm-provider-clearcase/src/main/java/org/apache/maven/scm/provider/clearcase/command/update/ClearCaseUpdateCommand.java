package org.apache.maven.scm.provider.clearcase.command.update;

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
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.ClearCaseCommand;
import org.apache.maven.scm.provider.clearcase.command.changelog.ClearCaseChangeLogCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 * @author Olivier Lamy
 * @version $Id$
 */
public class ClearCaseUpdateCommand
    extends AbstractUpdateCommand
    implements ClearCaseCommand
{
    /** {@inheritDoc} */
    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repository, ScmFileSet fileSet,
                                                    ScmVersion version )
        throws ScmException
    {
        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing update command..." );
        }
        Commandline cl = createCommandLine( fileSet );

        ClearCaseUpdateConsumer consumer = new ClearCaseUpdateConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug(
                                   "Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>"
                                       + cl.toString() );
            }
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing clearcase command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new UpdateScmResult( cl.toString(), "The cleartool command failed.", stderr.getOutput(), false );
        }

        return new UpdateScmResult( cl.toString(), consumer.getUpdatedFiles() );
    }

    /** {@inheritDoc} */
    protected ChangeLogCommand getChangeLogCommand()
    {
        ClearCaseChangeLogCommand changeLogCmd = new ClearCaseChangeLogCommand();

        changeLogCmd.setLogger( getLogger() );

        return changeLogCmd;
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( ScmFileSet scmFileSet )
    {
        Commandline command = new Commandline();

        File workingDirectory = scmFileSet.getBasedir();

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        command.setExecutable( "cleartool" );

        command.createArg().setValue( "update" );

        command.createArg().setValue( "-f" );

        return command;
    }
}
