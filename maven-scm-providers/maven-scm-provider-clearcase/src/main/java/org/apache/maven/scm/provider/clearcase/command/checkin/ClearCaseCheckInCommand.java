package org.apache.maven.scm.provider.clearcase.command.checkin;

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
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.ClearCaseCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 * @version $Id$
 */
public class ClearCaseCheckInCommand
    extends AbstractCheckInCommand
    implements ClearCaseCommand
{
    // ----------------------------------------------------------------------
    // AbstractCheckOutCommand Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository scmProviderRepository, ScmFileSet fileSet,
                                                      String message, ScmVersion version )
        throws ScmException
    {
        getLogger().debug( "executing checkin command..." );
        Commandline cl = createCommandLine( fileSet, message );

        ClearCaseCheckInConsumer consumer = new ClearCaseCheckInConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            getLogger().debug( "Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" + cl.toString() );
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing clearcase command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new CheckInScmResult( cl.toString(), "The cleartool command failed.", stderr.getOutput(), false );
        }

        return new CheckInScmResult( cl.toString(), consumer.getCheckedInFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( ScmFileSet scmFileSet, String message )
        throws ScmException
    {
        Commandline command = new Commandline();

        File workingDirectory = scmFileSet.getBasedir();

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        command.setExecutable( "cleartool" );

        command.createArgument().setValue( "ci" );

        if ( message != null )
        {
            command.createArgument().setValue( "-c" );
            command.createArgument().setLine( "\"" + message + "\"" );
        }
        else
        {
            command.createArgument().setValue( "-nc" );
        }

        File[] files = scmFileSet.getFiles();
        if ( files.length == 0 )
        {
            throw new ScmException( "There are no files in the fileset to check in!" );
        }
        for ( int i = 0; i < files.length; i++ )
        {
            File file = files[i];
            command.createArgument().setValue( file.getAbsolutePath() );
        }

        return command;
    }
}
