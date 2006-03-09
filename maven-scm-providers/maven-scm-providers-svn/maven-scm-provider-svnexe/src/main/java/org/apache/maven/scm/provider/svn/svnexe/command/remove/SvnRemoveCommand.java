package org.apache.maven.scm.provider.svn.svnexe.command.remove;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnexe.command.SvnCommandLineUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SvnRemoveCommand
    extends AbstractRemoveCommand
    implements SvnCommand
{
    protected ScmResult executeRemoveCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message )
        throws ScmException
    {
        if ( fileSet.getFiles().length == 0 )
        {
            throw new ScmException( "You must provide at least one file/directory to remove" );
        }

        Commandline cl = createCommandLine( fileSet.getBasedir(), fileSet.getFiles() );

        SvnRemoveConsumer consumer = new SvnRemoveConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Executing: " + SvnCommandLineUtils.cryptPassword( cl ) );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        int exitCode;

        try
        {
            exitCode = SvnCommandLineUtils.execute( cl, consumer, stderr, getLogger() );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new RemoveScmResult( cl.toString(), "The svn command failed.", stderr.getOutput(), false );
        }

        return new RemoveScmResult( cl.toString(), consumer.getRemovedFiles() );
    }

    private static Commandline createCommandLine( File workingDirectory, File[] files )
    {
        // Base command line doesn't make sense here - username/password not needed, and non-interactive/non-recusive is not valid

        Commandline cl = new Commandline();

        cl.setExecutable( "svn" );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        cl.createArgument().setValue( "remove" );

        SvnCommandLineUtils.addFiles( cl, files );

        return cl;
    }

}
