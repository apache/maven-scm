package org.apache.maven.scm.provider.cvslib.command.remove;

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
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.remove.AbstractRemoveCommand;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 * @todo separate the CVSlib stuff from the cvs command line so it is clear what needs to be updated eventually
 */
public class CvsRemoveCommand
    extends AbstractRemoveCommand
    implements CvsCommand
{
    protected ScmResult executeRemoveCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = new Commandline();

        cl.setExecutable( "cvs" );

        cl.setWorkingDirectory( fileSet.getBasedir().getAbsolutePath() );

        cl.createArgument().setValue( "-f" ); // don't use ~/.cvsrc

        cl.createArgument().setValue( "-d" );

        cl.createArgument().setValue( repository.getCvsRoot() );

        cl.createArgument().setValue( "-q" );

        cl.createArgument().setValue( "remove" );

        cl.createArgument().setValue( "-f" );

        cl.createArgument().setValue( "-l" );

        if ( message != null && message.length() > 0 )
        {
            cl.createArgument().setValue( "-m" );

            cl.createArgument().setValue( "\"" + message + "\"" );
        }

        File[] files = fileSet.getFiles();
        List removedFiles = new ArrayList();
        for ( int i = 0; i < files.length; i++ )
        {
            String path = files[i].getPath().replace( '\\', '/' );
            cl.createArgument().setValue( path );
            removedFiles.add( new ScmFile( path, ScmFileStatus.DELETED ) );
        }

        CommandLineUtils.StringStreamConsumer consumer = new CommandLineUtils.StringStreamConsumer();

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

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

        // TODO: actually it may have partially succeeded - should we cvs update the files and parse "A " responses?
        if ( exitCode != 0 )
        {
            return new RemoveScmResult( cl.toString(), "The cvs command failed.", stderr.getOutput(), false );
        }

        return new RemoveScmResult( cl.toString(), removedFiles );
    }
}
