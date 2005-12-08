package org.apache.maven.scm.provider.clearcase.command.status;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.status.AbstractStatusCommand;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.ClearCaseCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 * @version
 */
public class ClearCaseStatusCommand
    extends AbstractStatusCommand
    implements ClearCaseCommand
{
    protected StatusScmResult executeStatusCommand( ScmProviderRepository scmProviderRepository,
                                                    ScmFileSet scmFileSet ) throws ScmException
    {
        getLogger().debug( "executing status command..." );
        Commandline cl = createCommandLine( scmFileSet );

        ClearCaseStatusConsumer consumer = new ClearCaseStatusConsumer( getLogger(), scmFileSet.getBasedir() );

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
            return new StatusScmResult( cl.toString(), "The cleartool command failed.", stderr.getOutput(), false );
        }

        return new StatusScmResult( cl.toString(), consumer.getCheckedOutFiles() );
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

        command.createArgument().setValue( "lscheckout" );
        command.createArgument().setValue( "-r" );
        command.createArgument().setValue( "-fmt" );
        command.createArgument().setValue( "%n\\n");

        return command;
    }
}
