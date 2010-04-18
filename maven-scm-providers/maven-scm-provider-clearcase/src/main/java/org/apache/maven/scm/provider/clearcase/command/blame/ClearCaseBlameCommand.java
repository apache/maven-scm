package org.apache.maven.scm.provider.clearcase.command.blame;

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
import org.apache.maven.scm.command.blame.AbstractBlameCommand;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.clearcase.command.ClearCaseCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author Jérémie Lagarde
 * @since 1.4
 */
public class ClearCaseBlameCommand
    extends AbstractBlameCommand
    implements ClearCaseCommand
{

    public BlameScmResult executeBlameCommand( ScmProviderRepository repo, ScmFileSet workingDirectory, String filename )
        throws ScmException
    {

        if ( getLogger().isDebugEnabled() )
        {
            getLogger().debug( "executing blame command..." );
        }
        Commandline cl = createCommandLine( workingDirectory.getBasedir(), filename );

        ClearCaseBlameConsumer consumer = new ClearCaseBlameConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            if ( getLogger().isDebugEnabled() )
            {
                getLogger().debug( "Executing: " + cl.getWorkingDirectory().getAbsolutePath() + ">>" + cl.toString() );
            }
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing cvs command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new BlameScmResult( cl.toString(), "The cleartool command failed.", stderr.getOutput(), false );
        }

        return new BlameScmResult( cl.toString(), consumer.getLines() );
    }

    public static Commandline createCommandLine( File workingDirectory, String filename )
    {
        Commandline command = new Commandline();
        command.setExecutable( "cleartool" );
        command.createArg().setValue( "annotate" );

        command.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        StringBuffer format = new StringBuffer();
        format.append( "VERSION:%Ln@@@" );
        format.append( "USER:%u@@@" );
        format.append( "DATE:%Nd@@@" );

        command.createArg().setValue( "-out" );
        command.createArg().setValue( "-" );
        command.createArg().setValue( "-fmt" );
        command.createArg().setValue( format.toString() );
        command.createArg().setValue( "-nheader" );
        command.createArg().setValue( "-f" );
        command.createArg().setValue( filename );

        return command;
    }
}
