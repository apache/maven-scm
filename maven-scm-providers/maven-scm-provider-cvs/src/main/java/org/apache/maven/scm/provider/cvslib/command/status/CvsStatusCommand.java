package org.apache.maven.scm.provider.cvslib.command.status;

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
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class CvsStatusCommand
    extends AbstractStatusCommand
    implements CvsCommand
{
    protected StatusScmResult executeStatusCommand( ScmProviderRepository repo, ScmFileSet fileSet )
        throws ScmException
    {
        Commandline cl = createCommandLine( fileSet );

        CvsStatusConsumer consumer = new CvsStatusConsumer( getLogger(), fileSet.getBasedir() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        int exitCode;

        getLogger().debug( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );
        getLogger().debug( "Command line: " + cl );

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
            return new StatusScmResult( cl.toString(), "The cvs command failed.", stderr.getOutput(), false );
        }

        return new StatusScmResult( cl.toString(), consumer.getChangedFiles() );
    }

    public Commandline createCommandLine( ScmFileSet fileSet )
        throws ScmException
    {
        Commandline cl = new Commandline();

        cl.setExecutable( "cvs" );

        cl.setWorkingDirectory( fileSet.getBasedir().getAbsolutePath() );

        cl.createArgument().setValue( "-f" ); // don't use ~/.cvsrc

        cl.createArgument().setValue( "-n" );

        cl.createArgument().setValue( "-q" );

        cl.createArgument().setValue( "update" );

        cl.createArgument().setValue( "-d" );

        return cl;
    }
}
