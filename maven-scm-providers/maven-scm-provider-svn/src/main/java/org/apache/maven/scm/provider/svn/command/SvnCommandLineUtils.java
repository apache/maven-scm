package org.apache.maven.scm.provider.svn.command;

/*
 * Copyright 2003-2005 The Apache Software Foundation.
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

import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;

import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

/**
 * Command line construction utility.
 *
 * @author Brett Porter
 * @version $Id$
 */
public class SvnCommandLineUtils
{
    public static void addFiles( Commandline cl, File[] files )
    {
        for ( int i = 0; i < files.length; i++ )
        {
            cl.createArgument().setValue( files[i].getPath().replace( '\\', '/' ) );
        }
    }

    public static Commandline getBaseSvnCommandLine( File workingDirectory, SvnScmProviderRepository repository )
    {
        Commandline cl = new Commandline();

        cl.setExecutable( "svn" );

        cl.setWorkingDirectory( workingDirectory.getAbsolutePath() );

        if ( !StringUtils.isEmpty( repository.getUser() ) )
        {
            cl.createArgument().setValue( "--username" );

            cl.createArgument().setValue( repository.getUser() );
        }

        if ( !StringUtils.isEmpty( repository.getPassword() ) )
        {
            cl.createArgument().setValue( "--password" );

            cl.createArgument().setValue( repository.getPassword() );
        }

        cl.createArgument().setValue( "--non-interactive" );

        return cl;
    }
}
