package org.apache.maven.scm.provider.svn.command.checkin;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class SvnCheckInCommand
    extends AbstractCheckInCommand
{
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                                      String tag )
        throws ScmException
    {
        // TODO: it should
        if ( fileSet.getFiles().length != 0 )
        {
            throw new ScmException( "This command can only commit entire working directories." );
        }

        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "This provider can't handle tags." );
        }

        File messageFile = FileUtils.createTempFile( "maven-scm-", ".commit", null );

        try
        {
            FileUtils.fileWrite( messageFile.getAbsolutePath(), message );
        }
        catch( IOException ex )
        {
            return new CheckInScmResult( "Error while making a temporary file for the commit message: " + ex.getMessage(), null, false );
        }

        Commandline cl = createCommandLine( (SvnScmProviderRepository) repo, fileSet.getBasedir(), messageFile );

        SvnCheckInConsumer consumer = new SvnCheckInConsumer( getLogger(), fileSet.getBasedir().getParentFile() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        getLogger().info( "Working directory: " + fileSet.getBasedir().getAbsolutePath() );
        getLogger().info( "Command line: " + cl );

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }
        finally
        {
            try
            {
                FileUtils.forceDelete( messageFile );
            }
            catch( IOException ex )
            {
                // ignore
            }
        }

        if ( exitCode != 0 )
        {
            return new CheckInScmResult( "The svn command failed.", stderr.getOutput(), false );
        }

        return new CheckInScmResult( consumer.getCheckedInFiles() );
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public static Commandline createCommandLine( SvnScmProviderRepository repository, File workingDirectory, File messageFile )
    {
        Commandline cl = new Commandline();

        cl.setExecutable( "svn" );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        cl.createArgument().setValue( "commit" );

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

        cl.createArgument().setValue( "--file" );

        cl.createArgument().setValue( messageFile.getAbsolutePath() );

        return cl;
    }
}
