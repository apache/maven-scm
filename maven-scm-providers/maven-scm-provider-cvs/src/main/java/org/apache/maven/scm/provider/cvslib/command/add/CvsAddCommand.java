package org.apache.maven.scm.provider.cvslib.command.add;

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
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
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
public class CvsAddCommand extends AbstractAddCommand implements CvsCommand
{
    protected ScmResult executeAddCommand( ScmProviderRepository repo, ScmFileSet fileSet, String message,
                                           boolean binary )
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

        cl.createArgument().setValue( "add" );

        if ( binary )
        {
            cl.createArgument().setValue( "-kb" );
        }

        if ( message != null && message.length() > 0 )
        {
            cl.createArgument().setValue( "-m" );

            cl.createArgument().setValue( "\"" + message + "\"" );
        }

        File[] files = fileSet.getFiles();
        List addedFiles = new ArrayList();
        for ( int i = 0; i < files.length; i++ )
        {
            String path = files[i].getPath().replace( '\\', '/' );
            cl.createArgument().setValue( path );
            addedFiles.add( new ScmFile( path, ScmFileStatus.ADDED ) );
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
            return new AddScmResult( "The cvs command failed.", stderr.getOutput(), false );
        }

        return new AddScmResult( addedFiles );
    }
}
