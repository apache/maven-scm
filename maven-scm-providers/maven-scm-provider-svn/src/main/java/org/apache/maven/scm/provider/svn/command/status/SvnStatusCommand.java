package org.apache.maven.scm.provider.svn.command.status;

/*
 * Copyright 2003-2004 The Apache Software Foundation.
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
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class SvnStatusCommand
    extends AbstractStatusCommand
    implements SvnCommand
{
    protected StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        Commandline cl = createCommandLine( (SvnScmProviderRepository) repo, fileSet );

        SvnStatusConsumer consumer = new SvnStatusConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        if ( exitCode != 0 )
        {
            return new StatusScmResult( "The svn command failed.", stderr.getOutput(), false );
        }

        return new StatusScmResult( consumer.getChangedFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, ScmFileSet fileSet )
    {
        Commandline cl = new Commandline();

        cl.setExecutable( "svn" );

        cl.setWorkingDirectory( fileSet.getBasedir().getAbsolutePath() );

        cl.createArgument().setValue( "status" );

        cl.createArgument().setValue( "--non-interactive" );

        if ( repository.getUser() != null )
        {
            cl.createArgument().setValue( "--username" );

            cl.createArgument().setValue( repository.getUser() );
        }

        if ( repository.getPassword() != null )
        {
            cl.createArgument().setValue( "--password" );

            cl.createArgument().setValue( repository.getPassword() );
        }

        return cl;
    }
}
