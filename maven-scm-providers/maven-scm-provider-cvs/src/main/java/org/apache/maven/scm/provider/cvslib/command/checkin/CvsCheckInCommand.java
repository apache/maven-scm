package org.apache.maven.scm.provider.cvslib.command.checkin;

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

import java.io.File;
import java.io.IOException;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.checkin.AbstractCheckInCommand;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsCheckInCommand
    extends AbstractCheckInCommand
    implements CvsCommand
{
    protected CheckInScmResult executeCheckInCommand( ScmProviderRepository repo, File workingDirectory, String message, String tag, File[] files )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = new Commandline();

        cl.setExecutable( "cvs" );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        File messageFile;

        try
        {
            messageFile = File.createTempFile( "scm-commit-message", ".txt" );

            FileUtils.fileWrite( messageFile.getAbsolutePath(), message );
        }
        catch( IOException ex )
        {
            throw new ScmException( "Error while making a temporary commit message file." );
        }

        cl.createArgument().setValue( "-d" );

        cl.createArgument().setValue( repository.getCvsRoot() );

        cl.createArgument().setValue( "-q" );

        cl.createArgument().setValue( "commit" );

        if ( !StringUtils.isEmpty( tag ) )
        {
            cl.createArgument().setValue( "-r" + tag );
        }

        cl.createArgument().setValue( "-R" );

        cl.createArgument().setValue( "-F" );

        cl.createArgument().setValue( messageFile.getAbsolutePath() );

        // TODO: should be committing files here instead - for now check in everything
        // cl.createArgument().setValue( repository.getModule() );

        CvsCheckInConsumer consumer = new CvsCheckInConsumer( repository.getPath() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        try
        {
            FileUtils.forceDelete( messageFile );
        }
        catch( IOException ex )
        {
            // ignore
        }

        if ( exitCode != 0 )
        {
            return new CheckInScmResult( "The cvs command failed.", stderr.getOutput(), false );
        }

        return new CheckInScmResult( consumer.getCheckedInFiles() );
    }
}
