package org.apache.maven.scm.provider.clearcase.command.unedit;

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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.unedit.AbstractUnEditCommand;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.ClearCaseCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 */
public class ClearCaseUnEditCommand
        extends AbstractUnEditCommand
        implements ClearCaseCommand
{
    protected ScmResult executeUnEditCommand( ScmProviderRepository repository, ScmFileSet fileSet ) throws ScmException
    {
        getLogger().error( "executing edit command..." );
        Commandline cl = createCommandLine( getLogger(), fileSet );

        ClearCaseUnEditConsumer consumer = new ClearCaseUnEditConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch (CommandLineException ex)
        {
            throw new ScmException( "Error while executing clearcase command.", ex );
        }

        if (exitCode != 0)
        {
            return new StatusScmResult( cl.toString(), "The cleartool command failed.", stderr.getOutput(), false );
        }

        return new StatusScmResult( cl.toString(), consumer.getUnEditFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( ScmLogger logger, ScmFileSet scmFileSet )
    {
        Commandline command = new Commandline();

        File workingDirectory = scmFileSet.getBasedir();

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        command.setExecutable( "cleartool" );

        command.createArgument().setValue( "unco" );
        command.createArgument().setValue( "-keep" );

        File[] files = scmFileSet.getFiles();
        for (int i = 0; i < files.length; i++)
        {
            File file = files[i];
            logger.info( "Checking out file: " + file.getAbsolutePath() );
            command.createArgument().setValue( file.getName() );
        }

        return command;
    }
}
