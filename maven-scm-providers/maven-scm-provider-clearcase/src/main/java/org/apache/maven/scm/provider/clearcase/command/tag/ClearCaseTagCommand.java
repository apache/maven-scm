package org.apache.maven.scm.provider.clearcase.command.tag;

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
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.ClearCaseCommand;
import org.apache.maven.scm.provider.clearcase.command.checkin.ClearCaseCheckInConsumer;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 */
public class ClearCaseTagCommand
    extends AbstractTagCommand
    implements ClearCaseCommand
{

    protected ScmResult executeTagCommand( ScmProviderRepository scmProviderRepository, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        getLogger().debug( "executing tag command..." );
        Commandline cl = createCommandLine( fileSet, tag );

        ClearCaseCheckInConsumer consumer = new ClearCaseCheckInConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            getLogger().debug( "Creating label: " + tag );
            Commandline newLabelCommandLine = createNewLabelCommandLine( fileSet, tag );
            getLogger().debug( "Executing: " + newLabelCommandLine.getWorkingDirectory().getAbsolutePath() + ">>" +
                newLabelCommandLine.toString() );
            exitCode = CommandLineUtils.executeCommandLine( newLabelCommandLine,
                                                            new CommandLineUtils.StringStreamConsumer(), stderr );

            if ( exitCode == 0 )
            {
                getLogger().debug( "Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" + cl.toString() );
                exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
            }
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing clearcase command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new TagScmResult( cl.toString(), "The cleartool command failed.", stderr.getOutput(), false );
        }

        return new TagScmResult( cl.toString(), consumer.getCheckedInFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( ScmFileSet scmFileSet, String tag )
    {
        Commandline command = new Commandline();

        File workingDirectory = scmFileSet.getBasedir();

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        command.setExecutable( "cleartool" );

        command.createArgument().setValue( "mklabel" );
        File[] files = scmFileSet.getFiles();
        if ( files.length == 0 )
        {
            command.createArgument().setValue( "-recurse" );
        }
        command.createArgument().setValue( tag );

        if ( files.length > 0 )
        {
            for ( int i = 0; i < files.length; i++ )
            {
                File file = files[i];
                command.createArgument().setValue( file.getName() );
            }
        }
        else
        {
            command.createArgument().setValue( "." );
        }

        return command;
    }

    private static Commandline createNewLabelCommandLine( ScmFileSet scmFileSet, String tag )
    {
        Commandline command = new Commandline();

        File workingDirectory = scmFileSet.getBasedir();

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        command.setExecutable( "cleartool" );

        command.createArgument().setValue( "mklbtype" );
        command.createArgument().setValue( "-nc" );
        command.createArgument().setValue( tag );

        return command;
    }
}
